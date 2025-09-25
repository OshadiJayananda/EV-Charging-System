// --------------------------------------------------------------
// File Name: CreateEVOwnerDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object used for creating a new electric vehicle owner
// Created On: 13/09/2025
// --------------------------------------------------------------

namespace EvBackend.Models.DTOs
{
    public class CreateEVOwnerDto
    {
        public string NIC { get; set; }
        public string FullName { get; set; }
        public string Email { get; set; }
        public string Password { get; set; }
        public bool IsActive { get; set; } = false;
    }
}
