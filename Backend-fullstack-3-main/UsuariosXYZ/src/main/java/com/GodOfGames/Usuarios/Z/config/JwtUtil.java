package com.GodOfGames.Usuarios.Z.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import com.GodOfGames.Usuarios.Z.models.Usuario;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // En producción con Kubernetes, esta clave vendrá de un Secret.
    // Por ahora, generamos una clave segura automáticamente para firmar los tokens.
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // Tiempo de validez del token: 2 horas (en milisegundos)
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 2;

    public String generarToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getCorreo())
                .claim("id", usuario.getId())
                .claim("rol", usuario.getRol().name()) // Guardamos el rol dentro del token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }
}