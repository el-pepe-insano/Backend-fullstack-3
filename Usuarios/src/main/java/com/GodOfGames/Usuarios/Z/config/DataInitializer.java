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
            if (usuarioRepository.count() == 0) {
                System.out.println("Iniciando precarga de usuarios...");

                Usuario admin = Usuario.builder()
                        .nombre("Diego Dios del requiem")
                        .correo("diego@godofgames.com")
                        .contrasena(passwordEncoder.encode("DiegoSexy69!"))
                        .rol(Rol.ADMIN)
                        .activo(true)
                        .build();

                Usuario cliente = Usuario.builder()
                        .nombre("Jugador Uno")
                        .correo("cliente@godofgames.com")
                        .contrasena(passwordEncoder.encode("Cliente123!"))
                        .rol(Rol.CLIENTE)
                        .activo(true)
                        .build();

                usuarioRepository.save(admin);
                usuarioRepository.save(cliente);

                System.out.println("Precarga completada.");
            }
        };
    }
}
