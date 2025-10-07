using System;

namespace EvBackend.Models.DTOs
{
    public class SlotDto
    {
        public string SlotId { get; set; }
        public string StationId { get; set; }
        public string ConnectorType { get; set; }
        public string Status { get; set; }
        public int Number { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }

    public class CreateSlotDto
    {
        public string ConnectorType { get; set; }
    }

    public class UpdateSlotDto
    {
        public string Status { get; set; }
    }
}
