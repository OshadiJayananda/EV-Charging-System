// --------------------------------------------------------------
// File Name: CreateCSOperatorDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object used for creating a new charging station operator
// Created On: 13/09/2025
// --------------------------------------------------------------

namespace EvBackend.Models.DTOs
{
    public class CreateCSOperatorDto : CreateUserDto
    {
        public string StationId { get; set; }
        public string StationName { get; set; }
        public string StationLocation { get; set; }
    }
}
