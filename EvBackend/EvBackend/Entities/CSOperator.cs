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
        // ✅ No need to re-declare StationId, StationName, StationLocation
        // They already exist in base class User
        // public bool ReactivationRequested { get; set; } = false;
        public CSOperator() : base()
        {
            Role = "Operator"; // Ensure consistency
        }
    }
}