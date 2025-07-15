package ir.maktab127.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Controller
public class PaymentController {

    @GetMapping("/pay/{token}")
    public String showPaymentPage(@PathVariable String token, Model model) {
        model.addAttribute("token", token);
        return "PaymentPage";
    }

    // تولید کپچا (تصویر ساده Base64)
    @GetMapping(value = "/api/payment/captcha", produces = MediaType.IMAGE_PNG_VALUE)
    public void getCaptcha(HttpServletResponse response) throws IOException {
        // برای سادگی: یک تصویر کپچا ثابت
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
        File outputFile = new File("static/images/captcha_" + captchaText + ".png");
        ImageIO.write(bufferedImage, "png", outputFile);

        // ارسال تصویر به مرورگر
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        ImageIO.write(bufferedImage, "png", response.getOutputStream());
    }

    // دریافت اطلاعات پرداخت
    @PostMapping("/api/payment/submit")
    @ResponseBody
    public ResponseEntity<String> submitPayment(@RequestParam String token,
                                                @RequestParam String cardNumber,
                                                @RequestParam String cvv2,
                                                @RequestParam String expDate,
                                                @RequestParam String password,
                                                @RequestParam String captcha) {
        // اعتبارسنجی ساده (همه چیز معتبر فرض می‌شود)
        // بررسی مهلت پرداخت (در اینجا فقط شبیه‌سازی)
        // ...

        return ResponseEntity.ok("کیف پول با موفقیت شارژ شد");
    }
}
