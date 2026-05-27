package com.GodOfGames.Inventario.controllers;

import com.GodOfGames.Inventario.dtos.ProductoDTO;
import com.GodOfGames.Inventario.model.Producto;
import com.GodOfGames.Inventario.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // GET: http://localhost:8082/api/productos
    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarTodos() {
        // Ahora devuelve una lista de DTOs, no de Entidades
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    // GET: http://localhost:8082/api/productos/1
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    // POST: http://localhost:8082/api/productos
    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@RequestBody Producto producto) {
        // Recibe los datos, los guarda y devuelve la versión limpia (DTO)
        return ResponseEntity.ok(productoService.guardarProducto(producto));
    }

   // POST: http://localhost:8082/api/productos/1/reservar?cantidad=2
    @PostMapping("/{id}/reservar")
    public ResponseEntity<ProductoDTO> reservarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        // Si hay error de stock, salta al GlobalExceptionHandler automáticamente
        ProductoDTO productoActualizado = productoService.reservarStock(id, cantidad);
        return ResponseEntity.ok(productoActualizado);
    }
    }
