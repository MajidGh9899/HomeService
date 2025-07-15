package ir.maktab127.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WalletChargeRequestDto {
    private BigDecimal amount;
}
