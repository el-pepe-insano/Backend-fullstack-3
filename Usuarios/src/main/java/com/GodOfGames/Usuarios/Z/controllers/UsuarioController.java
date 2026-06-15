package com.GodOfGames.Usuarios.Z.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.GodOfGames.Usuarios.Z.Service.UsuarioService;
import com.GodOfGames.Usuarios.Z.config.JwtUtil;
import com.GodOfGames.Usuarios.Z.models.Usuario;
import com.GodOfGames.Usuarios.dto.ActualizarPerfilDTO;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@Slf4j
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public UsuarioController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/registro")
    public ResponseEntity<Usuario> registrar(@Valid @RequestBody Usuario usuario) {
        log.info("POST /api/usuarios/registro");
        Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
        nuevoUsuario.setContrasena(null);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String correo, @RequestParam String contrasena) {
        log.info("POST /api/usuarios/login");
        Usuario usuarioAutenticado = usuarioService.iniciarSesion(correo, contrasena);
        String token = jwtUtil.generarToken(usuarioAutenticado);
        usuarioAutenticado.setContrasena(null);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Bienvenido a GodOfGames.");
        respuesta.put("token", token);
        respuesta.put("usuario", usuarioAutenticado);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        log.info("PUT /api/usuarios/{}", id);
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuario);
        usuarioActualizado.setContrasena(null);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @PatchMapping("/{id}/perfil")
    public ResponseEntity<Usuario> actualizarPerfil(@PathVariable Long id, @RequestBody ActualizarPerfilDTO dto) {
        log.info("PATCH /api/usuarios/{}/perfil", id);
        Usuario actualizado = usuarioService.actualizarPerfil(id, dto);
        actualizado.setContrasena(null);
        return ResponseEntity.ok(actualizado);
    }

    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<Usuario> toggleActivo(@PathVariable Long id) {
        log.info("PATCH /api/usuarios/{}/toggle-activo", id);
        Usuario actualizado = usuarioService.toggleActivarUsuario(id);
        actualizado.setContrasena(null);
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/recuperar-contrasena")
    public ResponseEntity<Map<String, String>> recuperarContrasena(@RequestParam String correo) {
        log.info("POST /api/usuarios/recuperar-contrasena para: {}", correo);
        usuarioService.solicitarRecuperacion(correo);
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Codigo de recuperacion enviado al correo.");
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    public ResponseEntity<Iterable<Usuario>> listarTodos() {
        log.info("GET /api/usuarios");
        Iterable<Usuario> usuarios = usuarioService.listarUsuarios();
        usuarios.forEach(u -> u.setContrasena(null));
        return ResponseEntity.ok(usuarios);
    }
}
