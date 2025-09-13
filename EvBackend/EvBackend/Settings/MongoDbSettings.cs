// --------------------------------------------------------------
// File Name: MongoDbSettings.cs
// Author: Hasindu Koshitha
// Description: Configuration class for MongoDB connection
// Created On: 13/09/2025
// --------------------------------------------------------------

namespace EvBackend.Settings
{
    public class MongoDbSettings
    {
        public string ConnectionString { get; set; }
        public string DatabaseName { get; set; }
        public string UsersCollectionName { get; set; }
    }
}
