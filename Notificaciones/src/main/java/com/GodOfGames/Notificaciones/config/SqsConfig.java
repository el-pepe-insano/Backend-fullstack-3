package com.GodOfGames.Notificaciones.config;

public final class SqsConfig {

    // Los nombres de cola de SQS no admiten puntos (solo letras, numeros, guiones y guion bajo).
    public static final String QUEUE_RECUPERACION = "recuperacion-contrasena";

    private SqsConfig() {
    }
}
