package com.GodOfGames.Inventario.services;

import com.GodOfGames.Inventario.dtos.ActualizarProductoDTO;
import com.GodOfGames.Inventario.dtos.ProductoDTO;
import com.GodOfGames.Inventario.model.Producto;
import com.GodOfGames.Inventario.repositories.ProductoRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ImagenStorageService imagenStorageService;

    public ProductoDTO convertirADto(Producto producto) {
        return ProductoDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .imagen(producto.getImagen())
                .build();
    }

    public List<ProductoDTO> obtenerTodos() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public ProductoDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return convertirADto(producto);
    }

    public ProductoDTO guardarProducto(Producto producto, MultipartFile imagen) {
    if (imagen != null && !imagen.isEmpty()) {
        producto.setImagen(imagenStorageService.guardarImagen(imagen));
    }
    return convertirADto(productoRepository.save(producto));
    }


    @Transactional
    public ProductoDTO reservarStock(Long id, Integer cantidad) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente. Solo quedan " + producto.getStock() + " copias de " + producto.getNombre());
        }

        producto.setStock(producto.getStock() - cantidad);

        try {
            Producto productoActualizado = productoRepository.save(producto);
            ProductoDTO dto = convertirADto(productoActualizado);

            String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            String claveGenerada = String.format("GOG-%s-%s-%s",
                uuid.substring(0, 4),
                uuid.substring(4, 8),
                uuid.substring(8, 12));

            dto.setClaveJuego(claveGenerada);
            return dto;
        } catch (OptimisticLockException e) {
            throw new RuntimeException("Conflicto de concurrencia en stock de '" + producto.getNombre() + "'. Reintenta.");
        }
    }

    @Transactional
    public ProductoDTO actualizarStockDirecto(Long id, Integer nuevoStock) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        producto.setStock(nuevoStock);
        return convertirADto(productoRepository.save(producto));
    }

    @Transactional
    public ProductoDTO actualizarProducto(Long id, ActualizarProductoDTO dto, MultipartFile imagen) {
    Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

    if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
        producto.setNombre(dto.getNombre());
    }
    if (dto.getDescripcion() != null) {
        producto.setDescripcion(dto.getDescripcion());
    }
    if (dto.getPrecio() != null) {
        producto.setPrecio(dto.getPrecio());
    }
    if (dto.getStock() != null) {
        producto.setStock(dto.getStock());
    }

    if (imagen != null && !imagen.isEmpty()) {
        imagenStorageService.eliminarImagen(producto.getImagen()); // borra la anterior
        producto.setImagen(imagenStorageService.guardarImagen(imagen));
    }

       return convertirADto(productoRepository.save(producto));
    }

       
         @Transactional
          public List<Producto> ListarProductoBusqueda(String busqueda) {
             return productoRepository.buscarPorNombre(busqueda);
         }

    @Transactional
    public void eliminarProducto(Long id) {
    Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("El producto no existe con ID: " + id));
    imagenStorageService.eliminarImagen(producto.getImagen());
    productoRepository.deleteById(id);
    }
}
