package com.btg.proposals.model.entity;

import com.btg.proposals.model.enums.EmailStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "email_disparo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDisparoEntity {

    @Id
    private UUID id;

    @Column(name = "proposta_id", nullable = false)
    private UUID propostaId;

    @Column(nullable = false)
    private String destinatario;

    @Column(nullable = false)
    private String assunto;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_json", nullable = false)
    private Map<String, Object> templateJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailStatus status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
}
