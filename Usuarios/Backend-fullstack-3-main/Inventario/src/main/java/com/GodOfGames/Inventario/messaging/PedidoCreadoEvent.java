package com.GodOfGames.Inventario.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCreadoEvent {
    private Long pedidoId;
    private String usuarioId;
    private List<DetalleEvent> detalles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleEvent {
        private Long productoId;
        private Integer cantidad;
    }
}