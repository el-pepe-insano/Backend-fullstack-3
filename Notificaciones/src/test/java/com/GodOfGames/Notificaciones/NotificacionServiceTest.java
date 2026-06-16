package com.GodOfGames.Notificaciones;

import com.GodOfGames.Notificaciones.models.CodigoRecuperacion;
import com.GodOfGames.Notificaciones.repositories.CodigoRecuperacionRepository;
import com.GodOfGames.Notificaciones.services.NotificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private CodigoRecuperacionRepository codigoRepository;

    @InjectMocks
    private NotificacionService notificacionService;

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

    @Test
    void verificarCodigo_marcaComoUsado() {
        CodigoRecuperacion codigo = CodigoRecuperacion.builder()
                .correo("juan@test.com")
                .codigo("123456")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .usado(false)
                .build();

        when(codigoRepository.findByCorreoAndCodigoAndUsadoFalse("juan@test.com", "123456"))
                .thenReturn(Optional.of(codigo));
        when(codigoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        notificacionService.verificarCodigo("juan@test.com", "123456");
        verify(codigoRepository).save(argThat(c -> c.isUsado()));
    }
}
