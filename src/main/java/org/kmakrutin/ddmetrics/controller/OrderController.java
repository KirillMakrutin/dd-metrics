package org.kmakrutin.ddmetrics.controller;

import lombok.extern.slf4j.Slf4j;
import org.kmakrutin.ddmetrics.service.OrderProducer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.integration.amqp.channel.PointToPointSubscribableAmqpChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RequestMapping("/api/orders")
@Slf4j
@RestController
public class OrderController {
    private final OrderProducer orderProducer;
    private final Map<String, MessageChannel> messageChannels;

    public OrderController(OrderProducer orderProducer,
                           Map<String, MessageChannel> messageChannels) {
        this.orderProducer = orderProducer;
        this.messageChannels = messageChannels.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("order"))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (prev, next) -> next,
                        TreeMap::new));
    }

    @GetMapping("/producer")
    public Map<String, Boolean> producerStatus() {
        return Map.of("enabled", orderProducer.isTurnedOn());
    }

    @PostMapping("/producer")
    public void turnOffProducer(@RequestParam String toggle) {
        log.info("Toggle producer {} ", toggle);

        if ("ON".equalsIgnoreCase(toggle)) {
            orderProducer.turnOn();
        } else {
            orderProducer.turnOff();
        }
    }

    @GetMapping("/workers")
    public Map<String, Integer> getWorkersCount() {
        return messageChannels.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> getContainer(entry.getValue()).getActiveConsumerCount(),
                        (prev, next) -> next,
                        LinkedHashMap::new
                ));
    }

    @PostMapping("/workers/{name}/{size}")
    public void setWorkersCount(@PathVariable String name, @PathVariable int size) {
        final SimpleMessageListenerContainer container = getContainer(messageChannels.get(name));

        if (size >= 1) {
            container.setConcurrentConsumers(size);
        }
    }


    private SimpleMessageListenerContainer getContainer(MessageChannel messageChannel) {
        return Optional.ofNullable(messageChannel)
                .filter(PointToPointSubscribableAmqpChannel.class::isInstance)
                .flatMap(pointToPointChannel -> Optional.ofNullable(ReflectionUtils.findField(PointToPointSubscribableAmqpChannel.class, "container"))
                        .map(containerField -> {
                            ReflectionUtils.makeAccessible(containerField);
                            return containerField;
                        })
                        .map(containerField -> ReflectionUtils.getField(containerField, pointToPointChannel)))
                .filter(SimpleMessageListenerContainer.class::isInstance)
                .map(SimpleMessageListenerContainer.class::cast)
                .orElse(null);
    }


}
