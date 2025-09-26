// --------------------------------------------------------------
// File Name: AuthController.cs
// Author: Hasindu Koshitha
// Description: Handles authentication logic for the system
// Created On: 13/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;
using EvBackend.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Authentication;

namespace EvBackend.Controllers
{
    [ApiController]
    [Route("api/auth")]
    public class AuthController : ControllerBase
    {
        private readonly IAuthService _authService;

        public AuthController(IAuthService authService)
        {
            _authService = authService;
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginDto loginDto)
        {
            try
            {
                var response = await _authService.AuthenticateUser(loginDto);
                return Ok(new
                {
                    token = response.Token
                });
            }
            catch (AuthenticationException ex)
            {
                return Unauthorized(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return StatusCode(500, new { message = "An unexpected error occurred." });
            }
        }

        //introduce a me endpoint to verify token validity
        [HttpGet("me")]
        [Authorize]
        public IActionResult Me()
        {
            var userId = User.Claims.FirstOrDefault(c => c.Type == System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;
            var email = User.Claims.FirstOrDefault(c => c.Type == System.Security.Claims.ClaimTypes.Email)?.Value;
            var role = User.Claims.FirstOrDefault(c => c.Type == System.Security.Claims.ClaimTypes.Role)?.Value;
            var fullName = User.Claims.FirstOrDefault(c => c.Type == "FullName")?.Value;
            var userType = User.Claims.FirstOrDefault(c => c.Type == "UserType")?.Value;
            if (userId == null)
                return Unauthorized(new { message = "Invalid token" });
            return Ok(new
            {
                userId,
                email,
                role,
                fullName,
                userType
            });
        }

        [HttpPost("logout")]
        [Authorize]
        public IActionResult Logout()
        {
            return Ok(new { message = "Logged out successfully" });
        }

        [HttpPost("reset-password")]
        public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordDto resetPasswordDto)
        {
            await _authService.ResetPassword(resetPasswordDto);
            return Ok(new { message = "Password has been reset successfully" });
        }

        [HttpPost("forgot-password")]
        public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordDto forgotPasswordDto)
        {
            await _authService.SendPasswordResetEmail(forgotPasswordDto);

            return Ok(new { message = "If an account with that email exists, a password reset link has been sent." });
        }
    }
}
