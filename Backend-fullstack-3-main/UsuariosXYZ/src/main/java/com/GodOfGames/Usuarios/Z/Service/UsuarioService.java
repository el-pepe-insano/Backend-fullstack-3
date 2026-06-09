package com.GodOfGames.Usuarios.Z.Service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.GodOfGames.Usuarios.Z.models.Usuario;
import com.GodOfGames.Usuarios.Z.repositories.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j // Activamos el sistema de Logs de Lombok
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarUsuario(Usuario nuevoUsuario) {
        log.info("Iniciando protocolo de registro para el correo: {}", nuevoUsuario.getCorreo());

        if (usuarioRepository.findByCorreo(nuevoUsuario.getCorreo()).isPresent()) {
            log.warn("Intento de registro fallido. El correo {} ya existe en GodOfGames.", nuevoUsuario.getCorreo());
            throw new RuntimeException("Error: El correo ya está registrado en GodOfGames.");
        }
        
        nuevoUsuario.setContraseña(passwordEncoder.encode(nuevoUsuario.getContraseña()));
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        
        log.info("Operación exitosa. Usuario registrado con ID: {}", usuarioGuardado.getId());
        return usuarioGuardado;
    }

    public Usuario iniciarSesion(String correo, String contraseñaPlana) {
        log.info("Procesando solicitud de inicio de sesión para: {}", correo);
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (passwordEncoder.matches(contraseñaPlana, usuario.getContraseña())) {
                log.info("Autenticación exitosa para el usuario: {}", correo);
                return usuario;
            }
        }
        
        log.error("Brecha de seguridad detectada: Credenciales inválidas para {}", correo);
        throw new RuntimeException("Credenciales inválidas, señor.");
    }

    public Usuario actualizarUsuario(Long id, Usuario datosActualizados) {
        log.info("Solicitud para actualizar el perfil del usuario con ID: {}", id);
        
        return usuarioRepository.findById(id).map(usuarioExistente -> {
            usuarioExistente.setNombre(datosActualizados.getNombre());
            
            if (datosActualizados.getContraseña() != null && !datosActualizados.getContraseña().isEmpty()) {
                log.info("Actualizando credenciales (contraseña) para el usuario ID: {}", id);
                usuarioExistente.setContraseña(passwordEncoder.encode(datosActualizados.getContraseña()));
            }
            
            usuarioExistente.setRol(datosActualizados.getRol());
            Usuario actualizado = usuarioRepository.save(usuarioExistente);
            
            log.info("Perfil del usuario ID: {} actualizado correctamente.", id);
            return actualizado;
            
        }).orElseThrow(() -> {
            log.warn("Fallo al actualizar. Usuario con ID {} no encontrado.", id);
            return new RuntimeException("Error: Usuario no encontrado :(.");
        });
    }
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}