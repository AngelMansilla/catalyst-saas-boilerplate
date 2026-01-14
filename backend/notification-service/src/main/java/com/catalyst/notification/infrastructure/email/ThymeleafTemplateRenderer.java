package com.catalyst.notification.infrastructure.email;

import com.catalyst.notification.application.ports.output.TemplateRenderer;
import com.catalyst.notification.domain.exception.TemplateNotFoundException;
import com.catalyst.notification.domain.valueobject.NotificationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

/**
 * Thymeleaf implementation of TemplateRenderer port.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Component
public class ThymeleafTemplateRenderer implements TemplateRenderer {
    
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;
    
    @Value("${email.base-url:http://localhost:3000}")
    private String baseUrl;
    
    @Value("${email.logo-url:http://localhost:3000/logo.png}")
    private String logoUrl;
    
    public ThymeleafTemplateRenderer(TemplateEngine templateEngine, MessageSource messageSource) {
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
    }
    
    @Override
    public String render(NotificationType type, Map<String, Object> templateData) {
        try {
            String templateName = "email/" + type.getTemplateName();
            
            Context context = new Context(Locale.ENGLISH);
            
            // Add base template variables
            context.setVariable("baseUrl", baseUrl);
            context.setVariable("logoUrl", logoUrl);
            context.setVariable("subject", messageSource.getMessage(
                "email." + type.getCode() + ".subject", 
                null, 
                Locale.ENGLISH
            ));
            
            // Add custom template data
            if (templateData != null) {
                templateData.forEach(context::setVariable);
            }
            
            // Add message source to context for i18n
            context.setVariable("messageSource", messageSource);
            context.setVariable("locale", Locale.ENGLISH);
            
            return templateEngine.process(templateName, context);
            
        } catch (Exception e) {
            throw new TemplateNotFoundException(type.getTemplateName());
        }
    }
}

