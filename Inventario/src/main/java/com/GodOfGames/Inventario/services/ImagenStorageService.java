package com.GodOfGames.Inventario.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ImagenStorageService {

    private final Path rootLocation = Paths.get("uploads/productos");

    public ImagenStorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads/productos", e);
        }
    }

    public String guardarImagen(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("El archivo de imagen está vacío");
        }
        String extension = obtenerExtension(file.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID() + extension;

        try {
            Path destino = rootLocation.resolve(nombreArchivo);
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return nombreArchivo;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen", e);
        }
    }

    public void eliminarImagen(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) return;
        try {
            Files.deleteIfExists(rootLocation.resolve(nombreArchivo));
        } catch (IOException e) {
            // Log si quieres, pero no debería tumbar la operación principal
        }
    }

    private String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) return "";
        return nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
    }
}