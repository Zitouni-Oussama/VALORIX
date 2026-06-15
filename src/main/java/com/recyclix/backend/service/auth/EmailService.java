package com.recyclix.backend.service.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendResetCode(String toEmail, String code, String fullName) {
        String subject = "Recyclix - Code de réinitialisation de mot de passe";
        String htmlContent = buildHtmlEmail(code, fullName);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Attacher le logo (inline) avec un Content-ID
            ClassPathResource logoResource = new ClassPathResource("static/images/logo.png");
            if (logoResource.exists()) {
                helper.addInline("logo", logoResource);
                log.info("Logo attaché à l'email");
            } else {
                log.warn("Logo non trouvé dans classpath:static/images/logo.png");
            }

            mailSender.send(message);
            log.info("Email de réinitialisation envoyé à {}", toEmail);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email de réinitialisation", e);
        }
    }

    private String buildHtmlEmail(String code, String fullName) {
        String name = (fullName != null && !fullName.isBlank()) ? fullName : "Utilisateur";
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Réinitialisation de mot de passe - Recyclix</title>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        background-color: #f4f7fc;
                        font-family: 'Segoe UI', 'Inter', Arial, sans-serif;
                    }
                    .container {
                        max-width: 560px;
                        margin: 30px auto;
                        background: #ffffff;
                        border-radius: 28px;
                        box-shadow: 0 20px 35px -10px rgba(0, 0, 0, 0.08);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #43A047 0%%, #0A4FB3 100%%);
                        padding: 32px 24px;
                        text-align: center;
                    }
                    .logo {
                        max-width: 250px;
                        margin-bottom: 16px;
                    }
                    .header h1 {
                        color: white;
                        font-size: 26px;
                        font-weight: 700;
                        margin: 0;
                    }
                    .content {
                        padding: 32px 28px;
                    }
                    h2 {
                        color: #14213D;
                        font-size: 22px;
                        margin-top: 0;
                        margin-bottom: 16px;
                    }
                    .message {
                        color: #5C6B80;
                        line-height: 1.5;
                        margin-bottom: 24px;
                    }
                    .code-box {
                        background: #F0F8FF;
                        border: 2px dashed #0D6EFD;
                        border-radius: 20px;
                        padding: 20px;
                        text-align: center;
                        margin: 24px 0;
                    }
                    .code {
                        font-size: 36px;
                        font-weight: 800;
                        letter-spacing: 6px;
                        color: #0D6EFD;
                        font-family: 'Courier New', monospace;
                    }
                    .info {
                        background: #F8F9FA;
                        border-radius: 16px;
                        padding: 16px;
                        font-size: 13px;
                        color: #6C757D;
                        text-align: center;
                        margin: 20px 0;
                    }
                    .footer {
                        background: #F8F9FA;
                        padding: 20px;
                        text-align: center;
                        font-size: 12px;
                        color: #ADB5BD;
                        border-top: 1px solid #E9ECEF;
                    }
                    .button {
                        display: inline-block;
                        background: #0D6EFD;
                        color: white;
                        text-decoration: none;
                        padding: 12px 28px;
                        border-radius: 40px;
                        font-weight: 600;
                        margin-top: 16px;
                    }
                    @media (max-width: 600px) {
                        .content { padding: 24px 20px; }
                        .code { font-size: 28px; letter-spacing: 3px; }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <img class="logo" src="cid:logo" alt="Recyclix VALORIX">
                        <h1>Réinitialisation 🔐</h1>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <div class="message">
                            Vous avez demandé à réinitialiser votre mot de passe. Utilisez le code ci-dessous pour définir un nouveau mot de passe.
                        </div>
                        <div class="code-box">
                            <div class="code">%s</div>
                        </div>
                        <div class="info">
                            ⏱️ Ce code est valable <strong>30 minutes</strong>.<br>
                            🔒 Pour des raisons de sécurité, ne partagez jamais ce code.
                        </div>
                        <div class="message">
                            Si vous n'êtes pas à l'origine de cette demande, ignorez cet email. Votre mot de passe ne sera pas modifié.
                        </div>
                    </div>
                    <div class="footer">
                        © 2026 Recyclix · VALORIX – Tous droits réservés<br>
                        Recycler pour un avenir durable ♻️
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, code);
    }
}