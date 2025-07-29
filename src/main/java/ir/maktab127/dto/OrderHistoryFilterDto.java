package ir.maktab127.dto;

import ir.maktab127.entity.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderHistoryFilterDto {
    private OrderStatus status;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String serviceCategory;
    private Integer minPrice;
    private Integer maxPrice;
    private String sortBy = "orderDate";
    private String sortDirection = "DESC";
    private Integer page = 0;
    private Integer size = 10;
}
