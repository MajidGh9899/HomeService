package ir.maktab127.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResponseDto {
    private Long id;
    private String customerName;
    private String specialistName;
    private Integer rating;
    private String text;
    private String createdAt;
}
