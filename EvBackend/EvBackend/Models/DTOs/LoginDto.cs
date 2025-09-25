// --------------------------------------------------------------
// File Name: LoginDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object used for login the user
// Created On: 13/09/2025
// --------------------------------------------------------------


using System.ComponentModel.DataAnnotations;

namespace EvBackend.Models.DTOs
{
    public class LoginDto
    {
        [Required]
        [EmailAddress]
        public string Email { get; set; }
        [Required]
        [StringLength(100, MinimumLength = 6)]
        public string Password { get; set; }
    }

    public class LoginResponseDto
    {
        public string Token { get; set; }
    }
}
