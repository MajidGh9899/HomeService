package ir.maktab127.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderResponseDto {
    private Long id;
    private String customerName;
    private String serviceName;
    private String description;
    private BigDecimal proposedPrice;
    private String address;
    private String startDate;
    private String createdAt;
    private String status;
}
