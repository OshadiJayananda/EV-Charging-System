// --------------------------------------------------------------
// File Name: CreateEVOwnerDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object used for creating a new electric vehicle owner
// Created On: 13/09/2025
// --------------------------------------------------------------

using System.ComponentModel.DataAnnotations;

namespace EvBackend.Models.DTOs
{
    public class CreateEVOwnerDto
    {
        [Required]
        [StringLength(12, MinimumLength = 10)]
        public string NIC { get; set; }
        [Required]
        public string FullName { get; set; }
        [Required]
        [EmailAddress]
        public string Email { get; set; }
        [Required]
        [StringLength(100, MinimumLength = 6)]
        public string Password { get; set; }
        public bool IsActive { get; set; } = false;
    }
}
