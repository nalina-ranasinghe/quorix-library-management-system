package com.library.app.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * Sends an HTML email using a Thymeleaf template.
     *
     * @param to The recipient's email address.
     * @param subject The email subject.
     * @param templateName The name of the Thymeleaf template file (e.g., "email-report").
     * @param templateModel A map of data to be passed to the template.
     */
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> templateModel) {
        try {
            // Prepare the evaluation context with the model data
            Context context = new Context();
            context.setVariables(templateModel);

            // Process the Thymeleaf template into an HTML string
            String htmlBody = templateEngine.process("emails/" + templateName, context);

            // Create and send the MIME message
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {

            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}