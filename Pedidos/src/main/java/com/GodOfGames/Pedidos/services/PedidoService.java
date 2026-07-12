package com.GodOfGames.Pedidos.services;

import com.GodOfGames.Pedidos.dtos.PedidoRequestDTO;
import com.GodOfGames.Pedidos.dtos.PedidoResponseDTO;
import com.GodOfGames.Pedidos.models.EstadoPedido;
import java.time.LocalDateTime;
import java.util.List;

public interface PedidoService {
    PedidoResponseDTO crearPedido(PedidoRequestDTO pedidoDTO, String usuarioId, String token);
    PedidoResponseDTO obtenerPedidoPorId(Long id);
    List<PedidoResponseDTO> obtenerTodosLosPedidos();
    PedidoResponseDTO actualizarEstado(Long id, EstadoPedido nuevoEstado, String token);
    List<PedidoResponseDTO> obtenerPedidosPorUsuario(String usuarioId);
    List<PedidoResponseDTO> obtenerHistorial(LocalDateTime desde, LocalDateTime hasta, EstadoPedido estado);
}