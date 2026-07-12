package com.GodOfGames.Pedidos.services;

import com.GodOfGames.Pedidos.dtos.PedidoRequestDTO;
import com.GodOfGames.Pedidos.dtos.PedidoResponseDTO;
import com.GodOfGames.Pedidos.dtos.ProductoClientDTO;
import com.GodOfGames.Pedidos.exceptions.ResourceNotFoundException;
import com.GodOfGames.Pedidos.models.DetallePedido;
import com.GodOfGames.Pedidos.models.EstadoPedido;
import com.GodOfGames.Pedidos.models.Pedido;
import com.GodOfGames.Pedidos.repositories.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final WebClient webClient;

    @Value("${inventario.service.url:http://inventario-service:8082}")
    private String inventarioServiceUrl;

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(PedidoRequestDTO pedidoDTO, String usuarioId, String token) {
        Pedido pedido = Pedido.builder().usuarioId(usuarioId).fechaCreacion(LocalDateTime.now()).estado(EstadoPedido.PENDIENTE).build();
        BigDecimal total = BigDecimal.ZERO;
        for (PedidoRequestDTO.DetalleRequestDTO d : pedidoDTO.getDetalles()) {
            pedido.addDetalle(DetallePedido.builder().productoId(d.getProductoId()).cantidad(d.getCantidad()).precioUnitario(d.getPrecioUnitario()).claveJuego(null).build());
            total = total.add(d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())));
        }
        pedido.setTotal(total);
        return mapear(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoResponseDTO actualizarEstado(Long id, EstadoPedido nuevoEstado, String token) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id));
        if (nuevoEstado == EstadoPedido.COMPLETADO && pedido.getEstado() != EstadoPedido.COMPLETADO) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                try {
                    ProductoClientDTO p = webClient.post().uri(inventarioServiceUrl + "/api/productos/" + detalle.getProductoId() + "/reservar?cantidad=" + detalle.getCantidad()).header("Authorization", "Bearer " + token).retrieve().bodyToMono(ProductoClientDTO.class).block();
                    if (p != null) detalle.setClaveJuego(p.getClaveJuego());
                } catch (Exception e) {
                    throw new RuntimeException("Error de stock en juego ID " + detalle.getProductoId());
                }
            }
        }
        pedido.setEstado(nuevoEstado);
        return mapear(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPedidoPorId(Long id) {
        return mapear(pedidoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> obtenerTodosLosPedidos() {
        return pedidoRepository.findAll().stream().map(this::mapear).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> obtenerPedidosPorUsuario(String usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId).stream().map(this::mapear).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> obtenerHistorial(LocalDateTime desde, LocalDateTime hasta, EstadoPedido estado) {
        return pedidoRepository.findHistorial(desde, hasta, estado).stream().map(this::mapear).collect(Collectors.toList());
    }

    private PedidoResponseDTO mapear(Pedido pedido) {
        List<PedidoResponseDTO.DetalleResponseDTO> detalles = pedido.getDetalles().stream().map(d -> PedidoResponseDTO.DetalleResponseDTO.builder().id(d.getId()).productoId(d.getProductoId()).cantidad(d.getCantidad()).precioUnitario(d.getPrecioUnitario()).claveJuego(d.getClaveJuego()).build()).collect(Collectors.toList());
        return PedidoResponseDTO.builder().id(pedido.getId()).usuarioId(pedido.getUsuarioId()).fechaCreacion(pedido.getFechaCreacion()).estado(pedido.getEstado()).total(pedido.getTotal()).detalles(detalles).build();
    }
}