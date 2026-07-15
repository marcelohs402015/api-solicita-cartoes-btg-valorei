# Code Review: Proposals API (solução completa)

**Data da revisão:** 2026-07-15  
**Branch:** main  
**Escopo:** Backend (Spring Boot) + Frontend (React) + Infra (Docker/Kafka/Postgres)  
**Revisor:** Code Review Skill (flow-ciandt)

## Nota geral: 7/10

A solução atende bem ao escopo de POC: arquitetura em camadas, Strategy para elegibilidade, Kafka com workers, Postgres + Flyway, dashboard funcional e testes unitários com cobertura. Os principais riscos estão na **consistência transacional com Kafka**, **idempotência dos consumers** e **observabilidade enganosa** (status de fila e fluxo de execução). Para produção, esses pontos precisam ser endereçados antes de escalar.

---

## Críticos

### Issue 1: Publicação Kafka dentro da transação sem garantia de entrega

**Fonte da correção:** Clean Code / Arquitetura

**Localização:** `backend/src/main/java/com/btg/proposals/service/ProposalService.java:41-92`  
`backend/src/main/java/com/btg/proposals/messaging/ProposalEventPublisher.java:18-20`

**Código atual**
```java
@Transactional
public ProposalResponseDTO process(ProposalRequestDTO request) {
    // ...
    propostaRepository.save(proposta);
    historicoWorker.registerSync(proposalId, "PROPOSTA_PERSISTIDA", event);
    eventPublisher.publish(event);
    historicoWorker.registerSync(proposalId, "EVENTO_KAFKA_PUBLICADO", event);
    return response;
}

public void publish(ProposalEventDTO event) {
    kafkaTemplate.send(topic, event.getProposalId().toString(), event);
}
```

**Código sugerido**
```java
// Opção 1: Outbox pattern (recomendado em produção)
// Opção 2: Publicar após commit
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onProposalCommitted(ProposalCommittedEvent e) {
    kafkaTemplate.send(topic, e.proposalId().toString(), e.payload())
        .whenComplete((result, ex) -> {
            if (ex != null) historicoWorker.registerSync(e.proposalId(), "EVENTO_KAFKA_FALHOU", e.payload());
            else historicoWorker.registerSync(e.proposalId(), "EVENTO_KAFKA_PUBLICADO", e.payload());
        });
}
```

**Comentários**  
`KafkaTemplate.send()` é assíncrono e não garante entrega no fechamento da transação. Cenários possíveis: DB commita e mensagem se perde; ou mensagem chega ao broker e a transação faz rollback. O histórico `EVENTO_KAFKA_PUBLICADO` é gravado como sucesso sem confirmação real.

---

### Issue 2: Credenciais padrão em `application.yml`

**Fonte da correção:** Segurança / Project Guidelines

**Localização:** `backend/src/main/resources/application.yml:40-42`

**Código atual**
```yaml
security:
  username: ${APP_SECURITY_USERNAME:admin}
  password: ${APP_SECURITY_PASSWORD:admin123}
```

**Código sugerido**
```yaml
security:
  username: ${APP_SECURITY_USERNAME}
  password: ${APP_SECURITY_PASSWORD}
```

**Comentários**  
Aceitável em POC local, mas perigoso se o deploy esquecer de sobrescrever variáveis. O mesmo vale para credenciais do Postgres (`proposals/proposals`).

---

## Maiores

### Issue 3: Consumers Kafka sem idempotência — e-mails duplicados

**Fonte da correção:** DRY / Clean Code

**Localização:** `backend/src/main/java/com/btg/proposals/service/EmailWorker.java:33-54`  
`backend/src/main/resources/db/migration/V3__create_email_disparo.sql`

**Código atual**
```java
@KafkaListener(...)
public void process(ProposalEventDTO event) {
    if (event.getStatus() != ProposalStatus.APPROVED) return;
    EmailDisparoEntity entity = EmailDisparoEntity.builder()
        .id(UUID.randomUUID())
        .propostaId(event.getProposalId())
        // ...
        .build();
    emailDisparoRepository.save(entity);
}
```

**Código sugerido**
```java
if (emailDisparoRepository.findByPropostaId(event.getProposalId()).isPresent()) {
    return;
}
// Migration: CREATE UNIQUE INDEX uk_email_proposta ON email_disparo(proposta_id);
```

**Comentários**  
Kafka é at-least-once. Em retry/rebalance, o worker insere novo registro a cada entrega. O repositório já expõe `findByPropostaId`, mas o consumer não usa.

---

### Issue 4: Consumers sem idempotência — histórico duplicado

**Fonte da correção:** Clean Code

**Localização:** `backend/src/main/java/com/btg/proposals/service/HistoricoWorker.java:32-48`

**Comentários**  
Cada redelivery gera novo registro `STATUS_ALTERADO_KAFKA`. O campo `eventId` em `ProposalEventDTO` existe mas não é usado como chave de deduplicação.

---

### Issue 5: `QueueStatusService` reporta saúde fictícia

**Fonte da correção:** YAGNI / Clean Code

**Localização:** `backend/src/main/java/com/btg/proposals/service/QueueStatusService.java:18-36`

**Código atual**
```java
.status("ACTIVE")  // hardcoded
.recentMessagesCount((int) historicoRepository.count())  // total no DB, não Kafka
```

**Comentários**  
O dashboard de Filas mostra consumers sempre `ACTIVE` e "mensagens processadas" como count total do histórico. Operadores podem concluir erroneamente que Kafka está saudável.

---

### Issue 6: Fluxo de execução hardcoded como sucesso

**Fonte da correção:** Clean Code

**Localização:** `backend/src/main/java/com/btg/proposals/service/ProposalService.java:126-129`

**Código atual**
```java
steps.add(step("PUBLICACAO_KAFKA", "DONE", "Evento enviado ao topico"));
```

**Comentários**  
`PUBLICACAO_KAFKA` é sempre `DONE`, mesmo se o publish falhou. Apenas os workers assíncronos refletem estado real. O stepper do frontend pode mostrar sucesso enganoso.

---

### Issue 7: Workers sem validação de evento nulo

**Fonte da correção:** Clean Code

**Localização:** `EmailWorker.java:35-38`, `HistoricoWorker.java:34-44`

**Comentários**  
`event.getStatus() == null` não retorna cedo no email worker (`!= APPROVED` é true). `event.getTipoOferta().name()` pode gerar NPE. `proposalId` nulo causa violação de FK.

---

### Issue 8: Sem DLQ / error handler nos listeners Kafka

**Fonte da correção:** Clean Code

**Comentários**  
Mensagens inválidas (proposta inexistente, payload malformado) podem travar o consumer em loop de retry. Não há `DefaultErrorHandler`, DLQ ou skip policy configurados.

---

### Issue 9: `GlobalExceptionHandler` expõe mensagens internas

**Fonte da correção:** Segurança

**Localização:** `backend/src/main/java/com/btg/proposals/exception/GlobalExceptionHandler.java:75-85`

**Código atual**
```java
.details(List.of(ex.getMessage()))
```

**Comentários**  
Erros de JPA/SQL podem vazar para o cliente. Logar server-side e retornar mensagem genérica.

---

### Issue 10: Swagger público sem autenticação

**Fonte da correção:** Segurança

**Localização:** `backend/src/main/java/com/btg/proposals/config/SecurityConfig.java:34`

**Comentários**  
`/swagger-ui/**` e `/api-docs/**` são `permitAll()`. Em ambiente exposto, toda a superfície da API fica documentada publicamente.

---

### Issue 11: Frontend — 401 não sincroniza `AuthContext`

**Fonte da correção:** Clean Code

**Localização:** `frontend/src/services/api.ts:48-50`  
`frontend/src/contexts/AuthContext.tsx:12-18`

**Código atual**
```typescript
if (response.status === 401) {
  clearAuth();
  throw new Error('UNAUTHORIZED');
}
```

**Comentários**  
`clearAuth()` limpa `sessionStorage`, mas `AuthContext.authenticated` permanece `true`. O usuário continua vendo rotas protegidas até refresh ou logout manual. Só `ProposalForm` trata `UNAUTHORIZED` com redirect.

---

### Issue 12: Credenciais Basic Auth em `sessionStorage`

**Fonte da correção:** Segurança

**Localização:** `frontend/src/services/api.ts:15-17`

**Comentários**  
O header `Basic base64(user:pass)` fica em `sessionStorage`, vulnerável a XSS. Para POC é aceitável; em produção preferir cookie HttpOnly ou token com expiração curta.

---

### Issue 13: Polling sem tratamento de erro consistente

**Fonte da correção:** Clean Code

**Localização:** `frontend/src/pages/PropostasPage.tsx:8-11`  
`frontend/src/pages/HistoricoPage.tsx:10-11`  
`frontend/src/pages/FilasPage.tsx:9-16`

**Código atual (PropostasPage)**
```typescript
fetchPropostas(50).then(setPropostas);
const interval = setInterval(() => fetchPropostas(50).then(setPropostas), 3000);
```

**Comentários**  
`HistoricoPage` e `EmailsPage` tratam erro na carga inicial, mas o `setInterval` ignora falhas (incluindo 401). `FilasPage` não tem `.catch()` em `load()`. Erros silenciosos no polling.

---

### Issue 14: `ProposalForm` — `setTimeout` sem cleanup

**Fonte da correção:** Clean Code

**Localização:** `frontend/src/components/ProposalForm.tsx:41-44`

**Código atual**
```typescript
setTimeout(async () => {
  const flow = await fetchExecution(response.proposalId);
  setExecution(flow);
}, 1500);
```

**Comentários**  
Se o usuário navegar antes de 1,5s ou submeter outra proposta, o callback pode atualizar estado de componente desmontado ou proposta antiga. Usar `useRef` + `clearTimeout` no unmount.

---

### Issue 15: Sessão restaurada não redireciona para dashboard

**Fonte da correção:** Clean Code

**Localização:** `frontend/src/App.tsx:11-14`  
`frontend/src/contexts/AuthContext.tsx:15-18`

**Comentários**  
Com `authHeader` no `sessionStorage`, `authenticated` vira `true` no `useEffect`, mas se a rota atual for `/login`, o usuário permanece na tela de login até clicar em Entrar novamente.

---

## Menores

### Issue 16: `IllegalArgumentException` sempre retorna 404

**Localização:** `GlobalExceptionHandler.java:62-72`

Usado para "Proposta não encontrada", mas qualquer `IllegalArgumentException` vira 404.

---

### Issue 17: Security sem `STATELESS`

**Localização:** `SecurityConfig.java:28-39`

API REST com Basic Auth pode criar sessões HTTP desnecessárias.

---

### Issue 18: CORS sem `trim` em origens múltiplas

**Localização:** `backend/.../CorsConfig.java`

`"http://a.com, http://b.com"` com espaços pode falhar no match.

---

### Issue 19: Workers acumulam responsabilidades (consumer + query)

**Localização:** `EmailWorker`, `HistoricoWorker` usados em controllers e como `@KafkaListener`

Dificulta evolução e testes isolados.

---

### Issue 20: Kafka UI hardcoded no frontend

**Localização:** `frontend/src/pages/FilasPage.tsx:34`

`http://localhost:8090` quebra em Docker/produção sem ajuste.

---

### Issue 21: `openEmail` sem tratamento de erro

**Localização:** `frontend/src/pages/EmailsPage.tsx:16-18`

Falha na API não exibe feedback ao usuário.

---

### Issue 22: Lista de benefícios pode ser vazia

**Localização:** `ProposalRequestDTO` — `@NotNull` permite `[]`

Regras de negócio podem assumir ao menos um benefício dependendo da oferta.

---

## O que está correto

- Flyway + `ddl-auto: validate` + `open-in-view: false`
- FK constraints em `historico` e `email_disparo` → `proposta`
- Motor Strategy com 3 regras bem separadas e testadas
- Paginação com limite máximo nos endpoints de listagem
- Propostas rejeitadas não disparam e-mail
- Intervals de polling fazem cleanup no `useEffect` return
- Testes unitários backend (JaCoCo 60%) e frontend (Vitest ~97%)
- Docker multi-stage e compose com Postgres + Kafka + Kafka UI

---

## Resumo

| Severidade | Quantidade |
|------------|------------|
| Crítico    | 2          |
| Maior      | 13         |
| Menor      | 7          |
| **Total**  | **22**     |

### Prioridades recomendadas

1. **Transactional outbox** ou publish pós-commit com confirmação de entrega
2. **Idempotência** nos workers (`eventId` / `UNIQUE proposta_id`)
3. **Error handler Kafka** + validação de payload nos consumers
4. **Sincronizar 401** com `AuthContext` no frontend (callback ou evento global)
5. **Status honesto** em `QueueStatusService` e `getExecutionFlow()`
6. **Tratamento de erro** uniforme no polling das páginas

### Conclusão

Para um **desafio técnico / POC**, a implementação é sólida e demonstra bem o fluxo BTG (API → Postgres → Kafka → workers → dashboard). Para **produção**, os gaps de consistência Kafka-DB e idempotência são os bloqueadores mais importantes.
