package ir.maktab127.controller;

import ir.maktab127.dto.*;
import ir.maktab127.entity.Comment;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.service.CommentService;
import ir.maktab127.service.CustomerService;
import ir.maktab127.service.OrderService;
import ir.maktab127.service.ServiceCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
@Validated
public class CustomerController {
    private final CustomerService customerService;
    private final ServiceCategoryService serviceCategoryService;
    private final OrderService orderService;
    private final CommentService commentService;
    @Autowired
    public CustomerController(CustomerService customerService, CommentService commentService, ServiceCategoryService serviceCategoryService, OrderService orderService) {
        this.customerService = customerService;
        this.commentService = commentService;
        this.serviceCategoryService = serviceCategoryService;
        this.orderService = orderService;
    }
    @PostMapping("/register")
    public ResponseEntity<CustomerResponseDto> register(@Valid @RequestBody CustomerRegisterDto dto) {
        Customer customer = CustomerMapper.toEntity(dto);
        Customer saved = customerService.save(customer);
        return ResponseEntity.ok(CustomerMapper.toResponseDto(saved));
    }
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> getById(@PathVariable Long id) {
        Optional<Customer> customer = customerService.findById(id);
        return customer.map(c -> ResponseEntity.ok(CustomerMapper.toResponseDto(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //login
    @PostMapping("/login")
    public ResponseEntity<CustomerResponseDto> login(@Valid @RequestBody CustomerLoginDto dto) {
        Optional<Customer> customer = customerService.login(dto.getEmail(), dto.getPassword());
        return customer
                .map(CustomerMapper::toResponseDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(401).build());
    }
    //
    @PutMapping("/{id}/update-info")
    public ResponseEntity<Void> updateInfo(@PathVariable Long id, @Valid @RequestBody CustomerUpdateDto dto) {
        customerService.updateInfo(id, dto);
        return ResponseEntity.ok().build();
    }
    //
    @GetMapping("/service-categories")
    public List<ServiceCategoryResponseDto> getAllServiceCategories() {
        return serviceCategoryService.getAll().stream()
                .map(ServiceCategoryMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    //order
    @PostMapping("/add-Order")
    public ResponseEntity<OrderResponseDto> registerOrder(@Valid @RequestBody OrderRegisterDto dto) {
        try {
            Order order = orderService.registerOrder(dto);
            return ResponseEntity.ok(OrderMapper.toResponseDto(order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    //payOrder
    @PutMapping("pay/{orderId}/{SpecialistId}")
    public ResponseEntity<Void> payOrder(@PathVariable Long orderId, @PathVariable Long specialistId) {
        try {
            orderService.completedOrder(orderId);
            orderService.payToSpecialist(orderId, specialistId);
            return ResponseEntity.ok().build();


        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    //addComment
    @PostMapping("/register/{orderId}")
    public ResponseEntity<CommentResponseDto> registerComment(
            @PathVariable Long orderId,
            @Valid @RequestBody CommentRegisterDto dto) {
        try {
            Comment comment = commentService.registerComment(dto, orderId);
            return ResponseEntity.ok(CommentMapper.toResponseDto(comment));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(409).build();
        }
    }


}
