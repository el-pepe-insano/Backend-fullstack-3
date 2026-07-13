package com.GodOfGames.Usuarios.Z;

import com.GodOfGames.Usuarios.Z.Service.UsuarioService;
import com.GodOfGames.Usuarios.Z.models.Rol;
import com.GodOfGames.Usuarios.Z.models.Usuario;
import com.GodOfGames.Usuarios.Z.repositories.UsuarioRepository;
import com.GodOfGames.Usuarios.dto.ActualizarPerfilDTO;
import com.GodOfGames.Usuarios.dto.CambiarContrasenaDTO;
import com.GodOfGames.Usuarios.dto.CambiarContrasenasincodigoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UsuarioServiceExtraTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, rabbitTemplate, webClientBuilder);
    }

    private Usuario usuarioBase() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setCorreo("juan@test.com");
        usuario.setContrasena("hashed");
        usuario.setRol(Rol.CLIENTE);
        usuario.setActivo(true);
        return usuario;
    }

    @Test
    void iniciarSesion_contrasenaIncorrecta_lanzaExcepcion() {
        Usuario usuario = usuarioBase();
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> usuarioService.iniciarSesion("juan@test.com", "MalaClave1!"));
    }

    @Test
    void iniciarSesion_correoNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByCorreo("noexiste@test.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> usuarioService.iniciarSesion("noexiste@test.com", "algo"));
    }

    @Test
    void actualizarUsuario_exito_sinCambiarCorreo() {
        Usuario usuario = usuarioBase();
        ActualizarPerfilDTO dto = new ActualizarPerfilDTO();
        dto.setNombre("Juan Actualizado");
        dto.setCorreo("juan@test.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Usuario resultado = usuarioService.actualizarUsuario(1L, dto);
        assertEquals("Juan Actualizado", resultado.getNombre());
    }

    @Test
    void actualizarUsuario_cambiaCorreoDisponible() {
        Usuario usuario = usuarioBase();
        ActualizarPerfilDTO dto = new ActualizarPerfilDTO();
        dto.setNombre("Juan");
        dto.setCorreo("nuevo@test.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCorreo("nuevo@test.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Usuario resultado = usuarioService.actualizarUsuario(1L, dto);
        assertEquals("nuevo@test.com", resultado.getCorreo());
    }

    @Test
    void actualizarUsuario_correoEnUso_lanzaExcepcion() {
        Usuario usuario = usuarioBase();
        ActualizarPerfilDTO dto = new ActualizarPerfilDTO();
        dto.setNombre("Juan");
        dto.setCorreo("otro@test.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCorreo("otro@test.com")).thenReturn(Optional.of(new Usuario()));

        assertThrows(RuntimeException.class, () -> usuarioService.actualizarUsuario(1L, dto));
    }

    @Test
    void actualizarUsuario_noEncontrado_lanzaExcepcion() {
        ActualizarPerfilDTO dto = new ActualizarPerfilDTO();
        dto.setNombre("Juan");
        dto.setCorreo("juan@test.com");
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> usuarioService.actualizarUsuario(99L, dto));
    }

    @Test
    void toggleActivarUsuario_noEncontrado_lanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> usuarioService.toggleActivarUsuario(99L));
    }

    @Test
    void solicitarRecuperacion_exito() {
        Usuario usuario = usuarioBase();
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuario));

        usuarioService.solicitarRecuperacion("juan@test.com");

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(com.GodOfGames.Usuarios.Z.messaging.RecuperacionEvent.class));
    }

    @Test
    void solicitarRecuperacion_noEncontrado_lanzaExcepcion() {
        when(usuarioRepository.findByCorreo("noexiste@test.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> usuarioService.solicitarRecuperacion("noexiste@test.com"));
    }

    @Test
    void cambiarContrasenaConCodigo_exito() {
        CambiarContrasenaDTO dto = new CambiarContrasenaDTO();
        dto.setCorreo("juan@test.com");
        dto.setCodigo("123456");
        dto.setNuevaContrasena("NuevaClave1!");

        Usuario usuario = usuarioBase();
        Map<String, Object> respuestaValida = Map.of("valido", true);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), eq("juan@test.com"), eq("123456"))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(reactor.core.publisher.Mono.just(respuestaValida));
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(anyString())).thenReturn("nuevoHash");
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        usuarioService.cambiarContrasenaConCodigo(dto);

        verify(usuarioRepository, times(1)).save(any());
    }

    @Test
    void cambiarContrasenaConCodigo_codigoInvalido_lanzaExcepcion() {
        CambiarContrasenaDTO dto = new CambiarContrasenaDTO();
        dto.setCorreo("juan@test.com");
        dto.setCodigo("000000");
        dto.setNuevaContrasena("NuevaClave1!");

        Map<String, Object> respuestaInvalida = Map.of("valido", false);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), eq("juan@test.com"), eq("000000"))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(reactor.core.publisher.Mono.just(respuestaInvalida));

        assertThrows(RuntimeException.class, () -> usuarioService.cambiarContrasenaConCodigo(dto));
    }

    @Test
    void cambiarContrasena_sinCodigo_exito() {
        CambiarContrasenasincodigoDTO dto = new CambiarContrasenasincodigoDTO();
        dto.setCorreo("juan@test.com");
        dto.setContrasenaActual("ClaveVieja1!");
        dto.setNuevaContrasena("ClaveNueva1!");

        Usuario usuario = usuarioBase();
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("ClaveVieja1!", "hashed")).thenReturn(true);
        when(passwordEncoder.matches("ClaveNueva1!", "hashed")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashNuevo");
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        usuarioService.cambiarContrasena(dto);
        verify(usuarioRepository, times(1)).save(any());
    }

    @Test
    void cambiarContrasena_actualIncorrecta_lanzaExcepcion() {
        CambiarContrasenasincodigoDTO dto = new CambiarContrasenasincodigoDTO();
        dto.setCorreo("juan@test.com");
        dto.setContrasenaActual("Incorrecta1!");
        dto.setNuevaContrasena("ClaveNueva1!");

        Usuario usuario = usuarioBase();
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Incorrecta1!", "hashed")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> usuarioService.cambiarContrasena(dto));
    }

    @Test
    void cambiarContrasena_igualALaActual_lanzaExcepcion() {
        CambiarContrasenasincodigoDTO dto = new CambiarContrasenasincodigoDTO();
        dto.setCorreo("juan@test.com");
        dto.setContrasenaActual("ClaveVieja1!");
        dto.setNuevaContrasena("ClaveVieja1!");

        Usuario usuario = usuarioBase();
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("ClaveVieja1!", "hashed")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> usuarioService.cambiarContrasena(dto));
    }

    @Test
    void listarUsuarios_retornaLista() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioBase()));
        List<Usuario> resultado = usuarioService.listarUsuarios();
        assertEquals(1, resultado.size());
    }

    @Test
    void crearUsuarioAdmin_exito() {
        Usuario nuevoAdmin = new Usuario();
        nuevoAdmin.setNombre("Admin2");
        nuevoAdmin.setCorreo("admin2@test.com");
        nuevoAdmin.setContrasena("Clave123!");

        when(usuarioRepository.findByCorreo("admin2@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Usuario resultado = usuarioService.CrearUsuarioAdmin(nuevoAdmin);
        assertEquals(Rol.ADMIN, resultado.getRol());
    }

    @Test
    void crearUsuarioAdmin_correoExistente_lanzaExcepcion() {
        Usuario nuevoAdmin = new Usuario();
        nuevoAdmin.setCorreo("admin2@test.com");
        when(usuarioRepository.findByCorreo("admin2@test.com")).thenReturn(Optional.of(new Usuario()));

        assertThrows(RuntimeException.class, () -> usuarioService.CrearUsuarioAdmin(nuevoAdmin));
    }
}