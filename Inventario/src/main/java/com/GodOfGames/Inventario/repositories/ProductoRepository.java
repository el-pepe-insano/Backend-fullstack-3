package com.GodOfGames.Inventario.repositories;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.GodOfGames.Inventario.dtos.ProductoDTO;
import com.GodOfGames.Inventario.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

     @Query("SELECT p FROM Producto p WHERE " +
       "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
List<Producto> buscarPorNombre(@Param("busqueda") String busqueda);
}