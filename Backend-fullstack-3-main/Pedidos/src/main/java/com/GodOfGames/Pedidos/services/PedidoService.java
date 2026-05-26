package com.GodOfGames.Pedidos.services;

import com.GodOfGames.Pedidos.dtos.PedidoRequestDTO;
import com.GodOfGames.Pedidos.dtos.PedidoResponseDTO;
import com.GodOfGames.Pedidos.models.EstadoPedido;

import java.util.List;

public interface PedidoService {
    PedidoResponseDTO crearPedido(PedidoRequestDTO pedidoDTO, String usuarioId, String token);
    PedidoResponseDTO obtenerPedidoPorId(Long id);
    List<PedidoResponseDTO> obtenerTodosLosPedidos();
    PedidoResponseDTO actualizarEstado(Long id, EstadoPedido nuevoEstado);
    List<PedidoResponseDTO> obtenerPedidosPorUsuario(String usuarioId);
}