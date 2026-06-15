package com.GodOfGames.Notificaciones.controllers;

import com.GodOfGames.Notificaciones.services.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @PostMapping("/verificar-codigo")
    public ResponseEntity<Map<String, Object>> verificarCodigo(
            @RequestParam String correo,
            @RequestParam String codigo) {

        boolean valido = notificacionService.verificarCodigo(correo, codigo);

        if (valido) {
            return ResponseEntity.ok(Map.of(
                "valido", true,
                "mensaje", "Código verificado correctamente"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "valido", false,
                "mensaje", "Código inválido o expirado"
            ));
        }
    }
}