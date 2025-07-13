package ir.maktab127.repository;

import ir.maktab127.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    Optional<ServiceCategory> findByNameAndParentId(String name, Long parentId);
    Optional<ServiceCategory> findByName(String name);
}