package com.GodOfGames.Notificaciones.services;

import com.GodOfGames.Notificaciones.models.CodigoRecuperacion;
import com.GodOfGames.Notificaciones.repositories.CodigoRecuperacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // Eliminar códigos anteriores
        codigoRepository.deleteByCorreo(correo);

        // Generar código de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(999999));

        // Guardar código en BD
        CodigoRecuperacion codigoRecuperacion = CodigoRecuperacion.builder()
                .correo(correo)
                .codigo(codigo)
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusMinutes(15))
                .usado(false)
                .build();

        codigoRepository.save(codigoRecuperacion);

        // Enviar email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("diegolaraa10@gmail.com");
        message.setTo(correo);
        message.setSubject("GodOfGames - Recuperación de Contraseña");
        message.setText(
            "Hola " + nombre + ",\n\n" +
            "Tu código de recuperación de contraseña es:\n\n" +
            "  " + codigo + "\n\n" +
            "Este código expira en 15 minutos.\n\n" +
            "Si no solicitaste este código, ignora este mensaje.\n\n" +
            "GodOfGames Team"
        );

        mailSender.send(message);
        log.info("Código de recuperación enviado a: {}", correo);
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