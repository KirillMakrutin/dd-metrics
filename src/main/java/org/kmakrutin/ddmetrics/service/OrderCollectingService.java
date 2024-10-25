package org.kmakrutin.ddmetrics.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.kmakrutin.ddmetrics.gateway.PipelineGateway;
import org.kmakrutin.ddmetrics.metrics.OrderMetrics;
import org.kmakrutin.ddmetrics.model.Order;
import org.kmakrutin.ddmetrics.model.OrderStatus;
import org.kmakrutin.ddmetrics.order.Utils;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class OrderCollectingService implements GenericHandler<Order> {
    private final PipelineGateway pipelineGateway;
    private final OrderMetrics metrics;


    @Override
    public Order handle(Order order, MessageHeaders headers) {
        order.setOrderWaitingCollection(Duration.of(System.currentTimeMillis() - headers.get("readyToCollectMilliseconds", Long.class), ChronoUnit.MILLIS));

        metrics.record(order, Order::getOrderWaitingCollection);

        order.setStatus(OrderStatus.COLLECTING);

        int collectingTime = collecting();
        order.setOrderCollected(Duration.of(collectingTime, ChronoUnit.MILLIS));

        metrics.record(order, Order::getOrderCollected);

        order.setStatus(OrderStatus.WAITING_DELIVERY_WAREHOUSE);

        pipelineGateway.readyDeliveryWarehouse(order, System.currentTimeMillis());

        return null;
    }

    @SneakyThrows
    private int collecting() {
        final int sleepTime = Utils.nextInt(1000, 3000);
        TimeUnit.MILLISECONDS.sleep(sleepTime);

        return sleepTime;
    }
}
