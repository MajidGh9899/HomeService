package ir.maktab127.repository;

import ir.maktab127.entity.ServiceCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ServiceCategoryRepositoryImpl implements ServiceCategoryRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ServiceCategory save(ServiceCategory serviceCategory) {
        if (serviceCategory.getId() == null) {
            entityManager.persist(serviceCategory);
            return serviceCategory;
        } else {
            return entityManager.merge(serviceCategory);
        }
    }

    @Override
    public Optional<ServiceCategory> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ServiceCategory.class, id));
    }

    @Override
    public Optional<ServiceCategory> findByNameAndParentId(String name, Long parentId) {
        String jpql = "SELECT s FROM ServiceCategory s WHERE s.name = :name AND " +
                (parentId == null ? "s.parent IS NULL" : "s.parent.id = :parentId");
        var query = entityManager.createQuery(jpql, ServiceCategory.class)
                .setParameter("name", name);
        if (parentId != null) query.setParameter("parentId", parentId);
        List<ServiceCategory> result = query.getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<ServiceCategory> findAll() {
        return entityManager.createQuery("SELECT s FROM ServiceCategory s", ServiceCategory.class).getResultList();
    }

    @Override
    public void delete(ServiceCategory serviceCategory) {
        entityManager.remove(entityManager.contains(serviceCategory) ? serviceCategory : entityManager.merge(serviceCategory));
    }
}