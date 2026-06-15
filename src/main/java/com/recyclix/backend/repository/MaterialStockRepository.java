package com.recyclix.backend.repository;

import com.recyclix.backend.model.MaterialStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface MaterialStockRepository extends JpaRepository<MaterialStock, Long> {
    Optional<MaterialStock> findByMaterialId(Long materialId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM MaterialStock s WHERE s.material.id = :materialId")
    Optional<MaterialStock> lockByMaterialId(@Param("materialId") Long materialId);
}