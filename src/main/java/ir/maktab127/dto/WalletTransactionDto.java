package ir.maktab127.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter

public class WalletTransactionDto {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime createDate;
    private String description;
}
