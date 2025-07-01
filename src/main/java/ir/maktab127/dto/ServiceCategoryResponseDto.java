package ir.maktab127.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ServiceCategoryResponseDto {
    private Long id;
    private String name;
    private BigDecimal basePrice;
    private String description;
    private Long parentId;
    private String parentName;
}
