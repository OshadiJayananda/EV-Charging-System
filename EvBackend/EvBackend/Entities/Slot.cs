using System;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace EvBackend.Entities
{
    public class Slot
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string SlotId { get; set; }

        public string StationId { get; set; }
        public string ConnectorType { get; set; }
        public string Status { get; set; } // Available, Booked, Inactive
        public DateTime? StartTime { get; set; }
        public DateTime? EndTime { get; set; }
    }
}
