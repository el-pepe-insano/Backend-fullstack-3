package com.GodOfGames.Usuarios.Z.controllers;

import com.GodOfGames.Usuarios.Z.Service.UsuarioService;
import com.GodOfGames.Usuarios.Z.config.JwtUtil;
import com.GodOfGames.Usuarios.Z.exceptions.GlobalExceptionHandler;
import com.GodOfGames.Usuarios.Z.models.Rol;
import com.GodOfGames.Usuarios.Z.models.Usuario;
import com.GodOfGames.Usuarios.dto.ActualizarPerfilDTO;
import com.GodOfGames.Usuarios.dto.CambiarContrasenaDTO;
import com.GodOfGames.Usuarios.dto.CambiarContrasenasincodigoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UsuarioController controller = new UsuarioController(usuarioService, jwtUtil);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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
    void registrar_exito() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setCorreo("juan@test.com");
        usuario.setContrasena("Pass123!");
        usuario.setRol(Rol.CLIENTE);

        when(usuarioService.registrarUsuario(any())).thenReturn(usuarioBase());

        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void registrar_datosInvalidos_retorna400() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setCorreo("correo-invalido");

        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearAdmin_exito() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNombre("Admin2");
        usuario.setCorreo("admin2@test.com");
        usuario.setContrasena("Clave123!");
        usuario.setRol(Rol.ADMIN);

        Usuario admin = usuarioBase();
        admin.setRol(Rol.ADMIN);
        when(usuarioService.CrearUsuarioAdmin(any())).thenReturn(admin);

        mockMvc.perform(post("/api/usuarios/registro/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isCreated());
    }

    @Test
    void login_exito() throws Exception {
        when(usuarioService.iniciarSesion("juan@test.com", "Pass123!")).thenReturn(usuarioBase());
        when(jwtUtil.generarToken(any())).thenReturn("token-falso");

        mockMvc.perform(post("/api/usuarios/login")
                        .param("correo", "juan@test.com")
                        .param("contrasena", "Pass123!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-falso"));
    }

    @Test
    void login_credencialesInvalidas_retorna400() throws Exception {
        when(usuarioService.iniciarSesion(anyString(), anyString())).thenThrow(new RuntimeException("Credenciales invalidas."));

        mockMvc.perform(post("/api/usuarios/login")
                        .param("correo", "juan@test.com")
                        .param("contrasena", "MalaClave"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_exito() throws Exception {
        ActualizarPerfilDTO dto = new ActualizarPerfilDTO();
        dto.setNombre("Juan Actualizado");
        dto.setCorreo("juan@test.com");

        when(usuarioService.actualizarUsuario(eq(1L), any())).thenReturn(usuarioBase());

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void toggleActivo_exito() throws Exception {
        Usuario desactivado = usuarioBase();
        desactivado.setActivo(false);
        when(usuarioService.toggleActivarUsuario(1L)).thenReturn(desactivado);

        mockMvc.perform(patch("/api/usuarios/1/toggle-activo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void recuperarContrasena_exito() throws Exception {
        doNothing().when(usuarioService).solicitarRecuperacion("juan@test.com");

        mockMvc.perform(post("/api/usuarios/recuperar-contrasena")
                        .param("correo", "juan@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void cambiarContrasena_exito() throws Exception {
        CambiarContrasenaDTO dto = new CambiarContrasenaDTO();
        dto.setCorreo("juan@test.com");
        dto.setCodigo("123456");
        dto.setNuevaContrasena("NuevaClave1!");

        doNothing().when(usuarioService).cambiarContrasenaConCodigo(any());

        mockMvc.perform(post("/api/usuarios/cambiar-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarContrasenaSinCodigo_exito() throws Exception {
        CambiarContrasenasincodigoDTO dto = new CambiarContrasenasincodigoDTO();
        dto.setCorreo("juan@test.com");
        dto.setContrasenaActual("ClaveVieja1!");
        dto.setNuevaContrasena("ClaveNueva1!");

        doNothing().when(usuarioService).cambiarContrasena(any());

        mockMvc.perform(post("/api/usuarios/cambiar-contrasena-sin-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void listarTodos_retornaLista() throws Exception {
        when(usuarioService.listarUsuarios()).thenReturn(List.of(usuarioBase()));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}