package com.studentseva.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service to dispatch OTP emails to users.
 *
 * Designed to gracefully fall back to console logging when SMTP credentials
 * are not yet configured or when running in local development mode.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@studentseva.com}")
    private String fromEmail;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends the 6-digit OTP code to the requested email.
     *
     * @param toEmail The destination email address.
     * @param otp     The 6-digit plaintext OTP code.
     */
    public void sendOtpEmail(String toEmail, String otp) {
        // Always log to terminal so local development / testing never blocks
        log.info("=================================================");
        log.info("🔐 [StudentSeva DEV] OTP for {}: {}", toEmail, otp);
        log.info("=================================================");

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject("Your StudentSeva Login Code: " + otp);
                message.setText(
                        "Hello,\n\n" +
                        "Your one-time verification code for StudentSeva is: " + otp + "\n\n" +
                        "This code will expire in 5 minutes.\n" +
                        "If you did not request this code, please ignore this email.\n\n" +
                        "— The StudentSeva Team"
                );
                mailSender.send(message);
                log.info("Successfully dispatched email OTP to {}", toEmail);
            } catch (Exception ex) {
                log.warn("Could not dispatch SMTP email (falling back to console OTP): {}", ex.getMessage());
            }
        }
    }
}

