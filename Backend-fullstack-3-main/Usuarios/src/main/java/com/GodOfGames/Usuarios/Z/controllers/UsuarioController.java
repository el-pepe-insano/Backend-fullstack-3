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
    private final JwtUtil jwtUtil; // Inyectamos nuestra fábrica de tokens

    public UsuarioController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    // Agregamos @Valid para que Spring Boot verifique las reglas del modelo antes de procesar
    @PostMapping("/registro")
    public ResponseEntity<Usuario> registrar(@Valid @RequestBody Usuario usuario) {
        log.info("Petición HTTP POST recibida en /api/usuarios/registro");
        Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
        nuevoUsuario.setContraseña(null); 
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String correo, @RequestParam String contraseña) {
        log.info("Petición HTTP POST recibida en /api/usuarios/login");
        Usuario usuarioAutenticado = usuarioService.iniciarSesion(correo, contraseña);
        
        // Generamos el pase VIP
        String token = jwtUtil.generarToken(usuarioAutenticado);
        usuarioAutenticado.setContraseña(null); 
        
        // Devolvemos el usuario y su Token
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Bienvenido a GodOfGames, señor.");
        respuesta.put("token", token);
        respuesta.put("usuario", usuarioAutenticado);
        
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        log.info("Petición HTTP PUT recibida en /api/usuarios/{}", id);
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuario);
        usuarioActualizado.setContraseña(null);
        return ResponseEntity.ok(usuarioActualizado);
    }
    @GetMapping
    public ResponseEntity<Iterable<Usuario>> listarTodos() {
        log.info("Petición HTTP GET recibida en /api/usuarios para listar todos");
        Iterable<Usuario> usuarios = usuarioService.listarUsuarios();
        // Opcional: limpiar contraseñas por seguridad
        usuarios.forEach(u -> u.setContraseña(null));
        return ResponseEntity.ok(usuarios);
    }
    @PatchMapping("/{id}/perfil")
    public ResponseEntity<Usuario> actualizarPerfil(@PathVariable Long id,
                                                 @RequestBody ActualizarPerfilDTO dto) {
    Usuario actualizado = usuarioService.actualizarPerfil(id, dto);
    actualizado.setContraseña(null);
    return ResponseEntity.ok(actualizado);
}

   @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<Usuario> toggleActivo(@PathVariable Long id) {
    Usuario actualizado = usuarioService.toggleActivarUsuario(id);
    actualizado.setContraseña(null);
    return ResponseEntity.ok(actualizado);
}
}