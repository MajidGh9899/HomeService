package ir.maktab127.dto;

import ir.maktab127.entity.Order;

import java.time.format.DateTimeFormatter;

public class OrderMapper {
    public static OrderResponseDto toResponseDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setCustomerName(order.getCustomer() != null ? order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() : null);
        dto.setServiceName(order.getService() != null ? order.getService().getName() : null);
        dto.setDescription(order.getDescription());
        dto.setProposedPrice(order.getProposedPrice());
        dto.setAddress(order.getAddress());
        dto.setStartDate(order.getStartDate() != null ? order.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        dto.setCreatedAt(order.getCreateDate() != null ? order.getCreateDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        return dto;
    }
}
