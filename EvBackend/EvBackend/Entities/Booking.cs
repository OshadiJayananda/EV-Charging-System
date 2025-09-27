using System;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace EvBackend.Entities
{
    public class Booking
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string BookingId { get; set; }

        public string StationId { get; set; }
        public string SlotId { get; set; }
        public string OwnerId { get; set; }
        public string Status { get; set; }
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    public string? QrCode { get; set; }
    public DateTime? QrExpiresAt { get; set; }
    public string? QrImageBase64 { get; set; }
    }
}
