package com.projectmanagement.seller.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailUtil {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreplyhelixion@gmail.com}")
    private String senderEmail;

    /**
     * Send simple text email
     */
    public void sendSimpleMail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Simple email successfully sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Send HTML formatted email
     */
    public void sendHtmlMail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("HTML email successfully sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Send welcome email containing login credentials (username and password) to newly created user
     */
    public void sendUserCredentialsMail(String toEmail, String name, String username, String rawPassword) {
        String subject = "Welcome to Seller Management System - Your Account Credentials";
        String htmlContent = buildCredentialsEmailTemplate(name, username, toEmail, rawPassword);
        sendHtmlMail(toEmail, subject, htmlContent);
    }

    /**
     * Send OTP email for password reset
     */
    public void sendPasswordResetOtpMail(String toEmail, String name, String otp) {
        String subject = "Password Reset Request - Seller Management System";
        String htmlContent = buildOtpEmailTemplate(name, otp);
        sendHtmlMail(toEmail, subject, htmlContent);
    }

    private String buildOtpEmailTemplate(String name, String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.08); overflow: hidden; }
                    .header { background: linear-gradient(135deg, #4f46e5 0%%, #7c3aed 100%%); color: #ffffff; padding: 25px; text-align: center; }
                    .header h1 { margin: 0; font-size: 22px; font-weight: 600; }
                    .content { padding: 30px; color: #334155; line-height: 1.6; }
                    .otp-box { background-color: #f1f5f9; border: 2px dashed #4f46e5; border-radius: 8px; padding: 20px; margin: 25px 0; text-align: center; }
                    .otp-code { font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #4f46e5; font-family: monospace; }
                    .footer { text-align: center; font-size: 12px; color: #94a3b8; padding: 15px; border-top: 1px solid #f1f5f9; }
                    .security-notice { font-size: 13px; color: #dc2626; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Seller Management System</h1>
                    </div>
                    <div class="content">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>We received a request to reset your password. Use the following One-Time Password (OTP) to proceed with resetting your password:</p>
                        
                        <div class="otp-box">
                            <div class="otp-code">%s</div>
                            <p style="margin: 8px 0 0 0; color: #64748b; font-size: 13px;">This OTP is valid for 15 minutes.</p>
                        </div>
                        
                        <p class="security-notice">&#9888; If you did not request a password reset, please ignore this email or contact security support immediately.</p>
                        
                        <p>Best regards,<br/><strong>Seller Management Team</strong></p>
                    </div>
                    <div class="footer">
                        This is an automated notification. Please do not reply directly to this email.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name != null ? name : "User", otp);
    }

    private String buildCredentialsEmailTemplate(String name, String username, String email, String password) {
        String displayUsername = (username != null && !username.isBlank()) ? username : email;
        String displayPassword = (password != null && !password.isBlank()) ? password : "[Set via password reset / SSO]";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.08); overflow: hidden; }
                    .header { background: linear-gradient(135deg, #4f46e5 0%%, #7c3aed 100%%); color: #ffffff; padding: 25px; text-align: center; }
                    .header h1 { margin: 0; font-size: 22px; font-weight: 600; }
                    .content { padding: 30px; color: #334155; line-height: 1.6; }
                    .credentials-box { background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 20px; margin: 20px 0; }
                    .cred-row { display: flex; justify-content: space-between; margin-bottom: 10px; }
                    .cred-label { font-weight: 600; color: #475569; }
                    .cred-val { font-family: monospace; font-size: 15px; color: #0f172a; font-weight: bold; background: #e0e7ff; padding: 2px 8px; border-radius: 4px; }
                    .footer { text-align: center; font-size: 12px; color: #94a3b8; padding: 15px; border-top: 1px solid #f1f5f9; }
                    .security-notice { font-size: 13px; color: #dc2626; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Seller Management System</h1>
                    </div>
                    <div class="content">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>Your account has been created successfully. Below are your login credentials to access the portal:</p>
                        
                        <div class="credentials-box">
                            <p style="margin: 8px 0;"><span class="cred-label">Username:</span> <span class="cred-val">%s</span></p>
                            <p style="margin: 8px 0;"><span class="cred-label">Email:</span> <span class="cred-val">%s</span></p>
                            <p style="margin: 8px 0;"><span class="cred-label">Password:</span> <span class="cred-val">%s</span></p>
                        </div>
                        
                        <p class="security-notice">&#9888; For security reasons, please change your password immediately after your initial login.</p>
                        
                        <p>Best regards,<br/><strong>Seller Management Team</strong></p>
                    </div>
                    <div class="footer">
                        This is an automated notification. Please do not reply directly to this email.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                name != null ? name : "User",
                displayUsername,
                email,
                displayPassword
        );
    }
}
