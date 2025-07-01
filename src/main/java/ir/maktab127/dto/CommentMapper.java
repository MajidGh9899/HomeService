package ir.maktab127.dto;

import ir.maktab127.entity.Comment;

import java.time.format.DateTimeFormatter;

public class CommentMapper {
    public static CommentResponseDto toResponseDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setCustomerName(comment.getCustomer() != null ? comment.getCustomer().getFirstName() + " " + comment.getCustomer().getLastName() : null);
        dto.setSpecialistName(comment.getSpecialist() != null ? comment.getSpecialist().getFirstName() + " " + comment.getSpecialist().getLastName() : null);
        dto.setRating(comment.getRating());
        dto.setText(comment.getText());
        dto.setCreatedAt(comment.getCreatedAt() != null ? comment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        return dto;
    }
}
