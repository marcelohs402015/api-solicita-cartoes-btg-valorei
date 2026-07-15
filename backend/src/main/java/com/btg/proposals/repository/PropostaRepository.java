package com.btg.proposals.repository;

import com.btg.proposals.model.entity.PropostaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PropostaRepository extends JpaRepository<PropostaEntity, UUID> {

    List<PropostaEntity> findAllByOrderByCriadoEmDesc(Pageable pageable);
}
