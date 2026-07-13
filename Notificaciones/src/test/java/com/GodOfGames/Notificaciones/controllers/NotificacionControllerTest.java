package com.GodOfGames.Notificaciones.controllers;

import com.GodOfGames.Notificaciones.services.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificacionControllerTest {

    @Mock
    private NotificacionService notificacionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        NotificacionController controller = new NotificacionController(notificacionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void verificarCodigo_valido_retorna200() throws Exception {
        when(notificacionService.verificarCodigo("juan@test.com", "123456")).thenReturn(true);

        mockMvc.perform(post("/api/notificaciones/verificar-codigo")
                        .param("correo", "juan@test.com")
                        .param("codigo", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));
    }

    @Test
    void verificarCodigo_invalido_retorna400() throws Exception {
        when(notificacionService.verificarCodigo("juan@test.com", "000000")).thenReturn(false);

        mockMvc.perform(post("/api/notificaciones/verificar-codigo")
                        .param("correo", "juan@test.com")
                        .param("codigo", "000000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valido").value(false));
    }
}