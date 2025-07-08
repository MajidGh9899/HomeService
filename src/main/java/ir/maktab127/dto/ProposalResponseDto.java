package ir.maktab127.dto;

import ir.maktab127.entity.ProposalStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProposalResponseDto {
    private Long id;
    private Long orderId;
    private Long specialistId;
    private String specialistName;
    private BigDecimal proposedPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String description;
    private LocalDateTime createdAt;
    private ProposalStatus status;
}
