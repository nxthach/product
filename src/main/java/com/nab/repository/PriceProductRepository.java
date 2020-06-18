package com.nab.repository;

import com.nab.domain.PriceProduct;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the PriceProduct entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PriceProductRepository extends JpaRepository<PriceProduct, Long> {
}
