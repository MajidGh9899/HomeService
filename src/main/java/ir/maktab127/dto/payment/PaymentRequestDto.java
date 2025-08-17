package ir.maktab127.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDto {
    private String token;
    @NotNull
    private String amount;
    private String description;
    private Long customerId;



}
