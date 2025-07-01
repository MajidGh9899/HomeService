package ir.maktab127.repository;

import ir.maktab127.entity.ServiceCategory;

import java.util.List;
import java.util.Optional;

public interface ServiceCategoryRepository {
    ServiceCategory save(ServiceCategory serviceCategory);
    Optional<ServiceCategory> findById(Long id);
    Optional<ServiceCategory> findByNameAndParentId(String name, Long parentId);
    List<ServiceCategory> findAll();
    void delete(ServiceCategory serviceCategory);
}