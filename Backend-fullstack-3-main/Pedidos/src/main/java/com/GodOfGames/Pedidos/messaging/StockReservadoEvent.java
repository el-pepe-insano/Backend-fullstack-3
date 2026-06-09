package com.GodOfGames.Pedidos.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservadoEvent {
    private Long pedidoId;
    private boolean exitoso;
    private String mensaje;
}