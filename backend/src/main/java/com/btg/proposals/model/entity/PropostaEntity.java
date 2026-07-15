package com.btg.proposals.model.entity;

import com.btg.proposals.model.enums.BenefitType;
import com.btg.proposals.model.enums.OfferType;
import com.btg.proposals.model.enums.ProposalStatus;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "proposta")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropostaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private BigDecimal renda;

    @Column(nullable = false)
    private BigDecimal investimentos;

    @Column(name = "tempo_conta_anos", nullable = false)
    private Integer tempoContaAnos;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_oferta", nullable = false, columnDefinition = "varchar(1)")
    private OfferType tipoOferta;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<BenefitType> beneficios;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProposalStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "motivos_rejeicao")
    private List<String> motivosRejeicao;

    @Column(name = "account_id", length = 50)
    private String accountId;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
}
