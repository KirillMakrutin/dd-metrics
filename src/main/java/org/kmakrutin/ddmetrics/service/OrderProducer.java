package org.kmakrutin.ddmetrics.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kmakrutin.ddmetrics.gateway.PipelineGateway;
import org.kmakrutin.ddmetrics.model.Order;
import org.kmakrutin.ddmetrics.order.Utils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderProducer implements DisposableBean, PriorityOrdered {

    private final List<Map.Entry<String, String>> cityTimeZones = List.of(
            Map.entry("New York", "America/New_York"),       // Eastern Time
            Map.entry("Los Angeles", "America/Los_Angeles"), // Pacific Time
            Map.entry("Chicago", "America/Chicago"),         // Central Time
            Map.entry("Houston", "America/Chicago"),         // Central Time
            Map.entry("Phoenix", "America/Phoenix"),         // Mountain Time (No DST)
            Map.entry("Philadelphia", "America/New_York"),   // Eastern Time
            Map.entry("San Antonio", "America/Chicago"),     // Central Time
            Map.entry("San Diego", "America/Los_Angeles"),   // Pacific Time
            Map.entry("Dallas", "America/Chicago"),          // Central Time
            Map.entry("San Jose", "America/Los_Angeles"),    // Pacific Time
            Map.entry("Austin", "America/Chicago"),          // Central Time
            Map.entry("Jacksonville", "America/New_York"),   // Eastern Time
            Map.entry("Fort Worth", "America/Chicago"),      // Central Time
            Map.entry("Columbus", "America/New_York"),       // Eastern Time
            Map.entry("Indianapolis", "America/Indiana/Indianapolis"), // Eastern Time (No DST)
            Map.entry("Charlotte", "America/New_York"),      // Eastern Time
            Map.entry("San Francisco", "America/Los_Angeles"), // Pacific Time
            Map.entry("Seattle", "America/Los_Angeles"),     // Pacific Time
            Map.entry("Denver", "America/Denver"),           // Mountain Time
            Map.entry("Washington", "America/New_York")      // Eastern Time
    );

    private final PipelineGateway pipelineGateway;

    private final AtomicBoolean produce = new AtomicBoolean(false);

    public synchronized void turnOff() {
        if (produce.get()) {
            produce.set(false);
        }
    }

    public synchronized void turnOn() {
        if (!produce.get()) {
            produce.set(true);
            produce();
        }
    }

    public boolean isTurnedOn() {
        return produce.get();
    }

    void produce() {
        new Thread(() -> {
            while (this.produce.get()) {
                cityTimeZones.forEach(city -> {
                    final Order order = Order.builder()
                            .city(city.getKey())
                            .timezone(city.getValue())
                            .quantity(Utils.nextInt(1, 100))
                            .type(type(Utils.nextInt(0, 6)))
                            .build();

                    pipelineGateway.readyCollecting(order, System.currentTimeMillis());

                    sleep(Utils.nextInt(50, 200));

                    log.info("Order created: {}", order);
                });

                sleep(60_000);
            }
        }).start();
    }

    @SneakyThrows
    private static void sleep(long millis) {
        TimeUnit.MILLISECONDS.sleep(millis);
    }

    private static String type(int position) {
        return switch (position) {
            case 0:
                yield "car";
            case 1:
                yield "truck";
            case 2:
                yield "bus";
            case 3:
                yield "fruits";
            case 4:
                yield "glasses";
            case 5:
                yield "pizza";
            default:
                yield "horse";
        };
    }

    @PreDestroy
    @Override
    public void destroy() {
        produce.set(false);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
