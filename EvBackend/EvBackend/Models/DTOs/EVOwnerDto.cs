// --------------------------------------------------------------
// File Name: EVOwnerDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object for EV Owner
// Created On: 13/09/2025
// --------------------------------------------------------------

namespace EvBackend.Models.DTOs
{
    public class EVOwnerDto
    {
        public string NIC { get; set; }
        public string FullName { get; set; }
        public string Email { get; set; }
        public string PasswordHash { get; set; }
        public bool IsActive { get; set; }
        public DateTime CreatedAt { get; set; }
    }
}
