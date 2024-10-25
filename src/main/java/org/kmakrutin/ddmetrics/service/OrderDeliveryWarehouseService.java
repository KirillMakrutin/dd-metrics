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

@Component
@RequiredArgsConstructor
public class OrderDeliveryWarehouseService implements GenericHandler<Order> {
    private final PipelineGateway pipelineGateway;
    private final OrderMetrics metrics;

    @Override
    public Object handle(Order order, MessageHeaders headers) {
        order.setOrderWaitingDeliveryWarehouse(Duration.of(System.currentTimeMillis() - headers.get("readyToPickUpWarehouseMilliseconds", Long.class), ChronoUnit.MILLIS));

        metrics.record(order, Order::getOrderWaitingDeliveryWarehouse);

        order.setStatus(OrderStatus.DELIVERING_WAREHOUSE);

        final int deliveryTime = deliverWarehouse();
        order.setOrderDeliveredWarehouse(Duration.of(deliveryTime, ChronoUnit.MILLIS));

        metrics.record(order, Order::getOrderDeliveredWarehouse);

        order.setStatus(OrderStatus.WAITING_DELIVERY_CUSTOMER);

        pipelineGateway.readyDeliveryCustomer(order, System.currentTimeMillis());

        return null;
    }

    @SneakyThrows
    private int deliverWarehouse() {
        final int deliveryTime = Utils.nextInt(5000, 10000);
        TimeUnit.MILLISECONDS.sleep(deliveryTime);

        return deliveryTime;
    }
}
