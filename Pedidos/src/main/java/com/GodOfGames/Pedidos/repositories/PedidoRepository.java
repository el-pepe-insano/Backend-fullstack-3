package com.GodOfGames.Pedidos.repositories;

import com.GodOfGames.Pedidos.models.EstadoPedido;
import com.GodOfGames.Pedidos.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioId(String usuarioId);

    @Query("SELECT p FROM Pedido p WHERE " +
           "(:desde IS NULL OR p.fechaCreacion >= :desde) AND " +
           "(:hasta IS NULL OR p.fechaCreacion <= :hasta) AND " +
           "(:estado IS NULL OR p.estado = :estado) " +
           "ORDER BY p.fechaCreacion DESC")
    List<Pedido> findHistorial(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("estado") EstadoPedido estado);
}
