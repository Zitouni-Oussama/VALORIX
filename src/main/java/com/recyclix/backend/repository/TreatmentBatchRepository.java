package com.recyclix.backend.repository;

import com.recyclix.backend.model.TreatmentBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TreatmentBatchRepository extends JpaRepository<TreatmentBatch, Long> {
    List<TreatmentBatch> findByStatus(TreatmentBatch.BatchStatus status);
    List<TreatmentBatch> findByMaterialId(Long materialId);
    List<TreatmentBatch> findByStatusAndMaterialId(TreatmentBatch.BatchStatus status, Long materialId);
    List<TreatmentBatch> findAllByOrderByStartedAtDesc();
}