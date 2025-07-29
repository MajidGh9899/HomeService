package ir.maktab127.dto;

import ir.maktab127.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceHistoryDetailDto {
    private Long orderId;
    private String description;
    private BigDecimal proposedPrice;
    private LocalDateTime startDate;
    private LocalDateTime createdAt;
    private String address;
    private OrderStatus status;


    private Long customerId;
    private String customerName;
    private String customerEmail;



    private Long specialistId;
    private String specialistName;
    private String specialistEmail;



    private Long serviceId;
    private String serviceName;
    private String serviceDescription;


    private List<ProposalResponseDto> proposals;


    private List<CommentResponseDto> comments;
}
