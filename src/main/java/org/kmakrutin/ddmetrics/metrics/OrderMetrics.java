package org.kmakrutin.ddmetrics.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.kmakrutin.ddmetrics.model.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class OrderMetrics {

    private final MeterRegistry registry;

    public void record(final Order order, Function<Order, Duration> processingTimeProvider) {
        registry.timer("orders.processing." + order.getStatus().name().toLowerCase(),
                        "city", order.getCity().toLowerCase(),
                        "timezone", order.getTimezone().toLowerCase())
                .record(processingTimeProvider.apply(order));
    }

}