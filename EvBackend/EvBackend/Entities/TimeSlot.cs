using System;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace EvBackend.Entities;

public class TimeSlot
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string TimeSlotId { get; set; } // Automatically generated ObjectId for time slot

    public string StationId { get; set; }  // Reference to station
    public string SlotId { get; set; }     // Reference to slot
    public DateTime StartTime { get; set; }  // Start time of the slot
    public DateTime EndTime { get; set; }    // End time of the slot
    public string Status { get; set; }       // Status (Available, Booked, etc.)
}

