package com.GodOfGames.Usuarios.Z.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.GodOfGames.Usuarios.Z.config.RabbitMQConfig;
import com.GodOfGames.Usuarios.Z.messaging.RecuperacionEvent;
import com.GodOfGames.Usuarios.Z.models.Rol;
import com.GodOfGames.Usuarios.Z.models.Usuario;
import com.GodOfGames.Usuarios.Z.repositories.UsuarioRepository;
import com.GodOfGames.Usuarios.dto.ActualizarPerfilDTO;
import com.GodOfGames.Usuarios.dto.CambiarContrasenaDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;
    private final WebClient webClient;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          RabbitTemplate rabbitTemplate, WebClient.Builder webClientBuilder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.rabbitTemplate = rabbitTemplate;
        this.webClient = webClientBuilder.baseUrl("http://notificaciones-service:8088").build();
    }

    public Usuario registrarUsuario(Usuario nuevoUsuario) {
        log.info("Iniciando registro para: {}", nuevoUsuario.getCorreo());
        if (usuarioRepository.findByCorreo(nuevoUsuario.getCorreo()).isPresent()) {
            throw new RuntimeException("Error: El correo ya esta registrado en GodOfGames.");
        }
        String correoNormalizado = nuevoUsuario.getCorreo().trim().toLowerCase();
        if (correoNormalizado.equals("diego@godofgames.com")) {
            nuevoUsuario.setRol(Rol.ADMIN);
        } else {
            if (nuevoUsuario.getRol() == Rol.ADMIN) nuevoUsuario.setRol(Rol.CLIENTE);
            if (nuevoUsuario.getRol() == null) nuevoUsuario.setRol(Rol.CLIENTE);
        }
        nuevoUsuario.setContrasena(passwordEncoder.encode(nuevoUsuario.getContrasena()));
        nuevoUsuario.setActivo(true);
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        log.info("Usuario registrado con ID: {} y Rol: {}", usuarioGuardado.getId(), usuarioGuardado.getRol());
        return usuarioGuardado;
    }

    public Usuario iniciarSesion(String correo, String contrasenaPlana) {
        log.info("Inicio de sesion para: {}", correo);
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (!usuario.isActivo()) throw new RuntimeException("Cuenta desactivada. Contacta al administrador.");
            if (passwordEncoder.matches(contrasenaPlana, usuario.getContrasena())) {
                log.info("Autenticacion exitosa para: {}", correo);
                return usuario;
            }
        }
        throw new RuntimeException("Credenciales invalidas.");
    }

    public Usuario actualizarUsuario(Long id, Usuario datosActualizados) {
        return usuarioRepository.findById(id).map(usuarioExistente -> {
            usuarioExistente.setNombre(datosActualizados.getNombre());
            if (datosActualizados.getContrasena() != null && !datosActualizados.getContrasena().isEmpty()) {
                usuarioExistente.setContrasena(passwordEncoder.encode(datosActualizados.getContrasena()));
            }
            if (datosActualizados.getRol() == Rol.ADMIN) {
                if (usuarioExistente.getCorreo().trim().toLowerCase().equals("diego@godofgames.com")) {
                    usuarioExistente.setRol(Rol.ADMIN);
                }
            } else {
                usuarioExistente.setRol(datosActualizados.getRol());
            }
            return usuarioRepository.save(usuarioExistente);
        }).orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado."));
    }

    public Usuario actualizarPerfil(Long id, ActualizarPerfilDTO dto) {
        return usuarioRepository.findById(id).map(usuario -> {
            if (dto.getNombre() != null && !dto.getNombre().isBlank()) usuario.setNombre(dto.getNombre());

            if (dto.getFotoPerfil() != null && !dto.getFotoPerfil().isBlank());
            
            if (dto.getContrasenaNueva() != null && !dto.getContrasenaNueva().isBlank()) {
                if (!passwordEncoder.matches(dto.getContrasenaActual(), usuario.getContrasena())) {
                    throw new RuntimeException("La contrasena actual es incorrecta.");
                }
                usuario.setContrasena(passwordEncoder.encode(dto.getContrasenaNueva()));
            }
            return usuarioRepository.save(usuario);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
    }

    public Usuario toggleActivarUsuario(Long id) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setActivo(!usuario.isActivo());
            log.info("Usuario ID {} -> activo: {}", id, usuario.isActivo());
            return usuarioRepository.save(usuario);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
    }

    public void solicitarRecuperacion(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("No existe una cuenta con ese correo."));
        RecuperacionEvent event = new RecuperacionEvent(usuario.getCorreo(), usuario.getNombre());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "recuperacion.contrasena", event);
        log.info("Evento de recuperacion enviado para: {}", correo);
    }

    public void cambiarContrasenaConCodigo(CambiarContrasenaDTO dto) {
        Map respuesta = webClient.post()
                .uri("/api/notificaciones/verificar-codigo?correo={c}&codigo={k}",
                        dto.getCorreo(), dto.getCodigo())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (respuesta == null || !(Boolean) respuesta.get("valido")) {
            throw new RuntimeException("Codigo invalido o expirado.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(dto.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        usuario.setContrasena(passwordEncoder.encode(dto.getNuevaContrasena()));
        usuarioRepository.save(usuario);
        log.info("Contrasena cambiada exitosamente para: {}", dto.getCorreo());
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}
