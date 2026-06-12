package com.GodOfGames.Usuarios.Z.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio.")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "El formato del correo electronico no es valido.")
    @Column(nullable = false, unique = true)
    private String correo;

    @NotBlank(message = "La contrasena es obligatoria.")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
        message = "La contrasena debe tener minimo 8 caracteres, al menos un numero y un caracter especial"
    )
    @Column(nullable = false)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Column
    private String fotoPerfil;
}
