// --------------------------------------------------------------
// File Name: IEmailService.cs
// Author: Hasindu Koshitha
// Description: Defines the contract for sending emails within the system,
//              including password reset functionality.
// Created On: 26/09/2025
// --------------------------------------------------------------

namespace EvBackend.Services.Interfaces
{
    public interface IEmailService
    {
        Task SendPasswordResetEmail(string email, string resetToken);
    }
}
