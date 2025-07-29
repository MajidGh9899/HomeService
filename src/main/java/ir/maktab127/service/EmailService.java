package ir.maktab127.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String verificationToken) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Email Verification");
        String verificationUrl = "http://localhost:8080/api/auth/verify-email?token=" + verificationToken;
        helper.setText(
                "<h3>Please verify your email</h3>" +
                        "<p>Click the link below to verify your email address:</p>" +
                        "<a href=\"" + verificationUrl + "\">Verify Email</a>" +
                        "<p>This link is valid for 24 hours.</p>", true);
        mailSender.send(message);
    }
}
