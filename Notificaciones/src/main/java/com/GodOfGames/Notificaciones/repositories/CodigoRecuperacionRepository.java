package com.GodOfGames.Notificaciones.repositories;

import com.GodOfGames.Notificaciones.models.CodigoRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Long> {
    Optional<CodigoRecuperacion> findByCorreoAndCodigoAndUsadoFalse(String correo, String codigo);
    void deleteByCorreo(String correo);
}