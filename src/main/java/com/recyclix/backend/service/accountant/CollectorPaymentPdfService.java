package com.recyclix.backend.service.accountant;

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
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.recyclix.backend.model.Collector;
import com.recyclix.backend.model.FactoryValidation;
import com.recyclix.backend.model.Payment;
import com.recyclix.backend.model.FactoryUser;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
public class CollectorPaymentPdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("fr", "DZ"));
    private static final Color COLOR_BLACK = new DeviceGray(0);
    private static final Color COLOR_GRAY = new DeviceGray(0.5f);
    private static final Color COLOR_LIGHT_GRAY = new DeviceGray(0.85f);

    public byte[] generateReceipt(Collector collector, List<FactoryValidation> validations,
                                  Payment payment, FactoryUser payingUser) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(30, 50, 50, 50);

            PdfFont boldFont = getBoldFont();
            PdfFont normalFont = getNormalFont();

            // Titre
            document.add(new Paragraph("TICKET DE PAIEMENT COLLECTEUR")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Informations générales
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30f, 70f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(15)
                    .setBorder(new SolidBorder(COLOR_LIGHT_GRAY, 1));

            infoTable.addCell(createLabelCell("N° Paiement:", boldFont));
            infoTable.addCell(createValueCell(payment.getId().toString(), normalFont));
            infoTable.addCell(createLabelCell("Date:", boldFont));
            infoTable.addCell(createValueCell(payment.getPaymentDate().format(DATE_FORMATTER), normalFont));
            infoTable.addCell(createLabelCell("Collecteur:", boldFont));
            infoTable.addCell(createValueCell(collector.getFirstName() + " " + collector.getLastName(), normalFont));
            infoTable.addCell(createLabelCell("Email:", boldFont));
            infoTable.addCell(createValueCell(collector.getAccount().getEmail(), normalFont));
            infoTable.addCell(createLabelCell("Téléphone:", boldFont));
            infoTable.addCell(createValueCell(collector.getAccount().getPhone() != null ? collector.getAccount().getPhone() : "-", normalFont));
            infoTable.addCell(createLabelCell("Méthode de paiement:", boldFont));
            infoTable.addCell(createValueCell(payment.getPaymentMethod().name(), normalFont));
            if (payingUser != null) {
                infoTable.addCell(createLabelCell("Payé par:", boldFont));
                infoTable.addCell(createValueCell(payingUser.getFirstName() + " " + payingUser.getLastName(), normalFont));
            }
            document.add(infoTable);

            // Tableau des livraisons
            Paragraph tableTitle = new Paragraph("Détail des livraisons payées")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setMarginTop(10)
                    .setMarginBottom(10);
            document.add(tableTitle);

            Table detailTable = new Table(UnitValue.createPercentArray(new float[]{15f, 25f, 25f, 35f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(new SolidBorder(COLOR_LIGHT_GRAY, 1));

            detailTable.addCell(createHeaderCell("Date validation", boldFont));
            detailTable.addCell(createHeaderCell("Matériau", boldFont));
            detailTable.addCell(createHeaderCell("Poids validé (kg)", boldFont));
            detailTable.addCell(createHeaderCell("Montant (DA)", boldFont));

            BigDecimal total = BigDecimal.ZERO;
            for (FactoryValidation v : validations) {
                String materialName = "";
                if (v.getDelivery() != null && v.getDelivery().getCollection() != null
                        && v.getDelivery().getCollection().getRequest() != null
                        && v.getDelivery().getCollection().getRequest().getMaterial() != null) {
                    materialName = v.getDelivery().getCollection().getRequest().getMaterial().getName();
                }
                detailTable.addCell(createDataCell(v.getValidatedAt().format(DATE_FORMATTER), normalFont));
                detailTable.addCell(createDataCell(materialName, normalFont));
                detailTable.addCell(createDataCell(v.getValidatedWeight().toString(), normalFont));
                detailTable.addCell(createDataCell(formatMoney(v.getCollectorAmount()), normalFont));
                total = total.add(v.getCollectorAmount());
            }

            // Ligne total
            detailTable.addCell(new Cell(1, 3).add(new Paragraph("TOTAL")
                    .setFont(boldFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(new SolidBorder(COLOR_LIGHT_GRAY, 1)));
            detailTable.addCell(createDataCell(formatMoney(total), boldFont));

            document.add(detailTable);

            // Zone de signature
            Paragraph signatureLine = new Paragraph()
                    .setMarginTop(40)
                    .add(new Paragraph("Signature du collecteur : _________________________")
                            .setFont(normalFont)
                            .setFontSize(10)
                            .setFontColor(COLOR_GRAY));
            document.add(signatureLine);

            // Pied de page
            Paragraph footer = new Paragraph()
                    .setMarginTop(50)
                    .add(new Paragraph("Document généré automatiquement par Recyclix - VALORIX")
                            .setFont(normalFont)
                            .setFontSize(8)
                            .setFontColor(COLOR_GRAY)
                            .setTextAlignment(TextAlignment.CENTER));
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Erreur lors de la génération du PDF", e);
            throw new RuntimeException("Impossible de générer le ticket de paiement", e);
        }
    }

    private Cell createLabelCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(COLOR_BLACK))
                .setBorder(null).setPadding(5);
    }

    private Cell createValueCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(COLOR_BLACK))
                .setBorder(null).setPadding(5);
    }

    private Cell createHeaderCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(COLOR_BLACK))
                .setBackgroundColor(COLOR_LIGHT_GRAY).setPadding(5);
    }

    private Cell createDataCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(COLOR_BLACK))
                .setPadding(5);
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
}