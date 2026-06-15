package com.recyclix.backend.repository;

import com.recyclix.backend.model.ChallengeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeHistoryRepository extends JpaRepository<ChallengeHistory, Long> {

    List<ChallengeHistory> findAllByUserChallengeIdOrderByCreatedAtDesc(Long userChallengeId);

    List<ChallengeHistory> findAllByUserChallengeAccountIdOrderByCreatedAtDesc(Long accountId);
}