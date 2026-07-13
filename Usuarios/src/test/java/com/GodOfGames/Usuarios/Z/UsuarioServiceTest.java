package com.GodOfGames.Usuarios.Z;

import com.GodOfGames.Usuarios.Z.Service.UsuarioService;
import com.GodOfGames.Usuarios.Z.models.Rol;
import com.GodOfGames.Usuarios.Z.models.Usuario;
import com.GodOfGames.Usuarios.Z.repositories.UsuarioRepository;
import com.GodOfGames.Usuarios.dto.ActualizarPerfilDTO;
import com.GodOfGames.Usuarios.dto.CambiarContrasenasincodigoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, rabbitTemplate, webClientBuilder);
    }

    private Usuario usuarioBase() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNombre("Juan");
        u.setCorreo("juan@test.com");
        u.setContrasena("hashed");
        u.setRol(Rol.CLIENTE);
        u.setActivo(true);
        return u;
    }

    @Test
    void registrarUsuario_exitoso() {
        Usuario u = usuarioBase();
        u.setContrasena("Pass123!");
        when(usuarioRepository.findByCorreo(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(usuarioRepository.save(any())).thenReturn(u);
        Usuario resultado = usuarioService.registrarUsuario(u);
        assertNotNull(resultado);
        assertEquals(Rol.CLIENTE, resultado.getRol());
    }

    @Test
    void registrarUsuario_correoExistente_lanzaExcepcion() {
        Usuario u = usuarioBase();
        when(usuarioRepository.findByCorreo(any())).thenReturn(Optional.of(u));
        assertThrows(RuntimeException.class, () -> usuarioService.registrarUsuario(u));
    }

    @Test
    void registrarAdmin_correoMaestro() {
        Usuario u = usuarioBase();
        u.setCorreo("diego@godofgames.com");
        u.setContrasena("Admin123!");
        when(usuarioRepository.findByCorreo(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Usuario resultado = usuarioService.registrarUsuario(u);
        assertEquals(Rol.ADMIN, resultado.getRol());
    }

    @Test
    void registrarUsuario_intentaAdmin_degradaACliente() {
        Usuario u = usuarioBase();
        u.setRol(Rol.ADMIN);
        when(usuarioRepository.findByCorreo(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Usuario resultado = usuarioService.registrarUsuario(u);
        assertEquals(Rol.CLIENTE, resultado.getRol());
    }

    @Test
    void iniciarSesion_exitoso() {
        Usuario u = usuarioBase();
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("Pass123!", "hashed")).thenReturn(true);
        Usuario resultado = usuarioService.iniciarSesion("juan@test.com", "Pass123!");
        assertNotNull(resultado);
    }

    @Test
    void iniciarSesion_desactivado_lanzaExcepcion() {
        Usuario u = usuarioBase();
        u.setActivo(false);
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(u));
        assertThrows(RuntimeException.class, () -> usuarioService.iniciarSesion("juan@test.com", "Pass123!"));
    }

    @Test
    void iniciarSesion_credencialesInvalidas_lanzaExcepcion() {
        Usuario u = usuarioBase();
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        assertThrows(RuntimeException.class, () -> usuarioService.iniciarSesion("juan@test.com", "wrong"));
    }

    @Test
    void toggleActivarUsuario_desactiva() {
        Usuario u = usuarioBase();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Usuario resultado = usuarioService.toggleActivarUsuario(1L);
        assertFalse(resultado.isActivo());
    }

    @Test
    void toggleActivarUsuario_noExiste_lanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> usuarioService.toggleActivarUsuario(99L));
    }

    @Test
    void actualizarUsuario_exitoso() {
        Usuario u = usuarioBase();
        ActualizarPerfilDTO dto = new ActualizarPerfilDTO();
        dto.setNombre("Nuevo");
        dto.setCorreo("juan@test.com");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Usuario resultado = usuarioService.actualizarUsuario(1L, dto);
        assertEquals("Nuevo", resultado.getNombre());
    }

    @Test
    void actualizarUsuario_noExiste_lanzaExcepcion() {
        ActualizarPerfilDTO dto = new ActualizarPerfilDTO();
        dto.setNombre("Nuevo");
        dto.setCorreo("juan@test.com");
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> usuarioService.actualizarUsuario(99L, dto));
    }

    @Test
    void solicitarRecuperacion_exitoso() {
        Usuario u = usuarioBase();
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(u));
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
        assertDoesNotThrow(() -> usuarioService.solicitarRecuperacion("juan@test.com"));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void solicitarRecuperacion_noExiste_lanzaExcepcion() {
        when(usuarioRepository.findByCorreo("noexiste@test.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> usuarioService.solicitarRecuperacion("noexiste@test.com"));
    }

    @Test
    void cambiarContrasena_exitoso() {
        Usuario u = usuarioBase();
        CambiarContrasenasincodigoDTO dto = new CambiarContrasenasincodigoDTO();
        dto.setCorreo("juan@test.com");
        dto.setContrasenaActual("Pass123!");
        dto.setNuevaContrasena("Nueva456!");
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("Pass123!", "hashed")).thenReturn(true);
        when(passwordEncoder.matches("Nueva456!", "hashed")).thenReturn(false);
        when(passwordEncoder.encode("Nueva456!")).thenReturn("newHashed");
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertDoesNotThrow(() -> usuarioService.cambiarContrasena(dto));
    }

    @Test
    void cambiarContrasena_contrasenaActualIncorrecta_lanzaExcepcion() {
        Usuario u = usuarioBase();
        CambiarContrasenasincodigoDTO dto = new CambiarContrasenasincodigoDTO();
        dto.setCorreo("juan@test.com");
        dto.setContrasenaActual("wrong");
        dto.setNuevaContrasena("Nueva456!");
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
        assertThrows(RuntimeException.class, () -> usuarioService.cambiarContrasena(dto));
    }

    @Test
    void cambiarContrasena_mismaContrasena_lanzaExcepcion() {
        Usuario u = usuarioBase();
        CambiarContrasenasincodigoDTO dto = new CambiarContrasenasincodigoDTO();
        dto.setCorreo("juan@test.com");
        dto.setContrasenaActual("Pass123!");
        dto.setNuevaContrasena("Pass123!");
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("Pass123!", "hashed")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> usuarioService.cambiarContrasena(dto));
    }

    @Test
    void listarUsuarios_retornaLista() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioBase()));
        assertEquals(1, usuarioService.listarUsuarios().size());
    }

    @Test
    void crearUsuarioAdmin_exitoso() {
        Usuario u = usuarioBase();
        u.setRol(Rol.ADMIN);
        when(usuarioRepository.findByCorreo(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(usuarioRepository.save(any())).thenReturn(u);
        Usuario resultado = usuarioService.CrearUsuarioAdmin(u);
        assertEquals(Rol.ADMIN, resultado.getRol());
    }

    @Test
    void crearUsuarioAdmin_correoExistente_lanzaExcepcion() {
        Usuario u = usuarioBase();
        when(usuarioRepository.findByCorreo(any())).thenReturn(Optional.of(u));
        assertThrows(RuntimeException.class, () -> usuarioService.CrearUsuarioAdmin(u));
    }
}