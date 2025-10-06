// --------------------------------------------------------------
// File Name: CSOperator.cs
// Author: Hasindu Koshitha
// Description: Database entity representing a charging station operator
// Created On: 13/09/2025
// --------------------------------------------------------------

using MongoDB.Bson.Serialization.Attributes;

namespace EvBackend.Entities
{
    public class CSOperator : User
    {
        [BsonElement("stationId")]
        public string StationId { get; set; }

        [BsonElement("stationName")]
        public string StationName { get; set; }

        [BsonElement("stationLocation")]
        public string StationLocation { get; set; }

        // Ensure CreatedAt is properly initialized
        public CSOperator()
        {
            CreatedAt = DateTime.UtcNow;
        }
    }
}