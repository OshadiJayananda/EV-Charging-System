// --------------------------------------------------------------
// File Name: ChangePasswordDto.cs
// Author: Oshadi Jaananda
// Description: Data transfer object for changing user passwords
// Created On: 08/10/2025
// --------------------------------------------------------------

using System.ComponentModel.DataAnnotations;

namespace EvBackend.Models.DTOs
{
    public class ChangePasswordDto
    {
        [Required(ErrorMessage = "Current password is required")]
        public string OldPassword { get; set; }

        [Required(ErrorMessage = "New password is required")]
        [StringLength(100, MinimumLength = 6, ErrorMessage = "Password must be at least 6 characters long")]
        public string NewPassword { get; set; }

        [Required(ErrorMessage = "Please confirm your new password")]
        [Compare("NewPassword", ErrorMessage = "The new password and confirmation password do not match.")]
        public string ConfirmPassword { get; set; }
    }
}