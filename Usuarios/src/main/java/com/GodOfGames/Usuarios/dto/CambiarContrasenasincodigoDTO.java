package com.GodOfGames.Usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CambiarContrasenasincodigoDTO {

    @NotBlank(message = "El correo es obligatorio.")
    private String correo;

    @NotBlank(message = "La contraseña actual es obligatoria.")
    private String contrasenaActual;

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
        message = "La contrasena debe tener minimo 8 caracteres, al menos un numero y un caracter especial"
    )
    private String nuevaContrasena;
}