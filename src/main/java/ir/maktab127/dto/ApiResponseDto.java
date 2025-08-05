package ir.maktab127.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponseDto {
    private String message;
    private boolean success;
    private Object data;

    public ApiResponseDto(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public ApiResponseDto(String message, boolean success, Object data) {
        this.message = message;
        this.success = success;
        this.data = data;
    }
}
