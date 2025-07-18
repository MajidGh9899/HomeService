package ir.maktab127.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRegisterDto {
    @NotNull
    private Long customerId;
    @NotNull
    private Long specialistId;
    @NotNull
    private Long orderId;
    @NotNull
    @Min(1) @Max(5)
    private Integer rating;
    private String text;
}
