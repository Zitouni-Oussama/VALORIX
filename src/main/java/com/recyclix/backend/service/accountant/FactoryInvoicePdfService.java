package com.recyclix.backend.service.accountant;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.model.FactoryInvoice;
import com.recyclix.backend.repository.FactoryInvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactoryInvoicePdfService {

    private final FactoryInvoiceRepository factoryInvoiceRepository;
    private final ResourceLoader resourceLoader;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("fr", "DZ"));
    private static final Color COLOR_BLACK = new DeviceGray(0);
    private static final Color COLOR_GRAY = new DeviceGray(0.5f);
    private static final Color COLOR_LIGHT_GRAY = new DeviceGray(0.85f);

    private static final float LOGO_WIDTH = 100;
    private static final float LOGO_HEIGHT = 100;

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> generateFactoryInvoicePdf(Long invoiceId) {
        log.info("Génération du PDF pour la facture usine ID : {}", invoiceId);

        FactoryInvoice invoice = factoryInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Facture usine introuvable avec ID : " + invoiceId));

        byte[] pdfBytes = generateInvoicePdf(invoice);
        String filename = "facture_usine_" + invoice.getId() + "_" + LocalDate.now() + ".pdf";

        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Version corrigée : prend amountHt au lieu de amount (l'ancien champ)
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> previewFactoryInvoicePdf(
            String factoryName,
            BigDecimal amountHt,      // Changé : amount -> amountHt
            BigDecimal tvaRate,       // Nouveau paramètre
            LocalDate dueDate,
            FactoryInvoice.InvoiceStatus status
    ) {
        log.info("Génération du PDF preview pour facture usine : {}", factoryName);

        FactoryInvoice previewInvoice = FactoryInvoice.builder()
                .id(null)
                .factoryName(factoryName)
                .amountHt(amountHt != null ? amountHt : BigDecimal.ZERO)  // Changé: .amount() -> .amountHt()
                .tvaRate(tvaRate != null ? tvaRate : BigDecimal.ZERO)     // Nouveau champ
                .issueDate(LocalDate.now())
                .dueDate(dueDate)
                .status(status != null ? status : FactoryInvoice.InvoiceStatus.DRAFT)
                .build();

        // Forcer le calcul des montants TVA/TTC
        previewInvoice.calculateAmounts();

        byte[] pdfBytes = generateInvoicePdf(previewInvoice);
        String filename = "facture_usine_preview_" + factoryName.replaceAll("\\s+", "_") + "_" + LocalDate.now() + ".pdf";

        return buildPdfResponse(pdfBytes, filename);
    }

    private byte[] generateInvoicePdf(FactoryInvoice invoice) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(30, 50, 50, 50);

            PdfFont boldFont = getBoldFont();
            PdfFont normalFont = getNormalFont();

            Table headerTable = createHeaderTable(invoice, boldFont, normalFont);
            document.add(headerTable);

            Paragraph title = new Paragraph("FACTURE")
                    .setFont(boldFont)
                    .setFontSize(24)
                    .setFontColor(COLOR_BLACK)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20)
                    .setMarginBottom(20);
            document.add(title);

            Table infoTable = createInfoTable(invoice, boldFont, normalFont);
            document.add(infoTable);

            Table amountTable = createAmountTable(invoice, boldFont, normalFont);
            document.add(amountTable);

            Paragraph statusParagraph = createStatusParagraph(invoice, boldFont, normalFont);
            document.add(statusParagraph);

            Paragraph termsParagraph = createTermsParagraph(normalFont);
            document.add(termsParagraph);

            Paragraph footer = createFooter(normalFont);
            document.add(footer);

            document.close();
            pdfDoc.close();

            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Erreur lors de la génération du PDF", e);
            throw new BadRequestException("Erreur lors de la génération du PDF : " + e.getMessage());
        }
    }

    private Table createHeaderTable(FactoryInvoice invoice, PdfFont boldFont, PdfFont normalFont) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{30f, 70f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);

        Cell logoCell = new Cell();
        Image logo = loadLogo();
        if (logo != null) {
            logo.setWidth(LOGO_WIDTH);
            logo.setHeight(LOGO_HEIGHT);
            logoCell.add(logo);
        } else {
            Paragraph fallbackLogo = new Paragraph("RECYCLIX")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setFontColor(COLOR_BLACK);
            logoCell.add(fallbackLogo);
            Paragraph fallbackSub = new Paragraph("recyclix.dz")
                    .setFont(normalFont)
                    .setFontSize(9)
                    .setFontColor(COLOR_GRAY);
            logoCell.add(fallbackSub);
        }
        logoCell.setBorder(null);
        logoCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);

        Cell rightCell = new Cell();
        String statusText = getStatusText(invoice.getStatus());
        String formattedInvoiceNumber = getFormattedInvoiceNumber(invoice);

        Paragraph invoiceInfo = new Paragraph()
                .setFont(normalFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);

        invoiceInfo.add(new Paragraph("FACTURE N°: " + formattedInvoiceNumber).setFont(boldFont).setFontSize(12));
        invoiceInfo.add(new Paragraph("Date d'émission: " + formatDate(invoice.getIssueDate())));
        invoiceInfo.add(new Paragraph("Client: " + invoice.getFactoryName()));
        invoiceInfo.add(new Paragraph("Statut: " + statusText));

        rightCell.add(invoiceInfo);
        rightCell.setBorder(null);
        rightCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.BOTTOM);

        headerTable.addCell(logoCell);
        headerTable.addCell(rightCell);

        return headerTable;
    }

    private Image loadLogo() {
        try {
            InputStream logoStream = resourceLoader.getResource("classpath:static/images/logo.png").getInputStream();
            if (logoStream != null) {
                byte[] logoBytes = logoStream.readAllBytes();
                return new Image(ImageDataFactory.create(logoBytes));
            }
        } catch (Exception e) {
            log.warn("Logo non trouvé, utilisation du texte par défaut");
        }
        return null;
    }

    /**
     * Corrigé : utilise amountHt au lieu de getAmount()
     */
    private String getFormattedInvoiceNumber(FactoryInvoice invoice) {
        LocalDate issueDate = invoice.getIssueDate() != null ? invoice.getIssueDate() : LocalDate.now();

        String year = issueDate.format(YEAR_FORMATTER);
        String month = issueDate.format(MONTH_FORMATTER);
        String day = issueDate.format(DAY_FORMATTER);

        String sequentialNumber;

        if (invoice.getId() == null) {
            sequentialNumber = String.format("%08d", System.currentTimeMillis() % 100000000);
        } else {
            sequentialNumber = String.format("%08d", invoice.getId());
        }

        return String.format("FACT-%s/%s/%s-%s", year, month, day, sequentialNumber);
    }

    private Table createInfoTable(FactoryInvoice invoice, PdfFont boldFont, PdfFont normalFont) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30f, 70f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(15)
                .setMarginBottom(15)
                .setBorder(new SolidBorder(COLOR_LIGHT_GRAY, 1));

        infoTable.addCell(createLabelCell("Désignation", boldFont));
        infoTable.addCell(createValueCell("Collecte et recyclage de déchets", normalFont));

        infoTable.addCell(createLabelCell("Période", boldFont));
        infoTable.addCell(createValueCell("Collectes du mois", normalFont));

        infoTable.addCell(createLabelCell("Date d'échéance", boldFont));
        String dueDateText = invoice.getDueDate() != null ? formatDate(invoice.getDueDate()) : "Non spécifiée";
        infoTable.addCell(createValueCell(dueDateText, normalFont));

        if (invoice.getFactoryAddress() != null && !invoice.getFactoryAddress().isBlank()) {
            infoTable.addCell(createLabelCell("Adresse usine", boldFont));
            infoTable.addCell(createValueCell(invoice.getFactoryAddress(), normalFont));
        }

        if (invoice.getFactoryTaxId() != null && !invoice.getFactoryTaxId().isBlank()) {
            infoTable.addCell(createLabelCell("NIF", boldFont));
            infoTable.addCell(createValueCell(invoice.getFactoryTaxId(), normalFont));
        }

        return infoTable;
    }

    /**
     * Corrigé : utilise amountHt, tvaRate, tvaAmount, amountTtc
     */
    private Table createAmountTable(FactoryInvoice invoice, PdfFont boldFont, PdfFont normalFont) {
        Table amountTable = new Table(UnitValue.createPercentArray(new float[]{50f, 50f}))
                .setWidth(UnitValue.createPercentValue(60))
                .setMarginTop(20)
                .setMarginBottom(20)
                .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);

        // Montant HT
        amountTable.addCell(createLabelCell("Montant HT", boldFont));
        amountTable.addCell(createValueCell(formatMoney(invoice.getAmountHt()), normalFont));

        // TVA (taux dynamique)
        BigDecimal tvaRate = invoice.getTvaRate() != null ? invoice.getTvaRate() : BigDecimal.ZERO;
        String tvaLabel = String.format("TVA (%.0f%%)", tvaRate.multiply(new BigDecimal("100")).doubleValue());
        amountTable.addCell(createLabelCell(tvaLabel, boldFont));
        amountTable.addCell(createValueCell(formatMoney(invoice.getTvaAmount()), normalFont));

        // Total TTC
        amountTable.addCell(createLabelCell("TOTAL TTC", boldFont));
        amountTable.addCell(createValueCell(formatMoney(invoice.getAmountTtc()), boldFont));

        // Poids total (optionnel)
        if (invoice.getTotalWeightKg() != null && invoice.getTotalWeightKg().compareTo(BigDecimal.ZERO) > 0) {
            amountTable.addCell(createLabelCell("Poids total", boldFont));
            amountTable.addCell(createValueCell(invoice.getTotalWeightKg() + " kg", normalFont));
        }

        return amountTable;
    }

    private Paragraph createStatusParagraph(FactoryInvoice invoice, PdfFont boldFont, PdfFont normalFont) {
        Paragraph statusPara = new Paragraph()
                .setMarginTop(20)
                .setMarginBottom(15)
                .setPadding(10)
                .setBorder(new SolidBorder(getStatusColor(invoice.getStatus()), 2));

        String statusMessage = getStatusMessage(invoice.getStatus(), invoice.getDueDate());
        statusPara.add(new Paragraph(statusMessage)
                .setFont(boldFont)
                .setFontSize(12)
                .setFontColor(COLOR_BLACK)
                .setTextAlignment(TextAlignment.CENTER));

        if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(LocalDate.now())
                && invoice.getStatus() != FactoryInvoice.InvoiceStatus.PAID
                && invoice.getStatus() != FactoryInvoice.InvoiceStatus.CANCELLED) {
            statusPara.add(new Paragraph("⚠️ ATTENTION : Cette facture est en retard de paiement.")
                    .setFont(normalFont)
                    .setFontSize(10)
                    .setFontColor(COLOR_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        return statusPara;
    }

    private Paragraph createTermsParagraph(PdfFont normalFont) {
        Paragraph terms = new Paragraph()
                .setMarginTop(30)
                .add(new Paragraph("Conditions de paiement :")
                        .setFont(normalFont)
                        .setFontSize(10)
                        .setFontColor(COLOR_BLACK));

        terms.add(new Paragraph("Paiement par virement bancaire dans un délai de 30 jours.")
                .setFont(normalFont)
                .setFontSize(9)
                .setFontColor(COLOR_GRAY));

        terms.add(new Paragraph("IBAN: DZ 1234 5678 9012 3456 7890")
                .setFont(normalFont)
                .setFontSize(9)
                .setFontColor(COLOR_GRAY));

        return terms;
    }

    private Paragraph createFooter(PdfFont normalFont) {
        return new Paragraph()
                .setMarginTop(40)
                .add(new Paragraph("Document généré automatiquement par Recyclix")
                        .setFont(normalFont)
                        .setFontSize(8)
                        .setFontColor(COLOR_GRAY)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("Merci de contribuer à une économie circulaire ♻️")
                        .setFont(normalFont)
                        .setFontSize(8)
                        .setFontColor(COLOR_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
    }

    private Cell createLabelCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(COLOR_BLACK))
                .setBorder(null)
                .setPadding(5);
    }

    private Cell createValueCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(COLOR_BLACK))
                .setBorder(null)
                .setPadding(5)
                .setTextAlignment(TextAlignment.RIGHT);
    }

    private PdfFont getBoldFont() {
        try {
            return PdfFontFactory.createFont("Helvetica-Bold");
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger la police", e);
        }
    }

    private PdfFont getNormalFont() {
        try {
            return PdfFontFactory.createFont("Helvetica");
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger la police", e);
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0,00 DA";
        CURRENCY_FORMAT.setMinimumFractionDigits(2);
        CURRENCY_FORMAT.setMaximumFractionDigits(2);
        return CURRENCY_FORMAT.format(amount);
    }

    private String formatDate(LocalDate date) {
        return date != null ? DATE_FORMATTER.format(date) : "N/A";
    }

    private String getStatusText(FactoryInvoice.InvoiceStatus status) {
        if (status == null) return "BROUILLON";
        return switch (status) {
            case DRAFT -> "BROUILLON";
            case PENDING -> "EN ATTENTE";
            case PAID -> "PAYÉE";
            case OVERDUE -> "EN RETARD";
            case CANCELLED -> "ANNULÉE";
            case REFUNDED -> "REMBOURSÉE";
        };
    }

    private DeviceGray getStatusColor(FactoryInvoice.InvoiceStatus status) {
        if (status == null) return new DeviceGray(0.5f);
        return switch (status) {
            case PAID -> new DeviceGray(0.2f);
            case OVERDUE -> new DeviceGray(0.3f);
            default -> new DeviceGray(0.5f);
        };
    }

    private String getStatusMessage(FactoryInvoice.InvoiceStatus status, LocalDate dueDate) {
        return switch (status) {
            case DRAFT -> "📝 FACTURE EN BROUILLON";
            case PENDING -> "📄 FACTURE EN ATTENTE DE PAIEMENT";
            case PAID -> "✅ FACTURE PAYÉE - Merci pour votre règlement";
            case OVERDUE -> "⚠️ FACTURE EN RETARD - Merci de procéder au paiement";
            case CANCELLED -> "❌ FACTURE ANNULÉE";
            case REFUNDED -> "🔄 FACTURE REMBOURSÉE";
            default -> "FACTURE";
        };
    }

    private ResponseEntity<Resource> buildPdfResponse(byte[] pdfBytes, String filename) {
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
    }
}