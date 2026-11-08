package com.GodOfGames.Inventario.config;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsConfig {

    // Los nombres de cola de SQS no admiten puntos (solo letras, numeros, guiones y guion bajo).
    public static final String QUEUE_PEDIDO_CREADO = "pedido-creado";
    public static final String QUEUE_STOCK_RESERVADO = "stock-reservado";
    public static final String QUEUE_STOCK_FALLIDO = "stock-fallido";

    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.builder()
                .sqsAsyncClient(sqsAsyncClient)
                // No se envia el header "JavaType" con el nombre de la clase del payload: productor y
                // consumidor viven en microservicios distintos (paquetes distintos), asi el consumidor
                // deserializa segun el tipo del parametro de su metodo @SqsListener.
                .configureDefaultConverter(converter -> converter.setPayloadTypeHeaderValueFunction(message -> null))
                .build();
    }
}
