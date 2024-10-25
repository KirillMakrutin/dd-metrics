package org.kmakrutin.ddmetrics.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderDeliveryCustomerService implements GenericHandler<Order> {
    private final OrderMetrics metrics;

    @Override
    public Object handle(Order order, MessageHeaders headers) {
        order.setOrderWaitingDeliveryCustomer(Duration.of(System.currentTimeMillis() - headers.get("readyToPickUpCustomerMilliseconds", Long.class), ChronoUnit.MILLIS));

        metrics.record(order, Order::getOrderWaitingDeliveryCustomer);

        order.setStatus(OrderStatus.DELIVERING_CUSTOMER);

        final int deliveryTime = deliveryCustomer();
        order.setOrderDeliveredCustomer(Duration.of(deliveryTime, ChronoUnit.MILLIS));

        log.info("Order delivered: {}", order);

        metrics.record(order, Order::getOrderDeliveredCustomer);

        return null;
    }

    @SneakyThrows
    private int deliveryCustomer() {
        final int deliveryTime = Utils.nextInt(3000, 5000);
        TimeUnit.MILLISECONDS.sleep(deliveryTime);

        return deliveryTime;
    }
}
