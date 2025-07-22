package ir.maktab127.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDto {
    private String token;
    private String amount;
    private String description;
    private Long customerId;
    private String captcha;


    public PaymentRequestDto(long l) {
        this.customerId = l;
    }
}
