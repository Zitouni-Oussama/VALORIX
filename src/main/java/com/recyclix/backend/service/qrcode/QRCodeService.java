// recyclix/backend/service/qrcode/QRCodeService.java

package com.recyclix.backend.service.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.recyclix.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class QRCodeService {

    private static final int QR_CODE_WIDTH = 400;
    private static final int QR_CODE_HEIGHT = 400;
    private static final int LOGO_SIZE = 80;
    private static final int LOGO_CORNER_RADIUS = 15;

    // 🎨 Couleurs Recyclix
    private static final Color COLOR_RECYCLIX_GREEN = new Color(76, 175, 80);
    private static final Color COLOR_RECYCLIX_DARK_GREEN = new Color(56, 142, 60);
    private static final Color COLOR_RECYCLIX_BLUE = new Color(33, 150, 243);
    private static final Color COLOR_WHITE = new Color(255, 255, 255, 240);

    @Value("${app.qrcode.base-url:http://localhost:3000}")
    private String baseUrl;

    private final ResourceLoader resourceLoader;

    public byte[] generateQRCodeWithLogo(String content) {
        if (content == null || content.isBlank()) {
            throw new BadRequestException("Le contenu du QR code ne peut pas être vide.");
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_WIDTH,
                    QR_CODE_HEIGHT,
                    java.util.Map.of(
                            com.google.zxing.EncodeHintType.ERROR_CORRECTION,
                            ErrorCorrectionLevel.H
                    )
            );

            MatrixToImageConfig config = new MatrixToImageConfig(0xFF000000, 0xFFFFFFFF);
            BufferedImage qrImage = convertToARGB(
        MatrixToImageWriter.toBufferedImage(bitMatrix, config)
);

            BufferedImage finalImage = addColoredLogoToQRCode(qrImage);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(finalImage, "PNG", baos);
            return baos.toByteArray();

        } catch (WriterException | IOException e) {
            log.error("Erreur lors de la génération du QR code", e);
            throw new BadRequestException("Impossible de générer le QR code : " + e.getMessage());
        }
    }

    /**
     * 🎨 Version CORRIGÉE : Force les couleurs du logo
     */
    private BufferedImage addColoredLogoToQRCode(BufferedImage qrImage) {
        Graphics2D g = qrImage.createGraphics();

        try {
            // Configuration haute qualité
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);

            // Charger le logo avec une méthode qui force les couleurs
            BufferedImage logo = loadLogoWithForcedColors();

            int logoX = (qrImage.getWidth() - LOGO_SIZE) / 2;
            int logoY = (qrImage.getHeight() - LOGO_SIZE) / 2;

            if (logo != null) {
                // Redimensionner le logo
                BufferedImage resizedLogo = resizeImageWithHighQuality(logo, LOGO_SIZE, LOGO_SIZE);

                // Fond blanc avec ombre légère
                g.setColor(COLOR_WHITE);
                g.fillRoundRect(
                        logoX - 5, logoY - 5,
                        LOGO_SIZE + 10, LOGO_SIZE + 10,
                        LOGO_CORNER_RADIUS, LOGO_CORNER_RADIUS
                );

                // Bordure colorée
                g.setColor(COLOR_RECYCLIX_GREEN);
                g.setStroke(new BasicStroke(2.5f));
                g.drawRoundRect(
                        logoX - 5, logoY - 5,
                        LOGO_SIZE + 10, LOGO_SIZE + 10,
                        LOGO_CORNER_RADIUS, LOGO_CORNER_RADIUS
                );

                // Dessiner le logo
                g.drawImage(resizedLogo, logoX, logoY, null);
            } else {
                // 🎨 Fallback : dessiner un cercle coloré avec les initiales
                drawFallbackLogo(g, logoX, logoY);
            }

        } finally {
            g.dispose();
        }

        return qrImage;
    }

    /**
     * 🖼️ Charge le logo en FORÇANT les couleurs
     */
//    private BufferedImage loadLogoWithForcedColors() {
//        String[] logoPaths = {
//                "classpath:static/images/logo1.png",
//                "classpath:static/logo1.png",
//                "classpath:images/logo1.png",
//                "classpath:logo1.png"
//        };
//
//        for (String path : logoPaths) {
//            try (InputStream logoStream = resourceLoader.getResource(path).getInputStream()) {
//                if (logoStream != null) {
//                    // Lire l'image originale
//                    BufferedImage original = ImageIO.read(logoStream);
//                    if (original != null) {
//
//                        // 🔥 FORCER la conversion en ARGB (couleurs)
//                        BufferedImage colorImage = new BufferedImage(
//                                original.getWidth(),
//                                original.getHeight(),
//                                BufferedImage.TYPE_INT_ARGB
//                        );
//
//                        Graphics2D g = colorImage.createGraphics();
//                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//                        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//                        g.drawImage(original, 0, 0, null);
//                        g.dispose();
//
//                        logtxt.info("✅ Logo chargé en couleurs depuis : {}", path);
//                        return colorImage;
//                    }
//                }
//            } catch (IOException e) {
//                logtxt.debug("Logo non trouvé dans : {}", path);
//            }
//        }
//
//        logtxt.warn("⚠️ Logo non trouvé, utilisation du fallback coloré");
//        return null;
//    }


    private BufferedImage loadLogoWithForcedColors() {
        String[] logoPaths = {
                "classpath:static/images/logo1.png",
                "classpath:static/logo1.png",
                "classpath:images/logo1.png",
                "classpath:logo1.png"
        };

        for (String path : logoPaths) {
            try {
                Resource resource = resourceLoader.getResource(path);

                if (!resource.exists()) {
                    continue;
                }

                try (InputStream inputStream = resource.getInputStream()) {
                    BufferedImage original = ImageIO.read(inputStream);

                    if (original == null) {
                        continue;
                    }

                    // Conserver les couleurs originales du logo
                    BufferedImage coloredLogo = new BufferedImage(
                            original.getWidth(),
                            original.getHeight(),
                            BufferedImage.TYPE_INT_ARGB
                    );

                    Graphics2D g2d = coloredLogo.createGraphics();
                    g2d.setComposite(AlphaComposite.SrcOver);
                    g2d.drawImage(original, 0, 0, null);
                    g2d.dispose();

                    log.info("✅ Logo couleur chargé depuis {}", path);
                    return coloredLogo;
                }

            } catch (IOException e) {
                log.debug("Logo introuvable dans {}", path);
            }
        }

        log.warn("⚠️ Aucun logo trouvé.");
        return null;
    }

    /**
     * 🎨 Fallback : dessine un logo coloré personnalisé
     */
    private void drawFallbackLogo(Graphics2D g, int x, int y) {
        // Cercle de fond vert Recyclix
        g.setColor(COLOR_RECYCLIX_GREEN);
        g.fillOval(x, y, LOGO_SIZE, LOGO_SIZE);

        // Cercle intérieur
        g.setColor(COLOR_WHITE);
        g.fillOval(x + 8, y + 8, LOGO_SIZE - 16, LOGO_SIZE - 16);

        // Lettre "R" en vert
        g.setColor(COLOR_RECYCLIX_DARK_GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 42));
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (LOGO_SIZE - fm.stringWidth("R")) / 2;
        int textY = y + ((LOGO_SIZE - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString("R", textX, textY);

        // Petites feuilles (décoration)
        g.setColor(COLOR_RECYCLIX_GREEN);
        g.fillOval(x + LOGO_SIZE - 15, y + 5, 10, 10);
        g.fillOval(x + 5, y + LOGO_SIZE - 15, 10, 10);
    }

    /**
     * Redimensionnement haute qualité
     */
    private BufferedImage resizeImageWithHighQuality(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();

        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

            g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g.dispose();
        }

        return resized;
    }

    // =========================================================
    // MÉTHODES PUBLIQUES EXISTANTES
    // =========================================================

    public String generateQRCodeAsBase64(String content) {
        byte[] qrCodeBytes = generateQRCodeWithLogo(content);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrCodeBytes);
    }

    public String generateValidationUrl(String validationCode) {
        return baseUrl + "/validate?code=" + validationCode;
    }

    public byte[] generateValidationQRCode(String validationCode) {
        String url = generateValidationUrl(validationCode);
        return generateQRCodeWithLogo(url);
    }

    public byte[] generateSimpleQRCode(String content) {
        if (content == null || content.isBlank()) {
            throw new BadRequestException("Le contenu du QR code ne peut pas être vide.");
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            return baos.toByteArray();

        } catch (WriterException | IOException e) {
            log.error("Erreur lors de la génération du QR code", e);
            throw new BadRequestException("Impossible de générer le QR code : " + e.getMessage());
        }
    }

    public ResponseEntity<Resource> downloadQRCode(String validationCode, String filename) {
        byte[] qrCodeBytes = generateValidationQRCode(validationCode);
        ByteArrayResource resource = new ByteArrayResource(qrCodeBytes);

        String safeFilename = filename + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                ".png";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFilename + "\"")
                .contentType(MediaType.IMAGE_PNG)
                .contentLength(qrCodeBytes.length)
                .body(resource);
    }

    public byte[] generateCompactQRCode(String content) {
        if (content == null || content.isBlank()) {
            throw new BadRequestException("Le contenu du QR code ne peut pas être vide.");
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 250, 250);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            return baos.toByteArray();

        } catch (WriterException | IOException e) {
            log.error("Erreur lors de la génération du QR code compact", e);
            throw new BadRequestException("Impossible de générer le QR code : " + e.getMessage());
        }
    }



    private BufferedImage convertToARGB(BufferedImage image) {
        BufferedImage argbImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = argbImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return argbImage;
    }
}