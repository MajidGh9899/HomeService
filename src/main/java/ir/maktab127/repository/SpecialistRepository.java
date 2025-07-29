package ir.maktab127.repository;

import ir.maktab127.entity.user.Specialist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface SpecialistRepository extends JpaRepository<Specialist, Long> {

    Optional<Specialist> findByEmail(String email);


    @Query("SELECT s FROM Specialist s " +
            "LEFT JOIN s.serviceCategories sc " +
            "WHERE (:firstName IS NULL OR s.firstName LIKE %:firstName%) " +
            "AND (:lastName IS NULL OR s.lastName LIKE %:lastName%) " +
            "AND (:serviceName IS NULL OR sc.name LIKE %:serviceName%) " +
            "AND (:minScore IS NULL AND :maxScore IS NULL OR " +
            "     NOT EXISTS (SELECT c FROM s.comments c " +
            "                 WHERE (:minScore IS NOT NULL AND c.rating < :minScore) OR " +
            "                       (:maxScore IS NOT NULL AND c.rating > :maxScore)))")
    Page<Specialist> searchWithFilters(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("serviceName") String serviceName,
            @Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore,
            Pageable pageable);

    Optional<Specialist> findByEmailVerificationToken(String token);
}
