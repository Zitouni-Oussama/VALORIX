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
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.recyclix.backend.dto.financial_report.FinancialReportResponseDTO;
import com.recyclix.backend.exception.BadRequestException;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.mapper.FinancialReportMapper;
import com.recyclix.backend.model.FinancialReport;
import com.recyclix.backend.repository.FinancialReportRepository;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialReportPdfService {

    private final FinancialReportRepository financialReportRepository;
    private final FinancialReportMapper financialReportMapper;
    private final FinancialReportService financialReportService;
    private final ResourceLoader resourceLoader;

    // =========================================================
    // CONSTANTES
    // =========================================================
    private static final Color COLOR_BLACK = new DeviceGray(0);
    private static final Color COLOR_GRAY = new DeviceGray(0.5f);
    private static final Color COLOR_LIGHT_GRAY = new DeviceGray(0.85f);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // 🔥 NOUVEAU : Formateur pour le numéro de rapport (ex: FACT-26/05/01-00000024)
    private static final DateTimeFormatter REPORT_YEAR_FORMATTER = DateTimeFormatter.ofPattern("yy");
    private static final DateTimeFormatter REPORT_MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter REPORT_DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");

    // 🔥 Compteur séquentiel pour les numéros de rapport
    // ⚠️ À remplacer par une séquence BDD en production
    private static final AtomicLong REPORT_SEQUENCE = new AtomicLong(1);

    // =========================================================
    // POLICES
    // =========================================================
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

    // =========================================================
    // GÉNÉRATION DU NUMÉRO DE RAPPORT UNIQUE
    // Format: FACT-YY/MM/DD-XXXXXX
    // Exemple: FACT-26/05/01-00000024
    // =========================================================
    private String generateReportNumber() {
        LocalDate now = LocalDate.now();

        String year = now.format(REPORT_YEAR_FORMATTER);   // 26
        String month = now.format(REPORT_MONTH_FORMATTER); // 05
        String day = now.format(REPORT_DAY_FORMATTER);     // 01

        // Récupérer la prochaine valeur séquentielle
        // TODO: En production, remplacer AtomicLong par une séquence BDD
        long sequence = REPORT_SEQUENCE.getAndIncrement();

        // Formater avec 6 chiffres (padding)
        String sequentialPart = String.format("%06d", sequence);

        return String.format("RAP-%s/%s/%s-%s", year, month, day, sequentialPart);
    }

    // =========================================================
    // MÉTHODES PUBLIQUES
    // =========================================================

    /**
     * Génère un PDF à partir d'un FinancialReport existant
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> generatePdfFromExistingReport(Long reportId) {
        FinancialReport fullReport = financialReportRepository.findWithGeneratorById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Rapport introuvable avec ID : " + reportId));

        FinancialReportResponseDTO dto = financialReportMapper.toResponseDTO(fullReport);

        // 🔥 Utiliser l'ID du rapport comme base pour le numéro, ou générer un nouveau
        String reportNumber = generateReportNumberFromId(reportId);

        byte[] pdfBytes = generateSimplePdf(dto, reportNumber);

        String filename = "rapport_financier_" + reportNumber + "_" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Génère un PDF à partir d'un rapport en preview
     */
    public ResponseEntity<Resource> generatePreviewPdf(LocalDate startDate, LocalDate endDate) {
        FinancialReportResponseDTO dto = financialReportService.previewReport(startDate, endDate);

        // 🔥 Pour la preview, générer un numéro temporaire avec préfixe PREVIEW
        String reportNumber = generatePreviewReportNumber();

        byte[] pdfBytes = generateSimplePdf(dto, reportNumber);

        String filename = "rapport_financier_preview_" + startDate + "_au_" + endDate + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Génère et sauvegarde un rapport, puis retourne le PDF
     */
    public ResponseEntity<Resource> generateAndDownloadReport(
            FinancialReport.ReportType reportType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        com.recyclix.backend.dto.financial_report.FinancialReportRequestDTO request =
                new com.recyclix.backend.dto.financial_report.FinancialReportRequestDTO();
        request.setReportType(reportType);
        request.setPeriodStart(startDate);
        request.setPeriodEnd(endDate);

        FinancialReportResponseDTO dto = financialReportService.generateAndSaveReport(request);

        // 🔥 Générer un numéro de rapport unique
        String reportNumber = generateReportNumber();

        byte[] pdfBytes = generateSimplePdf(dto, reportNumber);

        String filename = "rapport_financier_" + reportNumber + "_" + startDate + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    // =========================================================
    // MÉTHODES PRIVÉES DE GÉNÉRATION DE NUMÉROS
    // =========================================================

    /**
     * Génère un numéro de rapport à partir de l'ID existant
     * Exemple: FACT-26/05/01-00000024 (avec ID = 24)
     */
    private String generateReportNumberFromId(Long reportId) {
        if (reportId == null) {
            return generateReportNumber();
        }

        LocalDate now = LocalDate.now();
        String year = now.format(REPORT_YEAR_FORMATTER);
        String month = now.format(REPORT_MONTH_FORMATTER);
        String day = now.format(REPORT_DAY_FORMATTER);

        // Utiliser l'ID comme numéro séquentiel (avec padding 6 chiffres)
        String sequentialPart = String.format("%06d", reportId);

        return String.format("RAP-%s/%s/%s-%s", year, month, day, sequentialPart);
    }

    /**
     * Génère un numéro de rapport pour la preview
     * Exemple: PREVIEW-26/05/01-00000000
     */
    private String generatePreviewReportNumber() {
        LocalDate now = LocalDate.now();
        String year = now.format(REPORT_YEAR_FORMATTER);
        String month = now.format(REPORT_MONTH_FORMATTER);
        String day = now.format(REPORT_DAY_FORMATTER);

        return String.format("PREVIEW-%s/%s/%s-%s", year, month, day, "000000");
    }

    // =========================================================
    // GÉNÉRATION PDF
    // =========================================================

    private byte[] generateSimplePdf(FinancialReportResponseDTO dto, String reportNumber) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(30, 50, 50, 50);

            PdfFont boldFont = getBoldFont();
            PdfFont normalFont = getNormalFont();

            // =========================================================
            // 1. EN-TÊTE AVEC LOGO
            // =========================================================
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{30f, 70f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(15);

            // Logo
            Cell logoCell = new Cell();
            try {
                InputStream logoStream = resourceLoader.getResource("classpath:static/images/logo.png").getInputStream();
                if (logoStream != null) {
                    byte[] logoBytes = logoStream.readAllBytes();
                    Image logo = new Image(ImageDataFactory.create(logoBytes));
                    logo.setWidth(100);
                    logo.setHeight(100);
                    logoCell.add(logo);
                } else {
                    logoCell.add(new Paragraph("RECYCLIX").setFont(boldFont).setFontSize(18).setFontColor(COLOR_BLACK));
                }
            } catch (Exception e) {
                log.warn("Logo non trouvé, utilisation du texte par défaut");
                logoCell.add(new Paragraph("RECYCLIX").setFont(boldFont).setFontSize(18).setFontColor(COLOR_BLACK));
            }
            logoCell.setBorder(null);
            logoCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);

            // Titre du rapport
            Cell titleCell = new Cell();
            String reportTypeName = dto.getReportType() != null ? dto.getReportType().name() : "PERSONNALISÉ";

            Paragraph title = new Paragraph("RAPPORT FINANCIER")
                    .setFont(boldFont)
                    .setFontSize(22)
                    .setFontColor(COLOR_BLACK)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(5);

            Paragraph subtitle = new Paragraph(
                    reportTypeName +
                            " - Du " + (dto.getPeriodStart() != null ? DATE_FORMATTER.format(dto.getPeriodStart()) : "N/A") +
                            " au " + (dto.getPeriodEnd() != null ? DATE_FORMATTER.format(dto.getPeriodEnd()) : "N/A")
            )
                    .setFont(normalFont)
                    .setFontSize(10)
                    .setFontColor(COLOR_GRAY)
                    .setTextAlignment(TextAlignment.RIGHT);

            titleCell.add(title);
            titleCell.add(subtitle);
            titleCell.setBorder(null);
            titleCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.BOTTOM);

            headerTable.addCell(logoCell);
            headerTable.addCell(titleCell);
            document.add(headerTable);

            // =========================================================
            // 2. NUMÉRO DE RAPPORT (format FACT-26/05/01-00000024)
            // =========================================================
            Table numberTable = new Table(UnitValue.createPercentArray(new float[]{100f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(15)
                    .setMarginBottom(15);

            Paragraph numberParagraph = new Paragraph()
                    .setTextAlignment(TextAlignment.CENTER);

            numberParagraph.add(new Text("N° RAPPORT : ").setFont(boldFont).setFontSize(11).setFontColor(COLOR_BLACK));
            numberParagraph.add(new Text(reportNumber).setFont(boldFont).setFontSize(14).setFontColor(COLOR_BLACK));

            Cell numberCell = new Cell().add(numberParagraph);
            numberCell.setBorder(new SolidBorder(COLOR_LIGHT_GRAY, 1));
            numberCell.setPadding(8);
            numberCell.setTextAlignment(TextAlignment.CENTER);

            numberTable.addCell(numberCell);
            document.add(numberTable);

            // =========================================================
            // 3. INFORMATIONS DE GÉNÉRATION
            // =========================================================
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30f, 70f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(15)
                    .setMarginBottom(25)
                    .setBorder(new SolidBorder(COLOR_LIGHT_GRAY, 1));

            infoTable.addCell(createLabelCell("Date de génération :", boldFont));
            infoTable.addCell(createValueCell(
                    dto.getGeneratedAt() != null ? DATETIME_FORMATTER.format(dto.getGeneratedAt()) : "N/A",
                    normalFont
            ));

            infoTable.addCell(createLabelCell("Généré par :", boldFont));
            infoTable.addCell(createValueCell(
                    dto.getGeneratedByFullName() != null ? dto.getGeneratedByFullName() : "Système",
                    normalFont
            ));

            document.add(infoTable);

            // =========================================================
            // 4. RÉSUMÉ FINANCIER (3 CARTES)
            // =========================================================
            Paragraph summaryTitle = new Paragraph("RÉSUMÉ FINANCIER")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setFontColor(COLOR_BLACK)
                    .setMarginTop(10)
                    .setMarginBottom(15);
            document.add(summaryTitle);

            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{33.3f, 33.3f, 33.3f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(25);

            summaryTable.addCell(createCardCell("REVENUS", formatMoney(dto.getTotalIncome()), boldFont, normalFont));
            summaryTable.addCell(createCardCell("DÉPENSES", formatMoney(dto.getTotalExpense()), boldFont, normalFont));
            summaryTable.addCell(createCardCell("BÉNÉFICE NET", formatMoney(dto.getNetProfit()), boldFont, normalFont));

            document.add(summaryTable);

            // =========================================================
            // 5. DÉTAILS
            // =========================================================
            Paragraph detailsTitle = new Paragraph("DÉTAILS")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setFontColor(COLOR_BLACK)
                    .setMarginTop(10)
                    .setMarginBottom(15);
            document.add(detailsTitle);

            Table detailTable = new Table(UnitValue.createPercentArray(new float[]{40f, 60f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);

            detailTable.addCell(createDetailLabelCell("Revenus des usines :", normalFont));
            detailTable.addCell(createDetailValueCell(formatMoney(dto.getTotalIncome()), normalFont));

            detailTable.addCell(createDetailLabelCell("Dépenses totales :", normalFont));
            detailTable.addCell(createDetailValueCell(formatMoney(dto.getTotalExpense()), normalFont));

            detailTable.addCell(createDetailLabelCell("Profit net :", normalFont));
            detailTable.addCell(createDetailValueCell(formatMoney(dto.getNetProfit()), normalFont, true));

            document.add(detailTable);

            // =========================================================
            // 6. PIED DE PAGE
            // =========================================================
            document.add(new Paragraph(" ").setMarginTop(175));

            Table footerTable = new Table(UnitValue.createPercentArray(new float[]{100f}))
                    .setWidth(UnitValue.createPercentValue(100));

            Paragraph footer = new Paragraph("Document généré automatiquement par Recyclix")
                    .setFont(normalFont)
                    .setFontSize(8)
                    .setFontColor(COLOR_GRAY)
                    .setTextAlignment(TextAlignment.CENTER);

            footerTable.addCell(new Cell().add(footer).setBorder(null).setPadding(5));
            document.add(footerTable);

            document.close();
            pdfDoc.close();

            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Erreur lors de la génération du PDF", e);
            throw new BadRequestException("Erreur lors de la génération du PDF : " + e.getMessage());
        }
    }

    // =========================================================
    // MÉTHODES DE MISE EN FORME
    // =========================================================

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
                .setPadding(5);
    }

    private Cell createCardCell(String label, String value, PdfFont boldFont, PdfFont normalFont) {
        Cell cell = new Cell();
        cell.add(new Paragraph(label).setFont(normalFont).setFontSize(9).setFontColor(COLOR_GRAY));
        cell.add(new Paragraph(value).setFont(boldFont).setFontSize(16).setFontColor(COLOR_BLACK));
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setPadding(12);
        cell.setBorder(new SolidBorder(COLOR_LIGHT_GRAY, 1));
        return cell;
    }

    private Cell createDetailLabelCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(COLOR_BLACK))
                .setBorder(null)
                .setPadding(5);
    }

    private Cell createDetailValueCell(String text, PdfFont font) {
        return createDetailValueCell(text, font, false);
    }

    private Cell createDetailValueCell(String text, PdfFont font, boolean bold) {
        Paragraph p = new Paragraph(text)
                .setFont(font)
                .setFontSize(bold ? 11 : 10);
        if (bold) {
            p.setFontColor(COLOR_BLACK);
        }
        return new Cell().add(p).setBorder(null).setPadding(5);
    }

    // =========================================================
    // MÉTHODES UTILITAIRES
    // =========================================================

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0,00 DA";
        NumberFormat formatter = NumberFormat.getInstance(new Locale("fr", "DZ"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(amount) + " DA";
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