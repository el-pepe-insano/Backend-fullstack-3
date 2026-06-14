package com.GodOfGames.Inventario.dtos;

import lombok.Data;

@Data
public class ActualizarProductoDTO {
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
}
