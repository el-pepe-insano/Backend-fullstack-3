package com.GodOfGames.Usuarios.Z.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.GodOfGames.Usuarios.Z.models.Rol;
import com.GodOfGames.Usuarios.Z.models.Usuario;
import com.GodOfGames.Usuarios.Z.repositories.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Verificamos si la base de datos está vacía antes de insertar
            if (usuarioRepository.count() == 0) {
                System.out.println("Iniciando protocolo de precarga de usuarios de GodOfGames, señor...");

                // CORREGIDO: Ahora usa estrictamente el correo maestro de administración
                Usuario admin = Usuario.builder()
                        .nombre("Diego Dios del requiem")
                        .correo("Diego@godofgames.com")
                        .contraseña(passwordEncoder.encode("DiegoSexy69"))
                        .rol(Rol.ADMIN)
                        .build();

                Usuario cliente = Usuario.builder()
                        .nombre("Jugador Uno")
                        .correo("cliente@godofgames.com")
                        .contraseña(passwordEncoder.encode("cliente123"))
                        .rol(Rol.CLIENTE)
                        .build();

                usuarioRepository.save(admin);
                usuarioRepository.save(cliente);

                System.out.println("Precarga completada. Usuarios base instalados y encriptados con el ADMIN maestro.");
            }
        };
    }
}