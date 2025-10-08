// --------------------------------------------------------------
// File Name: CSOperatorDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object for Charging Station Operator
// Created On: 13/09/2025
// --------------------------------------------------------------

using System.ComponentModel.DataAnnotations;

namespace EvBackend.Models.DTOs
{
    public class CSOperatorDto
    {
        public required string Id { get; set; }
        [Required]
        [StringLength(100)]
        public required string FullName { get; set; }
        [Required]
        [EmailAddress]
        public required string Email { get; set; }
        [Required]
        [EnumDataType(typeof(UserRole), ErrorMessage = "Role must be either 'Admin' or 'Operator'.")]
        public required string Role { get; set; }
        public bool IsActive { get; set; }
        public bool ReactivationRequested { get; set; } = false;
        public DateTime CreatedAt { get; set; }
        public required string StationId { get; set; }
        public required string StationName { get; set; }
        public required string StationLocation { get; set; }
    }
}