// --------------------------------------------------------------
// File Name: AdminService.cs
// Author: Hasindu Koshitha
// Description: Implements business logic for admin management
// Created On: 13/09/2025
// --------------------------------------------------------------

using EvBackend.Entities;
using EvBackend.Models.DTOs;
using EvBackend.Services.Interfaces;
using EvBackend.Settings;
using Microsoft.Extensions.Options;
using MongoDB.Driver;

namespace EvBackend.Services
{
    public class AdminService : IAdminService
    {
        private readonly IMongoCollection<Admin> _admins;
        private readonly IConfiguration _config;

        public AdminService(IMongoDatabase database, IConfiguration config, IOptions<MongoDbSettings> settings)
        {
            _admins = database.GetCollection<Admin>(settings.Value.UsersCollectionName);
            _config = config;
        }

        public async Task<UserDto> CreateAdmin(CreateUserDto dto)
        {
            if (await _admins.Find(a => a.Email == dto.Email).AnyAsync())
                throw new InvalidOperationException("Email already in use");

            dto.Email = dto.Email.Trim().ToLower();

            var admin = new Admin
            {
                Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
                FullName = dto.FullName,
                Email = dto.Email,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password),
                Role = "Admin",
                IsActive = dto.IsActive
            };
            await _admins.InsertOneAsync(admin);
            return new UserDto
            {
                Id = admin.Id,
                FullName = admin.FullName,
                Email = admin.Email,
                Role = admin.Role,
                IsActive = admin.IsActive,
                CreatedAt = admin.CreatedAt
            };
        }

        public async Task<UserDto> GetAdminById(string id)
        {
            var admin = await _admins.Find(a => a.Id == id).FirstOrDefaultAsync();
            if (admin == null) throw new KeyNotFoundException("Admin not found");
            return new UserDto
            {
                Id = admin.Id,
                FullName = admin.FullName,
                Email = admin.Email,
                Role = admin.Role,
                IsActive = admin.IsActive,
                CreatedAt = admin.CreatedAt
            };
        }

        public async Task<IEnumerable<UserDto>> GetAllAdmins(int page, int pageSize)
        {
            var admins = await _admins.Find(_ => true)
                .Skip((page - 1) * pageSize)
                .Limit(pageSize)
                .ToListAsync();
            return admins.Select(admin => new UserDto
            {
                Id = admin.Id,
                FullName = admin.FullName,
                Email = admin.Email,
                Role = admin.Role,
                IsActive = admin.IsActive,
                CreatedAt = admin.CreatedAt
            });
        }

        public async Task<UserDto> UpdateAdmin(string id, UserDto dto)
        {
            var update = Builders<Admin>.Update
                .Set(a => a.FullName, dto.FullName)
                .Set(a => a.Email, dto.Email)
                .Set(a => a.Role, dto.Role)
                .Set(a => a.IsActive, dto.IsActive);
            var result = await _admins.UpdateOneAsync(a => a.Id == id, update);
            if (result.MatchedCount == 0) throw new KeyNotFoundException("Admin not found");
            var updatedAdmin = await _admins.Find(a => a.Id == id).FirstOrDefaultAsync();
            return new UserDto
            {
                Id = updatedAdmin.Id,
                FullName = updatedAdmin.FullName,
                Email = updatedAdmin.Email,
                Role = updatedAdmin.Role,
                IsActive = updatedAdmin.IsActive,
                CreatedAt = updatedAdmin.CreatedAt
            };
        }

        public async Task<bool> ChangeAdminStatus(string id, bool isActive)
        {
            var update = Builders<Admin>.Update.Set(a => a.IsActive, isActive);
            var result = await _admins.UpdateOneAsync(a => a.Id == id, update);
            return result.ModifiedCount > 0;
        }
    }
}
