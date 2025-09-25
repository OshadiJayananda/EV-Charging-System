// --------------------------------------------------------------
// File Name: UserDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object for user information
// Created On: 13/09/2025
// --------------------------------------------------------------

using System.ComponentModel.DataAnnotations;

namespace EvBackend.Models.DTOs
{
    public enum UserRole
    {
        Admin,
        Operator
    }
    public class UserDto
    {
        public string Id { get; set; }
        [Required]
        [StringLength(100)]
        public string FullName { get; set; }
        [Required]
        [EmailAddress]
        public string Email { get; set; }
        public string PasswordHash { get; set; }
        [Required]
        [EnumDataType(typeof(UserRole), ErrorMessage = "Role must be either 'Admin' or 'Operator'.")]
        public string Role { get; set; }
        public bool IsActive { get; set; }
        public DateTime CreatedAt { get; set; }
    }

    public class UpdateUserDto
    {
        [Required]
        [StringLength(100)]
        public string FullName { get; set; }
        [Required]
        [EmailAddress]
        public string Email { get; set; }
        public bool IsActive { get; set; }
        public DateTime CreatedAt { get; set; }
    }
}
