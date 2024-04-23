package com.assignment.ledger.entity.application.ports.out;

import com.assignment.ledger.entity.domain.LedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Here, JpaRepository is leveraged as the outbound port for simplified purpose, actually, a technology neutral
 * interface should be declared as the outbound port to decouple the port interface and its concrete implementation
 *
 * */
public interface LedgerEntityRepository extends JpaRepository<LedgerEntity, Long> {
}
