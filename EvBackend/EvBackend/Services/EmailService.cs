// --------------------------------------------------------------
// File Name: EmailService.cs
// Author: Hasindu Koshitha
// Description: Implements email sending logic
// Created On: 26/09/2025
// --------------------------------------------------------------

using System.Net;
using System.Net.Mail;
using EvBackend.Services.Interfaces;
using EvBackend.Settings;
using Microsoft.Extensions.Options;

namespace EvBackend.Services
{
    public class EmailService : IEmailService
    {
        private readonly EmailSettings _emailSettings;

        public EmailService(
            IOptions<EmailSettings> emailSettings)
        {
            _emailSettings = emailSettings.Value;
        }

        public async Task SendPasswordResetEmail(string email, string resetToken)
        {
            using var smtpClient = new SmtpClient
            {
                Host = _emailSettings.Host,
                Port = _emailSettings.Port,
                EnableSsl = _emailSettings.Encryption?.ToLower() == "tls" || _emailSettings.Encryption?.ToLower() == "ssl",
                Credentials = new NetworkCredential(
                    _emailSettings.Username,
                    _emailSettings.Password)
            };

            string fromAddress = _emailSettings.FromAddress ?? _emailSettings.Username;
            string fromName = _emailSettings.FromName ?? "EV Backend";
            string resetUrl = $"{_emailSettings.FrontendUrl}/reset-password?token={resetToken}";

            var mailMessage = new MailMessage
            {
                From = new MailAddress(fromAddress, fromName),
                Subject = "Password Reset Request",
                Body = $"Click the link below to reset your password:\n\n{resetUrl}",
                IsBodyHtml = false
            };

            mailMessage.To.Add(email);

            await smtpClient.SendMailAsync(mailMessage);
        }
    }
}
