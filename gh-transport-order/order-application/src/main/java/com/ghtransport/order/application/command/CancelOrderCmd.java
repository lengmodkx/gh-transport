package com.ghtransport.order.application.command;

import lombok.Data;

@Data
public class CancelOrderCmd {
    private String orderId;
    private String reason;
}
