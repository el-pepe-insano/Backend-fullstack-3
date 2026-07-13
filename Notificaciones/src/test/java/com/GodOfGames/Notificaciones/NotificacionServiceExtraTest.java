package com.GodOfGames.Notificaciones;

import com.GodOfGames.Notificaciones.repositories.CodigoRecuperacionRepository;
import com.GodOfGames.Notificaciones.services.NotificacionService;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class NotificacionServiceExtraTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private CodigoRecuperacionRepository codigoRepository;

    private NotificacionService notificacionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notificacionService = new NotificacionService(mailSender, codigoRepository);
    }

    @Test
    void enviarCodigoRecuperacion_exito_eliminaCodigoPrevioYEnviaCorreo() {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(codigoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> notificacionService.enviarCodigoRecuperacion("juan@test.com", "Juan"));

        verify(codigoRepository, times(1)).deleteByCorreo("juan@test.com");
        verify(codigoRepository, times(1)).save(argThat(c -> c.getCorreo().equals("juan@test.com") && !c.isUsado()));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void enviarCodigoRecuperacion_generaCodigoDeSeisDigitos() {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(codigoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        notificacionService.enviarCodigoRecuperacion("juan@test.com", "Juan");

        verify(codigoRepository).save(argThat(c -> c.getCodigo().length() == 6 && c.getCodigo().chars().allMatch(Character::isDigit)));
    }
}