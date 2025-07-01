package ir.maktab127.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ServiceCategoryRegisterDto {
    @NotBlank
    private String name;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal basePrice;
    private String description;
    private Long parentId;
}
