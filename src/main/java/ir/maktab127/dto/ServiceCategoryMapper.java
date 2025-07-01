package ir.maktab127.dto;

import ir.maktab127.entity.ServiceCategory;

public class ServiceCategoryMapper {
    public static ServiceCategoryResponseDto toResponseDto(ServiceCategory service) {
        ServiceCategoryResponseDto dto = new ServiceCategoryResponseDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setBasePrice(service.getBasePrice());
        dto.setDescription(service.getDescription());
        dto.setParentId(service.getParent() != null ? service.getParent().getId() : null);
        dto.setParentName(service.getParent() != null ? service.getParent().getName() : null);
        return dto;
    }
    public static ServiceCategory toEntity(ServiceCategoryRegisterDto dto, ServiceCategory parent) {
        ServiceCategory service = new ServiceCategory();
        service.setName(dto.getName());
        service.setBasePrice(dto.getBasePrice());
        service.setDescription(dto.getDescription());
        service.setParent(parent);
        return service;
    }
}
