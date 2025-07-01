package ir.maktab127.repository;

import ir.maktab127.entity.user.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository

public class CustomerRepositoryImpl implements CustomerRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Customer save(Customer customer) {
        if (customer.getId() == null) {
            entityManager.persist(customer);
            return customer;
        } else {
            return entityManager.merge(customer);
        }
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Customer.class, id));
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        List<Customer> result = entityManager.createQuery("SELECT c FROM Customer c WHERE c.email = :email", Customer.class)
                .setParameter("email", email)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<Customer> findAll() {
        return entityManager.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
    }

    @Override
    public void delete(Customer customer) {
        entityManager.remove(entityManager.contains(customer) ? customer : entityManager.merge(customer));
    }
}
