package ir.maktab127.repository;

import ir.maktab127.entity.user.Specialist;

import java.util.List;
import java.util.Optional;

public interface SpecialistRepository {
    Specialist save(Specialist specialist);
    Optional<Specialist> findById(Long id);
    Optional<Specialist> findByEmail(String email);
    List<Specialist> findAll();
    void delete(Specialist specialist);

}
