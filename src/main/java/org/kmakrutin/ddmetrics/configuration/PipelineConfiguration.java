package org.kmakrutin.ddmetrics.configuration;

import lombok.extern.slf4j.Slf4j;
import org.kmakrutin.ddmetrics.service.OrderCollectingService;
import org.kmakrutin.ddmetrics.service.OrderDeliveryCustomerService;
import org.kmakrutin.ddmetrics.service.OrderDeliveryWarehouseService;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

@Slf4j
@IntegrationComponentScan(basePackages = "org.kmakrutin.ddmetrics.gateway")
@Configuration
public class PipelineConfiguration {

    @Bean(name = "orderCollecting")
    public MessageChannel orderCollecting(ConnectionFactory connectionFactory) {
        return Amqp.channel("orders.collecting", connectionFactory).concurrentConsumers(2).prefetchCount(1).get();
    }

    @Bean(name = "orderDeliveryWarehouse")
    public MessageChannel orderDeliveryWarehouse(ConnectionFactory connectionFactory) {
        return Amqp.channel("orders.delivery.warehouse", connectionFactory).concurrentConsumers(5).prefetchCount(1).get();
    }

    @Bean(name = "orderDeliveryCustomer")
    public MessageChannel orderDeliveryCustomer(ConnectionFactory connectionFactory) {
        return Amqp.channel("orders.delivery.customer", connectionFactory).concurrentConsumers(3).prefetchCount(1).get();
    }

    @Bean
    public IntegrationFlow orderCollectingFlow(OrderCollectingService orderCollectingService) {
        return IntegrationFlows.from("orderCollecting")
                .handle(orderCollectingService)
                .get();
    }

    @Bean
    public IntegrationFlow orderDeliveryWarehouseFlow(OrderDeliveryWarehouseService orderDeliveryWarehouseService) {
        return IntegrationFlows.from("orderDeliveryWarehouse")
                .handle(orderDeliveryWarehouseService)
                .get();
    }

    @Bean
    public IntegrationFlow orderDeliveryCustomerFlow(OrderDeliveryCustomerService orderDeliveryCustomerService) {
        return IntegrationFlows.from("orderDeliveryCustomer")
                .handle(orderDeliveryCustomerService)
                .get();
    }
}
