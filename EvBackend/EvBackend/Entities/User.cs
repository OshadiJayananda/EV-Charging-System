// --------------------------------------------------------------
// File Name: User.cs
// Author: Hasindu Koshitha
// Description: Database entity representing a user
// Created On: 13/09/2025
// Updated On: 06/10/2025 - Added station assignment fields for operators
// --------------------------------------------------------------

using MongoDB.Bson.Serialization.Attributes;
using MongoDB.Bson;

namespace EvBackend.Entities
{
    public class User
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; }

        [BsonElement("fullName")]
        public string FullName { get; set; }

        [BsonElement("email")]
        public string Email { get; set; }

        [BsonElement("passwordHash")]
        public string PasswordHash { get; set; }

        [BsonElement("role")]
        public string Role { get; set; }

        [BsonElement("isActive")]
        public bool IsActive { get; set; } = true;

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("passwordResetToken")]
        [BsonIgnoreIfNull]
        public string? PasswordResetToken { get; set; }

        [BsonElement("passwordResetTokenExpiration")]
        [BsonIgnoreIfNull]
        public DateTime? PasswordResetTokenExpiration { get; set; }

        // ✅ Optional fields for operators (added to fix stationId mismatch error)
        [BsonElement("stationId")]
        [BsonIgnoreIfNull]
        public string? StationId { get; set; }

        [BsonElement("stationName")]
        [BsonIgnoreIfNull]
        public string? StationName { get; set; }

        [BsonElement("stationLocation")]
        [BsonIgnoreIfNull]
        public string? StationLocation { get; set; }
    }
}
