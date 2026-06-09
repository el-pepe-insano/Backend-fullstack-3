package com.GodOfGames.Pedidos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_PEDIDO_CREADO = "pedido.creado";
    public static final String QUEUE_STOCK_RESERVADO = "stock.reservado";
    public static final String QUEUE_STOCK_FALLIDO = "stock.fallido";
    public static final String EXCHANGE = "godofgames.exchange";

    @Bean
    public Queue queuePedidoCreado() {
        return new Queue(QUEUE_PEDIDO_CREADO, true);
    }

    @Bean
    public Queue queueStockReservado() {
        return new Queue(QUEUE_STOCK_RESERVADO, true);
    }

    @Bean
    public Queue queueStockFallido() {
        return new Queue(QUEUE_STOCK_FALLIDO, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding bindingPedidoCreado(Queue queuePedidoCreado, TopicExchange exchange) {
        return BindingBuilder.bind(queuePedidoCreado).to(exchange).with(QUEUE_PEDIDO_CREADO);
    }

    @Bean
    public Binding bindingStockReservado(Queue queueStockReservado, TopicExchange exchange) {
        return BindingBuilder.bind(queueStockReservado).to(exchange).with(QUEUE_STOCK_RESERVADO);
    }

    @Bean
    public Binding bindingStockFallido(Queue queueStockFallido, TopicExchange exchange) {
        return BindingBuilder.bind(queueStockFallido).to(exchange).with(QUEUE_STOCK_FALLIDO);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}