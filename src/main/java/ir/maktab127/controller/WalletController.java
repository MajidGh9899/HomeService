package ir.maktab127.controller;

import ir.maktab127.dto.WalletChargeRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @PostMapping("/customer/{customerId}/charge")
    public ResponseEntity<String> chargeWallet(@PathVariable Long customerId, @RequestBody WalletChargeRequestDto request) {
        // تولید توکن پرداخت (برای سادگی: UUID)
        String token = java.util.UUID.randomUUID().toString();
        // ذخیره درخواست شارژ (در دیتابیس یا حافظه - اینجا فقط شبیه‌سازی)
        // ...
        String payLink = "http://localhost:8080/pay/" + token;
        return ResponseEntity.ok(payLink);
    }
}
