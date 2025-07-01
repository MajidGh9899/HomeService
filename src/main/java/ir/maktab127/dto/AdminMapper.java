package ir.maktab127.dto;

import ir.maktab127.entity.user.Admin;

import java.time.format.DateTimeFormatter;

public class AdminMapper {
    public static AdminResponseDto toResponseDto(Admin admin) {
        AdminResponseDto dto = new AdminResponseDto();
        dto.setId(admin.getId());
        dto.setFirstName(admin.getFirstName());
        dto.setLastName(admin.getLastName());
        dto.setEmail(admin.getEmail());
        dto.setRegisterDate(admin.getRegisterDate() != null ? admin.getRegisterDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        return dto;
    }
    public static Admin toEntity(AdminRegisterDto dto) {
        Admin admin = new Admin();
        admin.setFirstName(dto.getFirstName());
        admin.setLastName(dto.getLastName());
        admin.setEmail(dto.getEmail());
        admin.setPassword(dto.getPassword());
        return admin;
    }
}
