package ir.maktab127.repository;

import ir.maktab127.entity.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository

public class CommentRepositoryImpl implements CommentRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == null) {
            entityManager.persist(comment);
            return comment;
        } else {
            return entityManager.merge(comment);
        }
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Comment.class, id));
    }

    @Override
    public List<Comment> findAll() {
        return entityManager.createQuery("SELECT c FROM Comment c", Comment.class).getResultList();
    }

    @Override
    public void delete(Comment comment) {
        entityManager.remove(entityManager.contains(comment) ? comment : entityManager.merge(comment));
    }
}
