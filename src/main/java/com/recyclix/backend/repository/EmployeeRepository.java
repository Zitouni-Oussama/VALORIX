package com.recyclix.backend.repository;

import com.recyclix.backend.model.Employee;
import com.recyclix.backend.model.PayrollDeduction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByActiveTrue();

    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndPhone(
            String firstName, String lastName, String phone);

    Page<Employee> findByRecyclingCenterId(Long centerId, Pageable pageable);
    List<Employee> findByRecyclingCenterId(Long centerId);

}