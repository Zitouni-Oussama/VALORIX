package com.recyclix.backend.repository;

import com.recyclix.backend.model.WorkerAbsence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerAbsenceRepository extends JpaRepository<WorkerAbsence, Long> {

    List<WorkerAbsence> findByEmployeeId(Long employeeId);
}