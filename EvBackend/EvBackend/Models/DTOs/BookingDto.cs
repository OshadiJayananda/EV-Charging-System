// --------------------------------------------------------------
// File Name: BookingDto.cs
// Author: Miyuri Lokuhewage
// Description: DTOs for booking management. Simplified so that owners
// only provide stationId, connectorType, and time range. OwnerId comes
// from JWT and slot is auto-selected by backend.
// Created/Updated On: 27/09/2025
// --------------------------------------------------------------

namespace EvBackend.Models.DTOs
{
    public class BookingDto
    {
        public string BookingId { get; set; }
        public string StationId { get; set; }
        public string SlotId { get; set; }
        public string OwnerId { get; set; }   // NIC
        public string Status { get; set; }    // Pending, Approved, Finalized, Cancelled
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
        public string? QrCode { get; set; }
        public DateTime? QrExpiresAt { get; set; }
        public string? QrImageBase64 { get; set; }
    }

    // ✅ Owners don’t pass SlotId or OwnerId
    public class CreateBookingDto
    {
        public string StationId { get; set; }
        public string ConnectorType { get; set; } // e.g., Type2, CCS
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
    }

    // ✅ Used for rescheduling only
    public class UpdateBookingDto
    {
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
    }
}
