package ir.maktab127.controller;

import ir.maktab127.dto.payment.PaymentRequestDto;
import ir.maktab127.exception.OrderException;
import ir.maktab127.exception.WalletException;
import ir.maktab127.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Map<String, String>> payOrder(@PathVariable Long orderId, @RequestBody PaymentRequestDto request) {
        try {
            orderService.payOrder(orderId, request);
            return ResponseEntity.ok(Map.of("message", "Order paid successfully"));
        } catch (OrderException | WalletException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}