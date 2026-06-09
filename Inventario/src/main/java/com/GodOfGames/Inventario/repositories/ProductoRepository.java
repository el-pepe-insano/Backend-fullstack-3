package com.GodOfGames.Inventario.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GodOfGames.Inventario.dtos.ProductoDTO;
import com.GodOfGames.Inventario.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}