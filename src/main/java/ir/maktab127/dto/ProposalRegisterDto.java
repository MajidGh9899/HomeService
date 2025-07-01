package ir.maktab127.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProposalRegisterDto {
    @NotNull
    private Long specialistId;
    @NotNull
    private Long orderId;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal proposedPrice;
    @NotNull
    private String proposedStartTime; // ISO string
    @NotNull
    @Min(1)
    private Integer durationInHours;
}
