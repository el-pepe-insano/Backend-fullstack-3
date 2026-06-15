package com.GodOfGames.Notificaciones.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecuperacionEvent {
    private String correo;
    private String nombre;
}