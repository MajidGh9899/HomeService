package ir.maktab127.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpecialistResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String profileImagePath;
    private String status;
    private String registerDate;
}
