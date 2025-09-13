// --------------------------------------------------------------
// File Name: CSOperatorDto.cs
// Author: Hasindu Koshitha
// Description: Data Transfer Object for Charging Station Operator
// Created On: 13/09/2025
// --------------------------------------------------------------

namespace EvBackend.Models.DTOs
{
    public class CSOperatorDto : UserDto
    {
        public string StationId { get; set; }
        public string StationName { get; set; }
        public string StationLocation { get; set; }
    }
}
