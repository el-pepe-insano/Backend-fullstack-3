package com.GodOfGames.Pedidos.config;

public final class SqsConfig {

    // Los nombres de cola de SQS no admiten puntos (solo letras, numeros, guiones y guion bajo).
    public static final String QUEUE_PEDIDO_CREADO = "pedido-creado";
    public static final String QUEUE_STOCK_RESERVADO = "stock-reservado";
    public static final String QUEUE_STOCK_FALLIDO = "stock-fallido";

    private SqsConfig() {
    }
}
