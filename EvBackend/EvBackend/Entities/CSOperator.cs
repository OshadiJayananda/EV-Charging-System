// --------------------------------------------------------------
// File Name: CSOperator.cs
// Author: Hasindu Koshitha
// Description: Represents a Charging Station Operator user
// --------------------------------------------------------------

using MongoDB.Bson.Serialization.Attributes;
using MongoDB.Bson;

namespace EvBackend.Entities
{
    public class CSOperator : User
    {
        [BsonElement("ReactivationRequested")]
        public bool ReactivationRequested { get; set; } = false;
        public CSOperator() : base()
        {
            Role = "Operator";
        }
    }
}