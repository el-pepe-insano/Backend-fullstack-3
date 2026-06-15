package com.GodOfGames.Notificaciones;

import com.GodOfGames.Notificaciones.models.CodigoRecuperacion;
import com.GodOfGames.Notificaciones.repositories.CodigoRecuperacionRepository;
import com.GodOfGames.Notificaciones.services.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificacionServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private CodigoRecuperacionRepository codigoRepository;

    @InjectMocks
    private NotificacionService notificacionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void enviarCodigoRecuperacion_exitoso() {
        doNothing().when(codigoRepository).deleteByCorreo(any());
        when(codigoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> notificacionService.enviarCodigoRecuperacion("juan@test.com", "Juan"));

        verify(codigoRepository).deleteByCorreo("juan@test.com");
        verify(codigoRepository).save(any());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void verificarCodigo_valido() {
        CodigoRecuperacion codigo = CodigoRecuperacion.builder()
                .correo("juan@test.com")
                .codigo("123456")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .usado(false)
                .build();

        when(codigoRepository.findByCorreoAndCodigoAndUsadoFalse("juan@test.com", "123456"))
                .thenReturn(Optional.of(codigo));
        when(codigoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        boolean resultado = notificacionService.verificarCodigo("juan@test.com", "123456");
        assertTrue(resultado);
        assertTrue(codigo.isUsado());
    }

    @Test
    void verificarCodigo_noExiste_retornaFalse() {
        when(codigoRepository.findByCorreoAndCodigoAndUsadoFalse(any(), any()))
                .thenReturn(Optional.empty());

        boolean resultado = notificacionService.verificarCodigo("juan@test.com", "000000");
        assertFalse(resultado);
    }

    @Test
    void verificarCodigo_expirado_retornaFalse() {
        CodigoRecuperacion codigo = CodigoRecuperacion.builder()
                .correo("juan@test.com")
                .codigo("123456")
                .fechaExpiracion(LocalDateTime.now().minusMinutes(5))
                .usado(false)
                .build();

        when(codigoRepository.findByCorreoAndCodigoAndUsadoFalse("juan@test.com", "123456"))
                .thenReturn(Optional.of(codigo));

        boolean resultado = notificacionService.verificarCodigo("juan@test.com", "123456");
        assertFalse(resultado);
    }

    @Test
    void verificarCodigo_yaUsado_retornaFalse() {
        when(codigoRepository.findByCorreoAndCodigoAndUsadoFalse(any(), any()))
                .thenReturn(Optional.empty());

        boolean resultado = notificacionService.verificarCodigo("juan@test.com", "123456");
        assertFalse(resultado);
    }
}
