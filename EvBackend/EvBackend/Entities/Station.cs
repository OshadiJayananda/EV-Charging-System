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

        [BsonElement("type")]
        public string Type { get; set; } // AC or DC

        [BsonElement("capacity")]
        public int Capacity { get; set; } // Total slots

        [BsonElement("availableSlots")]
        public int AvailableSlots { get; set; }

        [BsonElement("isActive")]
        public bool IsActive { get; set; } = true;
    }
}
