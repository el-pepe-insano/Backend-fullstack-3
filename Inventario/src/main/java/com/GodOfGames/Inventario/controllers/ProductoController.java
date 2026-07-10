package com.GodOfGames.Inventario.controllers;

import com.GodOfGames.Inventario.dtos.ActualizarProductoDTO;
import com.GodOfGames.Inventario.dtos.ProductoDTO;
import com.GodOfGames.Inventario.model.Producto;
import com.GodOfGames.Inventario.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarTodos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @GetMapping("/buscar/{Busqueda}")
    public ResponseEntity<List<Producto>> listarPorNombre(@PathVariable String Busqueda) {
        return ResponseEntity.ok(productoService.ListarProductoBusqueda(Busqueda));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

   @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ProductoDTO> crearProducto(
        @RequestPart("producto") Producto producto,
        @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
    return ResponseEntity.ok(productoService.guardarProducto(producto, imagen));
    }

    @PostMapping("/{id}/reservar")
    public ResponseEntity<ProductoDTO> reservarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        return ResponseEntity.ok(productoService.reservarStock(id, cantidad));
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<ProductoDTO> actualizarStockAdmin(@PathVariable Long id, @RequestParam Integer nuevoStock) {
        return ResponseEntity.ok(productoService.actualizarStockDirecto(id, nuevoStock));
    }

   @PatchMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ProductoDTO> actualizarProducto(
        @PathVariable Long id,
        @RequestPart("producto") ActualizarProductoDTO dto,
        @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
    return ResponseEntity.ok(productoService.actualizarProducto(id, dto, imagen));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
