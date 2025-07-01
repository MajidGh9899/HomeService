package ir.maktab127.service;

import ir.maktab127.dto.CommentRegisterDto;
import ir.maktab127.entity.Comment;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.CommentRepository;
import ir.maktab127.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final OrderRepository orderRepository;
    private final CustomerService customerRepository;
    private final SpecialistService specialistRepository;
    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, OrderRepository orderRepository, CustomerService customerRepository, SpecialistService specialistRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.specialistRepository = specialistRepository;
        this.commentRepository = commentRepository;
    }
    @Override
    public Comment save(Comment comment) { return commentRepository.save(comment); }
    @Override
    public Optional<Comment> findById(Long id) { return commentRepository.findById(id); }
    @Override
    public List<Comment> getAll() { return commentRepository.findAll(); }
    @Override
    public void delete(Long id) { commentRepository.findById(id).ifPresent(commentRepository::delete); }
    @Override
    public Comment registerComment(CommentRegisterDto dto, Long orderId) {
        // بررسی اینکه سفارش کامل شده و مشتری و متخصص درست هستند
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getStatus() != OrderStatus.COMPLETED)
            throw new IllegalStateException("Order is not completed yet");
        if (!order.getCustomer().getId().equals(dto.getCustomerId()) ||
                !order.getService().getSpecialists().stream().anyMatch(s -> s.getId().equals(dto.getSpecialistId())))
            throw new IllegalArgumentException("Invalid customer or specialist for this order");

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        Specialist specialist = specialistRepository.findById(dto.getSpecialistId())
                .orElseThrow(() -> new IllegalArgumentException("Specialist not found"));

        Comment comment = new Comment();
        comment.setCustomer(customer);
        comment.setSpecialist(specialist);
        comment.setRating(dto.getRating());
        comment.setText(dto.getText());
        comment.setCreatedAt(java.time.LocalDateTime.now());
        return commentRepository.save(comment);
    }
}