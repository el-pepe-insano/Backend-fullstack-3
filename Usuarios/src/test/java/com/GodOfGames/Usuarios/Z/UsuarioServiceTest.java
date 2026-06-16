package com.GodOfGames.Usuarios.Z;

import com.GodOfGames.Usuarios.Z.Service.UsuarioService;
import com.GodOfGames.Usuarios.Z.models.Rol;
import com.GodOfGames.Usuarios.Z.models.Usuario;
import com.GodOfGames.Usuarios.Z.repositories.UsuarioRepository;
import com.GodOfGames.Usuarios.dto.ActualizarPerfilDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registrarUsuario_exitoso() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setCorreo("juan@test.com");
        usuario.setContrasena("Pass123!");
        usuario.setRol(Rol.CLIENTE);

        when(usuarioRepository.findByCorreo(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(usuarioRepository.save(any())).thenReturn(usuario);

        Usuario resultado = usuarioService.registrarUsuario(usuario);
        assertNotNull(resultado);
        assertEquals(Rol.CLIENTE, resultado.getRol());
    }

    @Test
    void registrarUsuario_correoExistente_lanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("juan@test.com");

        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(RuntimeException.class, () -> usuarioService.registrarUsuario(usuario));
    }

    @Test
    void registrarAdmin_correoMaestro() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Diego");
        usuario.setCorreo("diego@godofgames.com");
        usuario.setContrasena("Admin123!");

        when(usuarioRepository.findByCorreo(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Usuario resultado = usuarioService.registrarUsuario(usuario);
        assertEquals(Rol.ADMIN, resultado.getRol());
    }

    @Test
    void iniciarSesion_exitoso() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("juan@test.com");
        usuario.setContrasena("hashed");
        usuario.setActivo(true);

        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Pass123!", "hashed")).thenReturn(true);

        Usuario resultado = usuarioService.iniciarSesion("juan@test.com", "Pass123!");
        assertNotNull(resultado);
    }

    @Test
    void iniciarSesion_usuarioDesactivado_lanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("juan@test.com");
        usuario.setContrasena("hashed");
        usuario.setActivo(false);

        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(RuntimeException.class, () -> usuarioService.iniciarSesion("juan@test.com", "Pass123!"));
    }

    @Test
    void toggleActivarUsuario_desactiva() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setActivo(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Usuario resultado = usuarioService.toggleActivarUsuario(1L);
        assertFalse(resultado.isActivo());
    }

}
