package ir.maktab127.repository.specification;

import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.ProposalStatus;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class OrderSpecification {

    public static Specification<Order> hasStartDate(LocalDateTime startDate) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createDate"), startDate);
    }

    public static Specification<Order> hasEndDate(LocalDateTime endDate) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createDate"), endDate);
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Order> hasServiceCategoryId(Long serviceCategoryId) {
        return (root, query, cb) -> cb.equal(root.get("service").get("id"), serviceCategoryId);
    }

    public static Specification<Order> hasCustomerId(Long customerId) {
        return (root, query, cb) -> cb.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Order> hasAcceptedProposalBySpecialist(Long specialistId) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Proposal> proposalRoot = subquery.from(Proposal.class);
            subquery.select(proposalRoot.get("order").get("id"))
                    .where(
                            cb.equal(proposalRoot.get("specialist").get("id"), specialistId),
                            cb.equal(proposalRoot.get("status"), ProposalStatus.ACCEPTED),
                            cb.equal(proposalRoot.get("order").get("id"), root.get("id"))
                    );
            return cb.exists(subquery);
        };
    }
}
