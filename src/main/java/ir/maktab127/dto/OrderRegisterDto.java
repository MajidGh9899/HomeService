package ir.maktab127.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class OrderRegisterDto {

    private Long customerId;
    @NotNull
    private Long serviceCategoryId;
    @NotBlank
    private String description;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal proposedPrice;
    @NotBlank
    private String address;
    @NotNull
    @Future
    private LocalDateTime startDate;
}
