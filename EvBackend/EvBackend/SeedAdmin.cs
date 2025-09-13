// --------------------------------------------------------------
// File Name: SeedAdmin.cs
// Author: Hasindu Koshitha
// Description: Seeder for initial Admin account in MongoDB
// Created On: 13/09/2025
// --------------------------------------------------------------

using System;
using MongoDB.Driver;
using EvBackend.Entities;

namespace EvBackend.Seeders
{
    public static class SeedAdmin
    {
        public static void Seed(IMongoDatabase database)
        {
            var admins = database.GetCollection<Admin>("Users");
            var email = "admin@example.com";
            if (admins.Find(a => a.Email == email).Any())
            {
                Console.WriteLine("Admin already exists.");
                return;
            }
            var admin = new Admin
            {
                Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
                FullName = "Super Admin",
                Email = email,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword("Admin@123"),
                Role = "Admin",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            };
            admins.InsertOne(admin);
            Console.WriteLine("Seeded initial admin account.");
        }
    }
}
