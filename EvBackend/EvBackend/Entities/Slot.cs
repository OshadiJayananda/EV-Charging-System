// --------------------------------------------------------------
// File Name: Slot.cs
// Author: Miyuri Lokuhewage
// Description: Database entity representing Slot details.
// Created On: 26/09/2025
// --------------------------------------------------------------



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
        public int Number { get; set; }
        public string ConnectorType { get; set; }
        public string Status { get; set; } // Available, Booked, Inactive
        public DateTime? StartTime { get; set; }
        public DateTime? EndTime { get; set; }
    }
}
