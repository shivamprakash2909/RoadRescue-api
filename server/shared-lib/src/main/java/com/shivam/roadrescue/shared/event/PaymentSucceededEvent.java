package com.shivam.roadrescue.shared.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentSucceededEvent extends BaseEvent {
    private UUID paymentId;
    private UUID invoiceId;
    private UUID bookingId;
    private UUID customerId;
    private BigDecimal amount;
    private String gatewayReference;
}
