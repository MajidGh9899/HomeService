package ir.maktab127.repository;

import ir.maktab127.entity.user.Specialist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface SpecialistRepository extends JpaRepository<Specialist, Long> {

    Optional<Specialist> findByEmail(String email);


    @Query("SELECT DISTINCT s FROM Specialist s " +
            "LEFT JOIN FETCH s.serviceCategories sc " +
            "LEFT JOIN FETCH s.comments c " +
            "WHERE (:firstName IS NULL OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) " +
            "AND (:lastName IS NULL OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) " +
            "AND (:serviceName IS NULL OR EXISTS (SELECT sc2 FROM s.serviceCategories sc2 WHERE LOWER(sc2.name) LIKE LOWER(CONCAT('%', :serviceName, '%'))) " +
            "AND (:minScore IS NULL OR (SELECT AVG(c2.rating) FROM s.comments c2) >= :minScore) " +
            "AND (:maxScore IS NULL OR (SELECT AVG(c2.rating) FROM s.comments c2) <= :maxScore)")
    List<Specialist> searchWithFilters(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("serviceName") String serviceName,
            @Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore
    );
}
