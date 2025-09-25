// --------------------------------------------------------------
// File Name: CreateUserDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object used for creating a new user
// Created On: 13/09/2025
// --------------------------------------------------------------

using System.ComponentModel.DataAnnotations;

namespace EvBackend.Models.DTOs
{
    public class CreateUserDto
    {
        [Required]
        [StringLength(100)]
        public string FullName { get; set; }

        [Required]
        [EmailAddress]
        public string Email { get; set; }

        [Required]
        [StringLength(100, MinimumLength = 6)]
        public string Password { get; set; }

        [Required]
        public string Role { get; set; }

        public bool IsActive { get; set; } = false;
    }
}
