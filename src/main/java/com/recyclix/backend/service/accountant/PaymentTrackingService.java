package com.recyclix.backend.service.accountant;

import com.recyclix.backend.model.FactoryInvoice;
import com.recyclix.backend.repository.FactoryInvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTrackingService {

    private final FactoryInvoiceRepository factoryInvoiceRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void autoCheckOverdueInvoices() {
        LocalDate today = LocalDate.now();

        List<FactoryInvoice> lateInvoices = factoryInvoiceRepository
                .findByStatusAndDueDateBefore(FactoryInvoice.InvoiceStatus.PENDING, today);

        if (lateInvoices.isEmpty()) return;

        for (FactoryInvoice invoice : lateInvoices) {
            invoice.setStatus(FactoryInvoice.InvoiceStatus.OVERDUE);
        }

        factoryInvoiceRepository.saveAll(lateInvoices);
        log.warn("{} facture(s) passées en OVERDUE.", lateInvoices.size());
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpectedRevenue() {
        // ✅ CORRIGÉ : utilisation de amountTtc
        BigDecimal total = factoryInvoiceRepository.sumAmountTtcByStatuses(
                List.of(FactoryInvoice.InvoiceStatus.PENDING, FactoryInvoice.InvoiceStatus.OVERDUE)
        );
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<FactoryInvoice> getUrgentOverdueInvoices() {
        return factoryInvoiceRepository
                .findByStatus(FactoryInvoice.InvoiceStatus.OVERDUE)
                .stream()
                .sorted(Comparator.comparing(FactoryInvoice::getDueDate))
                .toList();
    }
}