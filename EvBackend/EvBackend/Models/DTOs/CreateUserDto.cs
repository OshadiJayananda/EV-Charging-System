// --------------------------------------------------------------
// File Name: CreateUserDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object used for creating a new user
// Created On: 13/09/2025
// --------------------------------------------------------------

namespace EvBackend.Models.DTOs
{
    public class CreateUserDto
    {
        public string FullName { get; set; }
        public string Email { get; set; }
        public string Password { get; set; }
        public string Role { get; set; }
        public bool IsActive { get; set; } = true;
    }
}
