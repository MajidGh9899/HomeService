package ir.maktab127.controller;

import ir.maktab127.dto.WalletChargeRequestDto;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.service.CustomerService;
import ir.maktab127.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    private final CustomerService customerService;

    @PostMapping("/charge")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> chargeWallet( @RequestBody WalletChargeRequestDto request) {
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        Customer cus=customerService.findByEmail(email).orElseThrow();
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid amount"));
        }
        String payLink = walletService.createPaymentRequest(cus.getId(), request.getAmount());
        return ResponseEntity.ok(Map.of("payLink", payLink));
    }
}

