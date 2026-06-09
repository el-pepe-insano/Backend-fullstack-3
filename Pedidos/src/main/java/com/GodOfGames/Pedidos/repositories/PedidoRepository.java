package com.GodOfGames.Pedidos.repositories;

import com.GodOfGames.Pedidos.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Si más adelante queremos buscar los pedidos de un usuario específico:
    List<Pedido> findByUsuarioId(String usuarioId);
}