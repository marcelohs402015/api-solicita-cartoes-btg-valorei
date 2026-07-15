package com.btg.proposals.repository;

import com.btg.proposals.model.entity.EmailDisparoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailDisparoRepository extends JpaRepository<EmailDisparoEntity, UUID> {

    List<EmailDisparoEntity> findAllByOrderByCriadoEmDesc(Pageable pageable);

    Optional<EmailDisparoEntity> findByPropostaId(UUID propostaId);
}
