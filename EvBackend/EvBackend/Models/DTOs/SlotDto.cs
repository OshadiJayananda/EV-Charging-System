// --------------------------------------------------------------
// File Name: SlotDto.cs
// Author: Miyuri Lokuhewage
// Description: DTOs for slot management. Cleaned to separate creation,
// updates, and response DTOs.
// Created/Updated On: 27/09/2025
// --------------------------------------------------------------

namespace EvBackend.Models.DTOs
{
    public class SlotDto
    {
        public string SlotId { get; set; }
        public string StationId { get; set; }
        public string ConnectorType { get; set; }
        public string Status { get; set; } // Available, Booked, Inactive
        public int Number { get; set; } // Slot number within the station
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }

    // ✅ Only needs connector type, stationId comes from route
    public class CreateSlotDto
    {
        public string ConnectorType { get; set; }
    }

    // ✅ Only status can be updated
    public class UpdateSlotDto
    {
        public string Status { get; set; }
    }
}
