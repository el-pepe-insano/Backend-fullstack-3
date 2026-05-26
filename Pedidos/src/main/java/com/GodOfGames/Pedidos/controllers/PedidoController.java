package com.GodOfGames.Pedidos.controllers;

import com.GodOfGames.Pedidos.dtos.PedidoRequestDTO;
import com.GodOfGames.Pedidos.dtos.PedidoResponseDTO;
import com.GodOfGames.Pedidos.models.EstadoPedido;
import com.GodOfGames.Pedidos.services.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "API para la gestión transaccional de Pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Crear un nuevo pedido", description = "Recibe una lista de productos y registra la intención de compra")
    public ResponseEntity<PedidoResponseDTO> crearPedido(
            @Valid @RequestBody PedidoRequestDTO pedidoDTO,
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader) {

        String usuarioId = authentication.getName();
        String token = authHeader.replace("Bearer ", "");

        PedidoResponseDTO response = pedidoService.crearPedido(pedidoDTO, usuarioId, token);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pedido por su ID")
    public ResponseEntity<PedidoResponseDTO> obtenerPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPedidoPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar todos los pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(pedidoService.obtenerTodosLosPedidos());
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar pedidos de un usuario específico")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(pedidoService.obtenerPedidosPorUsuario(usuarioId));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar el estado de un pedido (Ej: de PENDIENTE a COMPLETADO)")
    public ResponseEntity<PedidoResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPedido nuevoEstado) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, nuevoEstado));
    }
}