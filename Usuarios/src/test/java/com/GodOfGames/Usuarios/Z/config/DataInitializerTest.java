package com.GodOfGames.Usuarios.Z.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import com.GodOfGames.Usuarios.Z.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DataInitializerTest {

    @Test
    void initData_baseVacia_precargaUsuarios() throws Exception {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repo.count()).thenReturn(0L);
        when(encoder.encode(anyString())).thenReturn("hashed");

        DataInitializer initializer = new DataInitializer();
        CommandLineRunner runner = initializer.initData(repo, encoder);
        runner.run();

        verify(repo, times(3)).save(any());
    }

    @Test
    void initData_baseConDatos_noHacePrecarga() throws Exception {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repo.count()).thenReturn(3L);

        DataInitializer initializer = new DataInitializer();
        CommandLineRunner runner = initializer.initData(repo, encoder);
        runner.run();

        verify(repo, never()).save(any());
    }
}