package com.GodOfGames.Pedidos.dtos;

import com.GodOfGames.Pedidos.models.EstadoPedido;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PedidoResponseDTO {
    private Long id;
    private String usuarioId;
    private LocalDateTime fechaCreacion;
    private EstadoPedido estado;
    private BigDecimal total;
    private List<DetalleResponseDTO> detalles;

    @Data
    @Builder
    public static class DetalleResponseDTO {
        private Long id;
        private Long productoId;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private String claveJuego; 
    }
}