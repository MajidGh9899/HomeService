package ir.maktab127.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AdminResponseDto {
    // Getters/Setters
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String registerDate;

}