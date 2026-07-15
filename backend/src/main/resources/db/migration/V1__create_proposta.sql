CREATE TABLE proposta (
    id UUID PRIMARY KEY,
    renda NUMERIC(19, 2) NOT NULL,
    investimentos NUMERIC(19, 2) NOT NULL,
    tempo_conta_anos INTEGER NOT NULL,
    tipo_oferta VARCHAR(1) NOT NULL,
    beneficios JSON NOT NULL,
    status VARCHAR(20) NOT NULL,
    motivos_rejeicao JSON,
    account_id VARCHAR(50),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_proposta_status ON proposta(status);
CREATE INDEX idx_proposta_criado_em ON proposta(criado_em DESC);
