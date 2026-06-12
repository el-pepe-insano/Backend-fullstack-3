package com.GodOfGames.Usuarios.dto;

import lombok.Data;

@Data
public class ActualizarPerfilDTO {
    private String nombre;
    private String fotoPerfil;
    private String contrasenaNueva;
    private String contrasenaActual;
}
