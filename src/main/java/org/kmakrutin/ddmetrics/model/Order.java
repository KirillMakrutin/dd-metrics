package org.kmakrutin.ddmetrics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Order implements Serializable {
    @Builder.Default
    private OrderStatus status = OrderStatus.WAITING_COLLECTION;
    private String city;
    private String timezone;
    private String type;
    private int quantity;
    private Duration orderWaitingCollection;
    private Duration orderCollected;
    private Duration orderWaitingDeliveryWarehouse;
    private Duration orderDeliveredWarehouse;
    private Duration orderWaitingDeliveryCustomer;
    private Duration orderDeliveredCustomer;
}
