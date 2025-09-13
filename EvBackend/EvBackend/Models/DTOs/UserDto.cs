// --------------------------------------------------------------
// File Name: UserDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object for user information
// Created On: 13/09/2025
// --------------------------------------------------------------

namespace EvBackend.Models.DTOs
{
    public class UserDto
    {
        public string Id { get; set; } // GUID or string
        public string FullName { get; set; }
        public string Email { get; set; }
        public string Role { get; set; } // Admin, Operator, Owner
        public bool IsActive { get; set; }
    }
}
