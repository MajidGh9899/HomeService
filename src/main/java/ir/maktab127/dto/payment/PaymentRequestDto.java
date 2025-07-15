package ir.maktab127.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDto {
    private Long customerId;
    private String captcha;
}
