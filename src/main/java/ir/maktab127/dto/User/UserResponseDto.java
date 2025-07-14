package ir.maktab127.dto.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {
    private Long id;
    private String role;
    private String firstName;
    private String lastName;
    private String email;
    private String serviceName; // فقط برای متخصص
    private Double score;
}