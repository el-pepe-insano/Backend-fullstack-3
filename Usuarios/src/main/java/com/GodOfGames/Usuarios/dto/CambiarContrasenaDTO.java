package com.GodOfGames.Usuarios.dto;

import lombok.Data;

@Data
public class CambiarContrasenaDTO {
    private String correo;
    private String codigo;
    private String nuevaContrasena;
}
