package com.budgetpro.infrastructure.notification;

import com.budgetpro.infrastructure.persistence.entity.marketing.LeadEntity;
import com.resend.Resend;
import com.resend.services.emails.model.SendEmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio para notificaciones por correo.
 */
@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final String adminEmail;
    private final String sender;
    private final Resend resend;

    public EmailNotificationService(
            @Value("${resend.api.key:}") String apiKey,
            @Value("${app.notification.email}") String adminEmail,
            @Value("${app.notification.sender}") String sender) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("RESEND_API_KEY es obligatorio para notificaciones.");
        }
        if (adminEmail == null || adminEmail.isBlank()) {
            throw new IllegalStateException("ADMIN_EMAIL es obligatorio para notificaciones.");
        }
        if (sender == null || sender.isBlank()) {
            throw new IllegalStateException("NOTIFICATION_SENDER es obligatorio para notificaciones.");
        }
        this.adminEmail = adminEmail;
        this.sender = sender;
        this.resend = new Resend(apiKey);
    }

    public void enviarAlertaNuevoLead(LeadEntity lead) {
        try {
            String empresa = lead.getNombreEmpresa() != null && !lead.getNombreEmpresa().isBlank()
                    ? lead.getNombreEmpresa()
                    : "Sin empresa";

            String subject = "🚀 Nuevo Lead BudgetPro: " + empresa;
            String html = """
                    <div style="font-family: Arial, sans-serif; color: #0f172b;">
                      <h2 style="color:#1c398e;">¡Buenas noticias! Un nuevo cliente potencial ha solicitado una demo.</h2>
                      <p><strong>Nombre:</strong> %s</p>
                      <p><strong>Empresa:</strong> %s</p>
                      <p><strong>Email:</strong> %s</p>
                      <p><strong>Teléfono:</strong> %s</p>
                      <p><strong>Cargo:</strong> %s</p>
                    </div>
                    """.formatted(
                    safe(lead.getNombreContacto()),
                    safe(lead.getNombreEmpresa()),
                    safe(lead.getEmail()),
                    safe(lead.getTelefono()),
                    safe(lead.getRol())
            );

            SendEmailRequest request = SendEmailRequest.builder()
                    .from(sender)
                    .to(adminEmail)
                    .subject(subject)
                    .html(html)
                    .build();

            resend.emails().send(request);
        } catch (Exception ex) {
            log.error("Error enviando notificación de lead {}", lead.getId(), ex);
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "No especificado" : value;
    }
}
