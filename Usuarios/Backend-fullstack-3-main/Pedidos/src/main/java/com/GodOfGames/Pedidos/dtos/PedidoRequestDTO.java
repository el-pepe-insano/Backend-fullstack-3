package com.GodOfGames.Pedidos.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PedidoRequestDTO {

    @NotEmpty(message = "El pedido debe contener al menos un detalle")
    private List<DetalleRequestDTO> detalles;

    @Data
    public static class DetalleRequestDTO {
        @NotNull(message = "El ID del producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        private Integer cantidad;

        @NotNull(message = "El precio unitario es obligatorio")
        private BigDecimal precioUnitario;
    }
}