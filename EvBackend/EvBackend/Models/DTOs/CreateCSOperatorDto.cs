// --------------------------------------------------------------
// File Name: CreateCSOperatorDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object used for creating a new charging station operator
// Created On: 13/09/2025
// --------------------------------------------------------------

using System.ComponentModel.DataAnnotations;

namespace EvBackend.Models.DTOs
{
    public class CreateCSOperatorDto
    {
        [Required]
        [StringLength(100)]
        public required string FullName { get; set; }

        [Required]
        [EmailAddress]
        public required string Email { get; set; }

        [Required]
        [StringLength(100, MinimumLength = 6)]
        public required string Password { get; set; }
        public bool IsActive { get; set; } = false;
        [Required]
        public required string StationId { get; set; }

        [Required]
        public required string StationName { get; set; }

        [Required]
        public required string StationLocation { get; set; }
    }

    public class UpdateCSOperatorDto
    {
        [Required]
        [StringLength(100)]
        public required string FullName { get; set; }

        [Required]
        [EmailAddress]
        public required string Email { get; set; }

        public bool IsActive { get; set; }
        [Required]
        public required string StationId { get; set; }

        [Required]
        public required string StationName { get; set; }

        [Required]
        public required string StationLocation { get; set; }
    }
}
