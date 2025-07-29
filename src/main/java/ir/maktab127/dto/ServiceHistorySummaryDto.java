package ir.maktab127.dto;

import ir.maktab127.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceHistorySummaryDto {
    private Long orderId;
    private String customerName;
    private String specialistName;
    private String serviceName;
    private BigDecimal price;
    private OrderStatus status;
    private LocalDateTime startDate;
    private LocalDateTime createdAt;
    private String address;
}
