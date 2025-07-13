package ir.maktab127.repository;

import ir.maktab127.entity.Comment;
import ir.maktab127.entity.user.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
public interface CommentRepository extends JpaRepository<Comment, Long> {

}
