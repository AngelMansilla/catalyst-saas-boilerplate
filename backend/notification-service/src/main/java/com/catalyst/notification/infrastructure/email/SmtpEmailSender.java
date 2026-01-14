package com.catalyst.notification.infrastructure.email;

import com.catalyst.notification.application.ports.output.EmailSender;
import com.catalyst.notification.application.ports.output.TemplateRenderer;
import com.catalyst.notification.domain.exception.EmailDeliveryException;
import com.catalyst.notification.domain.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * SMTP implementation of EmailSender port.
 * Uses JavaMailSender (Mailpit for dev, configured SMTP for prod).
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Component
public class SmtpEmailSender implements EmailSender {
    
    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);
    
    private final JavaMailSender mailSender;
    private final TemplateRenderer templateRenderer;
    
    @Value("${spring.mail.from:noreply@catalyst.com}")
    private String fromEmail;
    
    @Value("${spring.mail.from-name:Catalyst}")
    private String fromName;
    
    public SmtpEmailSender(JavaMailSender mailSender, TemplateRenderer templateRenderer) {
        this.mailSender = mailSender;
        this.templateRenderer = templateRenderer;
    }
    
    @Override
    public void send(Notification notification) {
        log.info("Sending {} email to {}", notification.getType().getCode(), notification.getRecipient());
        
        try {
            // Render template
            String htmlContent = templateRenderer.render(
                notification.getType(),
                notification.getTemplateData()
            );
            
            // Create MIME message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(notification.getRecipient().toString());
            helper.setSubject(notification.getSubject());
            helper.setText(htmlContent, true); // true = HTML
            
            // Send email
            mailSender.send(message);
            
            log.info("Email sent successfully to {}", notification.getRecipient());
            
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", notification.getRecipient(), e.getMessage(), e);
            throw new EmailDeliveryException("Failed to send email: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", notification.getRecipient(), e.getMessage(), e);
            throw new EmailDeliveryException("Unexpected error: " + e.getMessage(), e);
        }
    }
}

