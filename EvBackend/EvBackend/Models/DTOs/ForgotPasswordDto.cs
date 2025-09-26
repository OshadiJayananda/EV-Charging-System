// --------------------------------------------------------------
// File Name: ForgotPasswordDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object used for forgot password functionality
// Created On: 26/09/2025
// --------------------------------------------------------------


using System.ComponentModel.DataAnnotations;

namespace EvBackend.Models.DTOs
{
    public class ForgotPasswordDto
    {
        [Required]
        [EmailAddress]
        public string email { get; set; }
    }
}