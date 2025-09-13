// --------------------------------------------------------------
// File Name: EVOwnerDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object for EV Owner
// Created On: 13/09/2025
// --------------------------------------------------------------

namespace EvBackend.Models.DTOs
{
    public class EVOwnerDto : UserDto
    {
        public string NIC { get; set; }
    }
}
