// // --------------------------------------------------------------
// // File Name: Slot.cs
// // Author: Miyuri Lokuhewage
// // Description: Database entity representing Slot details.
// // Created On: 26/09/2025
// // --------------------------------------------------------------



// using System;
// using MongoDB.Bson;
// using MongoDB.Bson.Serialization.Attributes;

// namespace EvBackend.Entities
// {
//     public class Slot
//     {
//         [BsonId]
//         [BsonRepresentation(BsonType.ObjectId)]
//         public string SlotId { get; set; }

//         public string StationId { get; set; }
//         public int Number { get; set; }
//         public string ConnectorType { get; set; }
//         public string Status { get; set; } // Available, Booked, Inactive
//         public DateTime? StartTime { get; set; }
//         public DateTime? EndTime { get; set; }
//     }
// }


using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace EvBackend.Entities
{
    [BsonIgnoreExtraElements]
    public class Slot
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string SlotId { get; set; } // MongoDB's ObjectId for each slot

        public string StationId { get; set; }  // Reference to station
        public int Number { get; set; }  // Slot number (1, 2, 3, etc.)
        public string Status { get; set; }  // Slot status (Available, Booked, etc.)
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;  // Timestamp for slot creation
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;  // Timestamp for last update

        // Reference to time slots for each slot
        public List<string> TimeSlotIds { get; set; } = new List<string>();
    }

}
