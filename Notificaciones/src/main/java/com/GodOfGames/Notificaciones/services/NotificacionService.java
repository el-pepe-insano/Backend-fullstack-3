package com.GodOfGames.Notificaciones.services;

import com.GodOfGames.Notificaciones.models.CodigoRecuperacion;
import com.GodOfGames.Notificaciones.repositories.CodigoRecuperacionRepository;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; 
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.mail.MessagingException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final JavaMailSender mailSender;
    private final CodigoRecuperacionRepository codigoRepository;

   @Transactional
public void enviarCodigoRecuperacion(String correo, String nombre) {
    codigoRepository.deleteByCorreo(correo);

    String codigo = String.format("%06d", new Random().nextInt(999999));

    CodigoRecuperacion codigoRecuperacion = CodigoRecuperacion.builder()
            .correo(correo)
            .codigo(codigo)
            .fechaCreacion(LocalDateTime.now())
            .fechaExpiracion(LocalDateTime.now().plusMinutes(15))
            .usado(false)
            .build();

    codigoRepository.save(codigoRecuperacion);

    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("diegolaraa10@gmail.com");
        helper.setTo(correo);
        helper.setSubject("GodOfGames - Recuperación de Contraseña");
        helper.setText(buildEmailHtml(nombre, codigo), true);

        mailSender.send(message);
        log.info("Código de recuperación enviado a: {}", correo);

    } catch (MessagingException e) {
        log.error("Error al enviar correo a: {}", correo, e);
        throw new RuntimeException("Error al enviar el correo de recuperación");
    }
}

private String buildEmailHtml(String nombre, String codigo) {
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
        </head>
        <body style="margin:0;padding:0;background-color:#0f0f0f;font-family:Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#0f0f0f;padding:40px 0;">
            <tr>
              <td align="center">
                <table width="520" cellpadding="0" cellspacing="0" style="background-color:#1a1a2e;border-radius:12px;overflow:hidden;border:1px solid #7c3aed;">

                  <!-- Header -->
                  <tr>
                    <td style="background:linear-gradient(135deg,#7c3aed,#4f46e5);padding:32px;text-align:center;">
                      <h1 style="margin:0;color:#ffffff;font-size:28px;letter-spacing:2px;">⚔️ GOD OF GAMES</h1>
                      <p style="margin:8px 0 0;color:#c4b5fd;font-size:14px;">Recuperación de Contraseña</p>
                    </td>
                  </tr>

                  <!-- Body -->
                  <tr>
                    <td style="padding:40px 36px;">
                      <p style="color:#e2e8f0;font-size:16px;margin:0 0 8px;">Hola, <strong style="color:#a78bfa;">{{nombre}}</strong> 👋</p>
                      <p style="color:#94a3b8;font-size:14px;margin:0 0 32px;line-height:1.6;">
                        Recibimos una solicitud para restablecer tu contraseña. Usa el siguiente código:
                      </p>

                      <!-- Código -->
                      <div style="background-color:#0f0f0f;border:2px dashed #7c3aed;border-radius:10px;padding:24px;text-align:center;margin-bottom:32px;">
                        <p style="margin:0 0 8px;color:#94a3b8;font-size:12px;letter-spacing:3px;text-transform:uppercase;">Tu código</p>
                        <span style="font-size:42px;font-weight:bold;color:#a78bfa;letter-spacing:10px;">{{codigo}}</span>
                      </div>

                      <p style="color:#94a3b8;font-size:13px;margin:0 0 8px;">⏱️ Este código expira en <strong style="color:#f59e0b;">15 minutos</strong>.</p>
                      <p style="color:#64748b;font-size:12px;margin:0;">Si no solicitaste este código, puedes ignorar este mensaje.</p>
                    </td>
                  </tr>

                    <!-- Footer -->
                    <tr>
                      <td style="background-color:#0f0f1a;padding:20px;text-align:center;border-top:1px solid #2d2d4e;">
                       <p style="margin:0;color:#475569;font-size:12px;">© 2025 GodOfGames · Todos los derechos reservados</p>
                      </td>
                    </tr>

                  </table>
                 </td>
              </tr>
            </table>
            </body>
             </html>
             """
        .replace("{{nombre}}", nombre)
        .replace("{{codigo}}", codigo);
       }

    @Transactional
    public boolean verificarCodigo(String correo, String codigo) {
        Optional<CodigoRecuperacion> codigoOpt = codigoRepository
                .findByCorreoAndCodigoAndUsadoFalse(correo, codigo);

        if (codigoOpt.isEmpty()) {
            return false;
        }

        CodigoRecuperacion codigoRecuperacion = codigoOpt.get();

        if (codigoRecuperacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return false;
        }

        codigoRecuperacion.setUsado(true);
        codigoRepository.save(codigoRecuperacion);
        return true;
    }
}