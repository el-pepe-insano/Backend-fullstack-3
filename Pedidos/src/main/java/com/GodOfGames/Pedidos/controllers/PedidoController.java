package com.GodOfGames.Pedidos.controllers;

import com.GodOfGames.Pedidos.dtos.PedidoRequestDTO;
import com.GodOfGames.Pedidos.dtos.PedidoResponseDTO;
import com.GodOfGames.Pedidos.models.EstadoPedido;
import com.GodOfGames.Pedidos.services.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Pedidos", description = "API para la gestion transaccional de Pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Crear un nuevo pedido")
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
    @Operation(summary = "Listar pedidos de un usuario especifico")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(pedidoService.obtenerPedidosPorUsuario(usuarioId));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar el estado de un pedido")
    public ResponseEntity<PedidoResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPedido nuevoEstado,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, nuevoEstado, token));
    }

    @GetMapping("/historial")
    @Operation(summary = "Historial de ventas para el admin con filtros opcionales por fecha y estado")
    public ResponseEntity<List<PedidoResponseDTO>> historialVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.obtenerHistorial(desde, hasta, estado));
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Estadisticas de ventas para el admin")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        List<PedidoResponseDTO> todos = pedidoService.obtenerTodosLosPedidos();

        long totalPedidos = todos.size();
        long completados = todos.stream().filter(p -> p.getEstado() == EstadoPedido.COMPLETADO).count();
        long pendientes = todos.stream().filter(p -> p.getEstado() == EstadoPedido.PENDIENTE).count();
        long cancelados = todos.stream().filter(p -> p.getEstado() == EstadoPedido.CANCELADO).count();
        BigDecimal totalVentas = todos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.COMPLETADO)
                .map(PedidoResponseDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPedidos", totalPedidos);
        stats.put("completados", completados);
        stats.put("pendientes", pendientes);
        stats.put("cancelados", cancelados);
        stats.put("totalVentas", totalVentas);

        return ResponseEntity.ok(stats);
    }
}
