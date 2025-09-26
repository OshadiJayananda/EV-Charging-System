// --------------------------------------------------------------
// File Name: ResetPasswordDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object used for resetting the user password
// Created On: 26/09/2025
// --------------------------------------------------------------

using System.ComponentModel.DataAnnotations;

namespace EvBackend.Models.DTOs
{
    public class ResetPasswordDto
    {
        [Required]
        public string resetToken { get; set; }

        [Required]
        public string newPassword { get; set; }
    }
}
