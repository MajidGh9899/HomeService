package ir.maktab127.dto.order;

import ir.maktab127.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderSummaryDTO(Long orderId,
                              String customerFullName,
                              String specialistFullName,
                              String serviceTitle,
                              LocalDateTime createDate,
                              OrderStatus status) {
}
