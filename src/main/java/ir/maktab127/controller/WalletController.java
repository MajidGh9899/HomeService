package ir.maktab127.controller;

import ir.maktab127.dto.WalletChargeRequestDto;
import ir.maktab127.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    @PostMapping("charge/{customerId}")
    public ResponseEntity<Map<String, String>> chargeWallet(@PathVariable Long customerId, @RequestBody WalletChargeRequestDto request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid amount"));
        }
        String payLink = walletService.createPaymentRequest(customerId, request.getAmount());
        return ResponseEntity.ok(Map.of("payLink", payLink));
    }
}

