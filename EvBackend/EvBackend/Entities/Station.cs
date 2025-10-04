// --------------------------------------------------------------
// File Name: Station.cs
// Author: Denuwan Sathsara
// Description: Database entity representing Station details.
// Created On: 26/09/2025
// --------------------------------------------------------------

using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace EvBackend.Entities
{
    public class Station
    {
        [BsonId] // Marks this as the MongoDB document _id
        [BsonRepresentation(BsonType.ObjectId)]
        public string StationId { get; set; }

        [BsonElement("name")]
        public string Name { get; set; }

        [BsonElement("location")]
        public string Location { get; set; }

        [BsonElement("latitude")]
        public double Latitude { get; set; }

        [BsonElement("longitude")]
        public double Longitude { get; set; }

        [BsonElement("type")]
        public string Type { get; set; } // AC or DC

        [BsonElement("capacity")]
        public int Capacity { get; set; } // Total slots

        [BsonElement("availableSlots")]
        public int AvailableSlots { get; set; }

        [BsonElement("isActive")]
        public bool IsActive { get; set; } = true;

        [BsonElement("slotIds")]
        public List<string> SlotIds { get; set; } = new();
    }
}
