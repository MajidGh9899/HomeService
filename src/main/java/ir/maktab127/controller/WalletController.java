package ir.maktab127.controller;

import ir.maktab127.dto.WalletChargeRequestDto;
import ir.maktab127.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/customer/{customerId}/charge")
    public ResponseEntity<String> chargeWallet(@PathVariable Long customerId, @RequestBody WalletChargeRequestDto request) {
        // تولید توکن پرداخت (برای سادگی: UUID)
        String token = UUID.randomUUID().toString();
        // ذخیره درخواست شارژ (در دیتابیس یا حافظه - اینجا فقط شبیه‌سازی)
        // ...

        String payLink = "http://localhost:8080/pay/" + token;

        return ResponseEntity.ok(payLink);
    }
}
