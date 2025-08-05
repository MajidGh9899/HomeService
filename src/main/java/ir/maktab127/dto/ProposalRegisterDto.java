package ir.maktab127.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProposalRegisterDto {
    @NotNull(message = "Order ID is required")
    private Long orderId;


    private Long specialistId;

    @NotNull(message = "Proposed price is required")
    @DecimalMin(value = "0.01", message = "Proposed price must be greater than 0")
    private BigDecimal proposedPrice;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private String description;
}
