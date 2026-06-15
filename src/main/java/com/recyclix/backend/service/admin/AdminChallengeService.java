package com.recyclix.backend.service.admin;

import com.recyclix.backend.dto.challenge.ChallengeRequestDTO;
import com.recyclix.backend.dto.challenge.ChallengeResponseDTO;
import com.recyclix.backend.dto.challenge.ChallengeSummaryDTO;
import com.recyclix.backend.dto.challenge.ChallengeUpdateDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ConflictException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.mapper.ChallengeMapper;
import com.recyclix.backend.model.Challenge;
import com.recyclix.backend.repository.ChallengeRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeMapper challengeMapper;

    public ChallengeResponseDTO createChallenge(ChallengeRequestDTO request) {
        validateCreateRequest(request);

        if (challengeRepository.existsByTitleIgnoreCase(request.getTitle().trim())) {
            throw new ConflictException("Un défi avec ce titre existe déjà.");
        }

        Challenge challenge = challengeMapper.toEntity(request);
        challenge.setTitle(request.getTitle().trim());
        challenge.setDescription(request.getDescription());
        challenge.setChallengeType(request.getChallengeType());
        challenge.setTargetMaterialId(request.getTargetMaterialId());
        challenge.setTargetValue(request.getTargetValue());
        challenge.setUnit(request.getUnit());
        challenge.setRewardPoints(request.getRewardPoints());
        challenge.setBonusPoints(request.getBonusPoints() != null ? request.getBonusPoints() : 0);
        challenge.setStartDate(request.getStartDate());
        challenge.setEndDate(request.getEndDate());
        challenge.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        challenge.setAutoValidate(request.getAutoValidate() != null ? request.getAutoValidate() : true);
        challenge.setMinQuantityPerCollection(request.getMinQuantityPerCollection());
        challenge.setMaxQuantityPerCollection(request.getMaxQuantityPerCollection());

        Challenge saved = challengeRepository.save(challenge);
        return challengeMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<ChallengeSummaryDTO> getAllChallenges(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return challengeRepository.findAll(pageable).map(challengeMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public ChallengeResponseDTO getChallengeById(Long id) {
        Challenge challenge = getChallengeOrThrow(id);
        return challengeMapper.toDto(challenge);
    }

    public ChallengeResponseDTO updateChallenge(Long id, ChallengeUpdateDTO request) {
        Challenge challenge = getChallengeOrThrow(id);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            challengeRepository.findByTitleIgnoreCase(request.getTitle().trim())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new ConflictException("Un défi avec ce titre existe déjà.");
                    });
            challenge.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null) challenge.setDescription(request.getDescription());
        if (request.getChallengeType() != null) challenge.setChallengeType(request.getChallengeType());
        if (request.getTargetMaterialId() != null) challenge.setTargetMaterialId(request.getTargetMaterialId());
        if (request.getTargetValue() != null) challenge.setTargetValue(request.getTargetValue());
        if (request.getUnit() != null) challenge.setUnit(request.getUnit());
        if (request.getRewardPoints() != null) challenge.setRewardPoints(request.getRewardPoints());
        if (request.getBonusPoints() != null) challenge.setBonusPoints(request.getBonusPoints());
        if (request.getStartDate() != null) challenge.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) challenge.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) challenge.setIsActive(request.getIsActive());
        if (request.getAutoValidate() != null) challenge.setAutoValidate(request.getAutoValidate());
        if (request.getMinQuantityPerCollection() != null) challenge.setMinQuantityPerCollection(request.getMinQuantityPerCollection());
        if (request.getMaxQuantityPerCollection() != null) challenge.setMaxQuantityPerCollection(request.getMaxQuantityPerCollection());

        return challengeMapper.toDto(challengeRepository.save(challenge));
    }

    public ChallengeResponseDTO activateChallenge(Long id) {
        Challenge challenge = getChallengeOrThrow(id);
        challenge.activate();
        return challengeMapper.toDto(challengeRepository.save(challenge));
    }

    public ChallengeResponseDTO deactivateChallenge(Long id) {
        Challenge challenge = getChallengeOrThrow(id);
        challenge.deactivate();
        return challengeMapper.toDto(challengeRepository.save(challenge));
    }

    public void deleteChallenge(Long id) {
        Challenge challenge = getChallengeOrThrow(id);
        challengeRepository.delete(challenge);
    }

    private Challenge getChallengeOrThrow(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Défi introuvable avec l'ID : " + id));
    }

    private void validateCreateRequest(ChallengeRequestDTO request) {
        if (request == null) throw new BadRequestException("Les données du défi sont obligatoires.");
        if (request.getTitle() == null || request.getTitle().isBlank())
            throw new BadRequestException("Le titre est obligatoire.");
        if (request.getChallengeType() == null)
            throw new BadRequestException("Le type de défi est obligatoire.");
        if (request.getTargetValue() == null || request.getTargetValue() <= 0)
            throw new BadRequestException("La valeur cible doit être supérieure à 0.");
        if (request.getUnit() == null)
            throw new BadRequestException("L'unité est obligatoire.");
        if (request.getRewardPoints() == null || request.getRewardPoints() <= 0)
            throw new BadRequestException("Les points de récompense doivent être supérieurs à 0.");
        if (request.getStartDate() != null && request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("La date de fin ne peut pas être avant la date de début.");
        }
    }

    // Dans AdminChallengeService.java
    @Transactional(readOnly = true)
    public Page<ChallengeSummaryDTO> getFilteredChallenges(String keyword, Boolean isActive, String challengeType, Pageable pageable) {
        Specification<Challenge> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            if (challengeType != null && !challengeType.isBlank()) {
                predicates.add(cb.equal(root.get("challengeType"), Challenge.ChallengeType.valueOf(challengeType)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return challengeRepository.findAll(spec, pageable).map(challengeMapper::toSummaryDto);
    }



}