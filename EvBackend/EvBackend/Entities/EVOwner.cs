// --------------------------------------------------------------
// File Name: EVOwner.cs
// Author: Hasindu Koshitha
// Description: Database entity representing an EV owner
// Created On: 13/09/2025
// --------------------------------------------------------------


using MongoDB.Bson.Serialization.Attributes;

namespace EvBackend.Entities
{
    public class EVOwner : User
    {
        [BsonId]
        [BsonRepresentation(MongoDB.Bson.BsonType.String)]
        public string NIC { get; set; }
    }
}
