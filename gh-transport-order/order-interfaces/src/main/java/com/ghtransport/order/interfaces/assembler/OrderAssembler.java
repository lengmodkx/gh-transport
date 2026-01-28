package com.ghtransport.order.interfaces.assembler;

import com.ghtransport.order.application.command.CreateOrderCmd;
import com.ghtransport.order.interfaces.dto.CreateOrderRequest;
import org.springframework.stereotype.Component;

@Component
public class OrderAssembler {

    public CreateOrderCmd toCreateCmd(CreateOrderRequest request) {
        CreateOrderCmd cmd = new CreateOrderCmd();
        cmd.setCustomerId(request.getCustomerId());
        cmd.setPickupProvince(request.getPickupProvince());
        cmd.setPickupCity(request.getPickupCity());
        cmd.setPickupDistrict(request.getPickupDistrict());
        cmd.setPickupDetail(request.getPickupDetail());
        cmd.setPickupContact(request.getPickupContact());
        cmd.setPickupPhone(request.getPickupPhone());
        cmd.setDeliveryProvince(request.getDeliveryProvince());
        cmd.setDeliveryCity(request.getDeliveryCity());
        cmd.setDeliveryDistrict(request.getDeliveryDistrict());
        cmd.setDeliveryDetail(request.getDeliveryDetail());
        cmd.setDeliveryContact(request.getDeliveryContact());
        cmd.setDeliveryPhone(request.getDeliveryPhone());
        cmd.setItems(request.getItems().stream()
            .map(item -> {
                var itemCmd = new com.ghtransport.order.application.command.OrderItemCmd();
                itemCmd.setItemName(item.getItemName());
                itemCmd.setQuantity(item.getQuantity());
                itemCmd.setWeight(item.getWeight());
                itemCmd.setVolume(item.getVolume());
                itemCmd.setUnitPrice(item.getUnitPrice());
                return itemCmd;
            })
            .toList());
        cmd.setPickupTime(request.getPickupTime());
        cmd.setDeliveryTime(request.getDeliveryTime());
        return cmd;
    }
}
