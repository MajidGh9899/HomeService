package ir.maktab127.repository;

import ir.maktab127.entity.Comment;

import java.util.List;
import java.util.Optional;
public interface CommentRepository {
    Comment save(Comment comment);
    Optional<Comment> findById(Long id);
    List<Comment> findAll();
    void delete(Comment comment);
}
