package com.recyclix.backend.service.collector;

import com.recyclix.backend.dto.faq_entry.FaqEntryResponseDTO;
import com.recyclix.backend.dto.faq_entry.FaqEntrySummaryDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.mapper.FaqEntryMapper;
import com.recyclix.backend.model.FaqEntry;
import com.recyclix.backend.repository.FaqEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectorFaqService {

    private final FaqEntryRepository faqEntryRepository;
    private final FaqEntryMapper faqEntryMapper;

    //. -------------------- GET COLLECTOR FAQ -------------------- .\\
//    @Transactional(readOnly = true)
//    public List<FaqEntrySummaryDTO> getCollectorFaq() {
//        return faqEntryRepository.findAll().stream()
//                .filter(entry -> entry.getRoleType() == FaqEntry.RoleType.COLLECTOR)
//                .filter(entry -> entry.getStatus() == FaqEntry.Status.ACTIVE)
//                .sorted(Comparator
//                        .comparing(FaqEntry::getCategoryLabel, Comparator.nullsLast(String::compareToIgnoreCase))
//                        .thenComparing(FaqEntry::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
//                        .thenComparing(FaqEntry::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
//                )
//                .map(faqEntryMapper::toSummaryDto)
//                .toList();
//    }

    public List<FaqEntrySummaryDTO> getCollectorFaq() {
        return faqEntryRepository.findAll().stream()
                .filter(entry -> entry.getRoleType() == FaqEntry.RoleType.COLLECTOR
                        || entry.getRoleType() == FaqEntry.RoleType.ALL)  // ✅ AJOUTER ALL
                .filter(entry -> entry.getStatus() == FaqEntry.Status.ACTIVE)
                .sorted(Comparator
                        .comparing(FaqEntry::getCategoryLabel, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(FaqEntry::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(FaqEntry::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .map(faqEntryMapper::toSummaryDto)
                .toList();
    }


    //. -------------------- GET FAQ DETAIL -------------------- .\\
    @Transactional(readOnly = true)
    public FaqEntryResponseDTO getCollectorFaqById(Long faqId) {
        if (faqId == null) {
            throw new BadRequestException("L'identifiant de la FAQ est obligatoire.");
        }

        FaqEntry entry = faqEntryRepository.findById(faqId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrée FAQ introuvable."));

//        if (entry.getRoleType() != FaqEntry.RoleType.COLLECTOR) {
//            throw new ResourceNotFoundException("Entrée FAQ introuvable pour le collecteur.");
//        }
        if (entry.getRoleType() != FaqEntry.RoleType.COLLECTOR
                && entry.getRoleType() != FaqEntry.RoleType.ALL) {
            throw new ResourceNotFoundException("Entrée FAQ introuvable pour le collecteur.");
        }

        if (entry.getStatus() != FaqEntry.Status.ACTIVE) {
            throw new ResourceNotFoundException("Cette entrée FAQ n'est pas disponible.");
        }

        return faqEntryMapper.toDto(entry);
    }
}