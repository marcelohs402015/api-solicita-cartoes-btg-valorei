package com.btg.proposals.repository;

import com.btg.proposals.model.entity.HistoricoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoRepository extends JpaRepository<HistoricoEntity, UUID> {

    List<HistoricoEntity> findAllByOrderByCriadoEmDesc(Pageable pageable);

    List<HistoricoEntity> findByPropostaIdOrderByCriadoEmAsc(UUID propostaId);

    long countByEvento(String evento);

    boolean existsBySourceEventId(UUID sourceEventId);
}
