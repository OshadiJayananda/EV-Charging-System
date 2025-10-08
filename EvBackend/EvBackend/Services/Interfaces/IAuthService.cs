// --------------------------------------------------------------
// File Name: IAuthService.cs
// Author: Hasindu Koshitha
// Description: Defines user authentication service methods for the system
// Created On: 25/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;

namespace EvBackend.Services.Interfaces
{
    public interface IAuthService
    {
        Task<LoginResponseDto> AuthenticateUser(LoginDto loginDto, HttpRequest request);
        Task SendPasswordResetEmail(ForgotPasswordDto forgotPasswordDto);
        Task ResetPassword(ResetPasswordDto resetPasswordDto);
        Task ChangePassword(string userId, ChangePasswordDto changePasswordDto);
    }
}
