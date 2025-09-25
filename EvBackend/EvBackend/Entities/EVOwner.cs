// --------------------------------------------------------------
// File Name: EVOwner.cs
// Author: Hasindu Koshitha
// Description: Database entity representing an EV owner
// Created On: 13/09/2025
// --------------------------------------------------------------

using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace EvBackend.Entities
{
    public class EVOwner
    {
        [BsonId]
        [BsonRepresentation(BsonType.String)]
        public string NIC { get; set; }

        [BsonElement("fullName")]
        public string FullName { get; set; }

        [BsonElement("email")]
        public string Email { get; set; }

        [BsonElement("passwordHash")]
        public string PasswordHash { get; set; }

        [BsonElement("isActive")]
        public bool IsActive { get; set; } = true;

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}
