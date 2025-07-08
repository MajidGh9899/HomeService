package ir.maktab127.repository;

import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Repository
@Transactional
public class OrderRepositoryImpl implements OrderRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            entityManager.persist(order);
            return order;
        } else {
            return entityManager.merge(order);
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Order.class, id));
    }

    @Override
    public List<Order> findAll() {
        return entityManager.createQuery("SELECT o FROM Order o", Order.class).getResultList();
}
    @Override
    public void delete(Order order) {
        entityManager.remove(entityManager.contains(order) ? order : entityManager.merge(order));
    }

    //

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return entityManager.createQuery(
                        "SELECT o FROM Order o WHERE o.status = :status", Order.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public List<Order> findByStatusIn(List<OrderStatus> statuses) {
        return entityManager.createQuery(
                        "SELECT o FROM Order o WHERE o.status IN :statuses", Order.class)
                .setParameter("statuses", statuses)
                .getResultList();
    }

    @Override
    public List<Order> findByServiceCategoryId(Long serviceCategoryId) {
        return entityManager.createQuery(
                        "SELECT o FROM Order o WHERE o.service.id = :serviceCategoryId", Order.class)
                .setParameter("serviceCategoryId", serviceCategoryId)
                .getResultList();
    }

    @Override
    public Boolean hasActualOrder(Long serviceCategoryId) {

        return !entityManager.createQuery(
                        "SELECT o FROM Order o WHERE o.service.id = :serviceCategoryId AND o.status = :status", Order.class)
                .setParameter("serviceCategoryId", serviceCategoryId)
                .setParameter("status", OrderStatus.IN_PROGRESS)
                .getResultList().isEmpty();

    }
}
