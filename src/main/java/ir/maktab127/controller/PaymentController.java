package ir.maktab127.controller;

import ir.maktab127.service.WalletService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/pay")
public class PaymentController {
    private final WalletService walletService;

    @Autowired
    public PaymentController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{token}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String showPaymentPage(@PathVariable String token, Model model) {
        if (!walletService.isValidPaymentToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        model.addAttribute("token", token);
        return "PaymentPage";
    }

    @GetMapping(value = "/api/payment/captcha", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    public void getCaptcha(HttpServletResponse response, HttpSession session) throws IOException {
//        String captchaText = String.valueOf((int) (Math.random() * 900000 + 100000));
//        session.setAttribute("captcha", captchaText);
//
//        int width = 160;
//        int height = 50;
//        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g2d = bufferedImage.createGraphics();
//
//        g2d.setColor(Color.WHITE);
//        g2d.fillRect(0, 0, width, height);
//        g2d.setFont(new Font("Arial", Font.BOLD, 30));
//        g2d.setColor(Color.BLUE);
//        g2d.drawString(captchaText, 20, 35);
//        g2d.dispose();
//
//        response.setContentType(MediaType.IMAGE_PNG_VALUE);
//        ImageIO.write(bufferedImage, "png", response.getOutputStream());
        String captchaText = String.valueOf((int) (Math.random() * 900000 + 100000));

        // اندازه تصویر
        int width = 160;
        int height = 50;

        // ساخت تصویر
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // پس‌زمینه سفید
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // رسم متن کپچا
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        g2d.setColor(Color.BLUE);
        g2d.drawString(captchaText, 20, 35);

        g2d.dispose(); // بستن منابع گرافیکی

        // ذخیره تصویر در فایل (در پوشه temp مثلاً)
        File outputFile = new File("captcha_" + captchaText + ".png");
        ImageIO.write(bufferedImage, "png", outputFile);

        // ارسال تصویر به مرورگر
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        ImageIO.write(bufferedImage, "png", response.getOutputStream());
    }

    @PostMapping("/api/payment/submit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> submitPayment(
            @RequestParam String token,
            @RequestParam String cardNumber,
            @RequestParam String cvv2,
            @RequestParam String expDate,
            @RequestParam String password,
            @RequestParam String captcha,
            HttpSession session) {
        String storedCaptcha = (String) session.getAttribute("captcha");
        if (storedCaptcha == null || !storedCaptcha.equals(captcha)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid captcha"));
        }



        boolean paymentSuccess = walletService.processPayment(token);
        if (!paymentSuccess) {
            return ResponseEntity.badRequest().body(Map.of("message", "Payment failed"));
        }

        return ResponseEntity.ok(Map.of("message", "Wallet charged successfully"));
    }

//    @GetMapping("/pay/{token}")
//    public String showPaymentPage(@PathVariable String token, Model model) {
//        model.addAttribute("token", token);
//        return "PaymentPage";
//    }
//
//    // تولید کپچا (تصویر ساده Base64)
//    @GetMapping(value = "/api/payment/captcha", produces = MediaType.IMAGE_PNG_VALUE)
//    public void getCaptcha(HttpServletResponse response) throws IOException {
//        // برای سادگی: یک تصویر کپچا ثابت
//        String captchaText = String.valueOf((int) (Math.random() * 900000 + 100000));
//
//        // اندازه تصویر
//        int width = 160;
//        int height = 50;
//
//        // ساخت تصویر
//        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g2d = bufferedImage.createGraphics();
//
//        // پس‌زمینه سفید
//        g2d.setColor(Color.WHITE);
//        g2d.fillRect(0, 0, width, height);
//
//        // رسم متن کپچا
//        g2d.setFont(new Font("Arial", Font.BOLD, 30));
//        g2d.setColor(Color.BLUE);
//        g2d.drawString(captchaText, 20, 35);
//
//        g2d.dispose(); // بستن منابع گرافیکی
//
//        // ذخیره تصویر در فایل (در پوشه temp مثلاً)
//        File outputFile = new File("captcha_" + captchaText + ".png");
//        ImageIO.write(bufferedImage, "png", outputFile);
//
//        // ارسال تصویر به مرورگر
//        response.setContentType(MediaType.IMAGE_PNG_VALUE);
//        ImageIO.write(bufferedImage, "png", response.getOutputStream());
//    }
//
//    // دریافت اطلاعات پرداخت
//    @PostMapping("/api/payment/submit")
//    @ResponseBody
//    public ResponseEntity<String> submitPayment(@RequestParam String token,
//                                                @RequestParam String cardNumber,
//                                                @RequestParam String cvv2,
//                                                @RequestParam String expDate,
//                                                @RequestParam String password,
//                                                @RequestParam String captcha) {
//        // اعتبارسنجی ساده (همه چیز معتبر فرض می‌شود)
//        // بررسی مهلت پرداخت (در اینجا فقط شبیه‌سازی)
//        // ...
//
//        return ResponseEntity.ok("کیف پول با موفقیت شارژ شد");
//    }
}
