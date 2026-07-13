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
@Tag(name = "Pedidos", description = "API para la gestion de Pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@Valid @RequestBody PedidoRequestDTO pedidoDTO, Authentication authentication, @RequestHeader("Authorization") String authHeader) {
        return new ResponseEntity<>(pedidoService.crearPedido(pedidoDTO, authentication.getName(), authHeader.replace("Bearer ", "")), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtenerPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPedidoPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(pedidoService.obtenerTodosLosPedidos());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(pedidoService.obtenerPedidosPorUsuario(usuarioId));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> actualizarEstado(@PathVariable Long id, @RequestParam EstadoPedido nuevoEstado, @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, nuevoEstado, authHeader.replace("Bearer ", "")));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<PedidoResponseDTO>> historialVentas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta, @RequestParam(required = false) EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.obtenerHistorial(desde, hasta, estado));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        List<PedidoResponseDTO> todos = pedidoService.obtenerTodosLosPedidos();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPedidos", todos.size());
        stats.put("completados", todos.stream().filter(p -> p.getEstado() == EstadoPedido.COMPLETADO).count());
        stats.put("pendientes", todos.stream().filter(p -> p.getEstado() == EstadoPedido.PENDIENTE).count());
        stats.put("cancelados", todos.stream().filter(p -> p.getEstado() == EstadoPedido.CANCELADO).count());
        stats.put("totalVentas", todos.stream().filter(p -> p.getEstado() == EstadoPedido.COMPLETADO).map(PedidoResponseDTO::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        return ResponseEntity.ok(stats);
    }
}