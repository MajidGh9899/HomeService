package ir.maktab127.repository.specification;


import ir.maktab127.entity.ServiceCategory;
import ir.maktab127.entity.Comment;
import ir.maktab127.entity.user.Specialist;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class SpecialistSpecification {

    public static Specification<Specialist> searchWithFilters(
            String firstName,
            String lastName,
            String serviceName,
            Integer minScore,
            Integer maxScore) {
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


            if (serviceName != null && !serviceName.isEmpty()) {
                Join<Specialist, ServiceCategory> serviceCategoryJoin = root.join("serviceCategories", JoinType.LEFT);
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(serviceCategoryJoin.get("name"), "%" + serviceName + "%")
                );
            }


            if (minScore != null || maxScore != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Comment> commentRoot = subquery.from(Comment.class);
                subquery.select(commentRoot.get("id"));

                // ارتباط با Specialist
                Predicate commentPredicate = criteriaBuilder.equal(
                        commentRoot.get("specialist").get("id"),
                        root.get("id")
                );

                // شرط‌های امتیاز
                if (minScore != null) {
                    commentPredicate = criteriaBuilder.and(
                            commentPredicate,
                            criteriaBuilder.lessThan(commentRoot.get("rating"), minScore)
                    );
                }
                if (maxScore != null) {
                    commentPredicate = criteriaBuilder.and(
                            commentPredicate,
                            criteriaBuilder.greaterThan(commentRoot.get("rating"), maxScore)
                    );
                }

                subquery.where(commentPredicate);
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.not(criteriaBuilder.exists(subquery))
                );
            }

            return predicate;
        };
    }
}