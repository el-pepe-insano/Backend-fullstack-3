package com.GodOfGames.Inventario.services;

import com.GodOfGames.Inventario.dtos.ProductoDTO;
import com.GodOfGames.Inventario.model.Producto;
import com.GodOfGames.Inventario.repositories.ProductoRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public ProductoDTO convertirADto(Producto producto) {
        return ProductoDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
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
                .orElseThrow(() -> new RuntimeException("Error: Producto no encontrado con el ID: " + id));
        return convertirADto(producto);
    }

    public ProductoDTO guardarProducto(Producto producto) {
        Producto productoGuardado = productoRepository.save(producto);
        return convertirADto(productoGuardado);
    }

    @Transactional
    public ProductoDTO reservarStock(Long id, Integer cantidad) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Producto no encontrado con el ID: " + id));

        if (producto.getStock() < cantidad) {
            throw new RuntimeException("¡Stock insuficiente! Solo quedan " + producto.getStock() + " copias de " + producto.getNombre());
        }

        producto.setStock(producto.getStock() - cantidad);

        try {
            Producto productoActualizado = productoRepository.save(producto);
            ProductoDTO dto = convertirADto(productoActualizado);
            
            // Generar GameKey única (Formato: GOG-XXXX-XXXX-XXXX)
            String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            String claveGenerada = String.format("GOG-%s-%s-%s", 
                uuid.substring(0, 4), 
                uuid.substring(4, 8), 
                uuid.substring(8, 12));
            
            dto.setClaveJuego(claveGenerada);
            
            return dto;
        } catch (OptimisticLockException e) {
            throw new RuntimeException("Conflicto de concurrencia: el stock de '" + producto.getNombre() +
                    "' fue modificado por otra operación simultánea. Por favor, reintenta.");
        }
    }
}