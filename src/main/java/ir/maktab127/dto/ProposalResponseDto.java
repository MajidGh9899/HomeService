package ir.maktab127.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProposalResponseDto {
    private Long id;
    private String specialistName;
    private Long orderId;
    private BigDecimal proposedPrice;
    private String proposedStartTime;
    private Integer durationInHours;
    private String createdAt;
}
