package ir.maktab127.dto;

import ir.maktab127.entity.user.Specialist;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;


public class SpecialistMapper {
    public static SpecialistResponseDto toResponseDto(Specialist specialist) {
        SpecialistResponseDto dto = new SpecialistResponseDto();
        dto.setId(specialist.getId());
        dto.setFirstName(specialist.getFirstName());
        dto.setLastName(specialist.getLastName());
        dto.setEmail(specialist.getEmail());
        dto.setProfileImage(specialist.getProfileImage());
        dto.setStatus(specialist.getStatus() != null ? specialist.getStatus().name() : null);
        dto.setRegisterDate(specialist.getCreateDate() != null ? specialist.getCreateDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        return dto;
    }

    public static Specialist toEntity(SpecialistRegisterDto dto) {
        Specialist specialist = new Specialist();
        specialist.setFirstName(dto.getFirstName());
        specialist.setLastName(dto.getLastName());
        specialist.setEmail(dto.getEmail());
        specialist.setPassword(dto.getPassword());
        specialist.setProfileImage(dto.getProfileImage());
        return specialist;
    }

}
