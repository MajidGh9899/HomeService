package ir.maktab127.repository.specification;

import ir.maktab127.entity.user.Customer;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {
    public static Specification<Customer> searchWithFilters(String firstName, String lastName) {
        return (root, query, criteriaBuilder) -> {

            Predicate predicate = criteriaBuilder.conjunction();


            if (firstName != null && !firstName.isEmpty()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(root.get("firstName"), "%" + firstName + "%")
                );
            }


            if (lastName != null && !lastName.isEmpty()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(root.get("lastName"), "%" + lastName + "%")
                );
            }

            return predicate;
        };
    }
}
