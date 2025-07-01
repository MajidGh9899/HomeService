package ir.maktab127.service;
import ir.maktab127.dto.CommentRegisterDto;
import ir.maktab127.entity.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    Comment save(Comment comment);
    Optional<Comment> findById(Long id);
    List<Comment> getAll();
    void delete(Long id);
    Comment registerComment(CommentRegisterDto dto, Long orderId);
}