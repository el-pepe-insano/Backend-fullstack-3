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
@CrossOrigin(origins = "*") // Añadimos esto para evitar problemas de CORS con tu Frontend en React
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // GET: http://localhost:8082/api/productos
    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarTodos() {
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
        return ResponseEntity.ok(productoService.guardarProducto(producto));
    }

    // POST: http://localhost:8082/api/productos/1/reservar?cantidad=2
    @PostMapping("/{id}/reservar")
    public ResponseEntity<ProductoDTO> reservarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        ProductoDTO productoActualizado = productoService.reservarStock(id, cantidad);
        return ResponseEntity.ok(productoActualizado);
    }

    // PUT: http://localhost:8082/api/productos/1/stock?nuevoStock=50
    @PutMapping("/{id}/stock")
    public ResponseEntity<ProductoDTO> actualizarStockAdmin(@PathVariable Long id, @RequestParam Integer nuevoStock) {
        // Llama al servicio para sobreescribir el stock directamente
        ProductoDTO productoActualizado = productoService.actualizarStockDirecto(id, nuevoStock);
        return ResponseEntity.ok(productoActualizado);
    }

    // DELETE: http://localhost:8082/api/productos/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build(); // Devuelve un estado 204 (Todo OK, sin contenido que mostrar)
    }
}