package org.kmakrutin.ddmetrics.gateway;

import org.kmakrutin.ddmetrics.model.Order;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway
public interface PipelineGateway {

    @Gateway(requestChannel = "orderCollecting")
    void readyCollecting(Order event, @Header("readyToCollectMilliseconds") long readyToCollectMilliseconds);

    @Gateway(requestChannel = "orderDeliveryWarehouse")
    void readyDeliveryWarehouse(Order event, @Header("readyToPickUpWarehouseMilliseconds") long readyToPickUpWarehouseMilliseconds);

    @Gateway(requestChannel = "orderDeliveryCustomer")
    void readyDeliveryCustomer(Order event, @Header("readyToPickUpCustomerMilliseconds") long readyToPickUpCustomerMilliseconds);

}