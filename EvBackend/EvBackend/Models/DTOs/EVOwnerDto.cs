// --------------------------------------------------------------
// File Name: EVOwnerDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object for EV Owner
// Created On: 13/09/2025
// --------------------------------------------------------------

using System.ComponentModel.DataAnnotations;

namespace EvBackend.Models.DTOs
{
    public class EVOwnerDto
    {
        //Role is always "Owner"

        [Required]
        [StringLength(12, MinimumLength = 10)]
        public string NIC { get; set; }
        [Required]
        public string FullName { get; set; }
        [Required]
        [EmailAddress]
        public string Email { get; set; }
        public bool IsActive { get; set; } = false;
        public DateTime CreatedAt { get; set; }
    }
}
