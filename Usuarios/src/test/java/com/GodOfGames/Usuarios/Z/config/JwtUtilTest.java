package com.GodOfGames.Usuarios.Z.config;

import com.GodOfGames.Usuarios.Z.models.Rol;
import com.GodOfGames.Usuarios.Z.models.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "requiemElMejorJuegoDeGodOfGames2026SecretKeyParaJWT12345678901234";

    @Test
    void generarToken_contieneClaimsCorrectos() {
        JwtUtil jwtUtil = new JwtUtil();

        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setCorreo("juan@test.com");
        usuario.setRol(Rol.ADMIN);

        String token = jwtUtil.generarToken(usuario);
        assertNotNull(token);

        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

        assertEquals("juan@test.com", claims.getSubject());
        assertEquals(5, claims.get("id", Integer.class));
        assertEquals("ADMIN", claims.get("rol", String.class));
    }
}