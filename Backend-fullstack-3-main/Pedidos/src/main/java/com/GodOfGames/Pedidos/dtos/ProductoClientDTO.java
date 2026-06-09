package com.GodOfGames.Pedidos.dtos;

import lombok.Data;

@Data
public class ProductoClientDTO {
    private Long id;
    private String nombre;
    private Integer stock;
    private Double precio;
    private String claveJuego; 
}