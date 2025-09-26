// --------------------------------------------------------------
// File Name: EmailSettings.cs
// Author: Hasindu Koshitha
// Description: Strongly-typed configuration class for Email (SMTP) settings
// Created On: 13/09/2025
// --------------------------------------------------------------

namespace EvBackend.Settings
{
    public class EmailSettings
    {
        public string? Mailer { get; set; }
        public string? Host { get; set; }
        public int Port { get; set; }
        public string? Username { get; set; }
        public string? Password { get; set; }
        public string? Encryption { get; set; }
        public string? FromAddress { get; set; }
        public string? FromName { get; set; }
    }
}
