// --------------------------------------------------------------
// File Name: EVOwnerService.cs
// Author: Hasindu Koshitha
// Description: Implements business logic for electric vehicle owner management
// Created On: 13/09/2025
// --------------------------------------------------------------

using EvBackend.Entities;
using EvBackend.Models.DTOs;
using EvBackend.Settings;
using Microsoft.Extensions.Options;
using MongoDB.Driver;

namespace EvBackend.Services
{
    public class EVOwnerService : IEVOwnerService
    {
        private readonly IMongoCollection<EVOwner> _owners;
        private readonly IConfiguration _config;

        public EVOwnerService(IMongoDatabase database, IConfiguration config, IOptions<MongoDbSettings> settings)
        {
            _owners = database.GetCollection<EVOwner>(settings.Value.UsersCollectionName);
            _config = config;
        }

        public Task<bool> ChangeEVOwnerStatus(string nic, bool isActive)
        {
            var update = Builders<EVOwner>.Update.Set(o => o.IsActive, isActive);
            var result = _owners.UpdateOne(o => o.NIC == nic, update);
            return Task.FromResult(result.ModifiedCount > 0);
        }

        public async Task<EVOwnerDto> CreateEVOwner(CreateEVOwnerDto dto)
        {
            if (await _owners.Find(u => u.NIC == dto.NIC).AnyAsync())
                throw new InvalidOperationException("NIC already in use");

            var owner = new EVOwner
            {
                NIC = dto.NIC,
                FullName = dto.FullName,
                Email = dto.Email,
                PasswordHash = HashPassword(dto.Password),
                IsActive = true
            };

            await _owners.InsertOneAsync(owner);

            return new EVOwnerDto
            {
                Id = owner.Id,
                FullName = owner.FullName,
                Email = owner.Email,
                Role = "EVOwner",
                IsActive = owner.IsActive,
                CreatedAt = owner.CreatedAt,
                NIC = owner.NIC
            };
        }

        public Task<IEnumerable<EVOwnerDto>> GetAllEVOwners(int page, int pageSize)
        {
            var owners = _owners.Find(_ => true)
                .Skip((page - 1) * pageSize)
                .Limit(pageSize)
                .ToList();
            var dtos = owners.Select(owner => new EVOwnerDto
            {
                Id = owner.Id,
                FullName = owner.FullName,
                Email = owner.Email,
                Role = "EVOwner",
                IsActive = owner.IsActive,
                CreatedAt = owner.CreatedAt,
                NIC = owner.NIC
            });
            return Task.FromResult(dtos);
        }

        public Task<EVOwnerDto> GetEVOwnerByNIC(string nic)
        {
            var owner = _owners.Find(o => o.NIC == nic).FirstOrDefault();
            if (owner == null) throw new KeyNotFoundException("EV Owner not found");
            var dto = new EVOwnerDto
            {
                Id = owner.Id,
                FullName = owner.FullName,
                Email = owner.Email,
                Role = "EVOwner",
                IsActive = owner.IsActive,
                CreatedAt = owner.CreatedAt,
                NIC = owner.NIC
            };
            return Task.FromResult(dto);
        }

        public Task<EVOwnerDto> UpdateEVOwner(string nic, CreateEVOwnerDto dto)
        {
            var update = Builders<EVOwner>.Update
                .Set(o => o.FullName, dto.FullName)
                .Set(o => o.Email, dto.Email)
                .Set(o => o.NIC, dto.NIC)
                .Set(o => o.IsActive, dto.IsActive);
            var result = _owners.UpdateOne(o => o.NIC == nic, update);
            if (result.MatchedCount == 0) throw new KeyNotFoundException("EV Owner not found");
            var updatedOwner = _owners.Find(o => o.NIC == dto.NIC).FirstOrDefault();
            var updatedDto = new EVOwnerDto
            {
                Id = updatedOwner.Id,
                FullName = updatedOwner.FullName,
                Email = updatedOwner.Email,
                Role = "EVOwner",
                IsActive = updatedOwner.IsActive,
                CreatedAt = updatedOwner.CreatedAt,
                NIC = updatedOwner.NIC
            };
            return Task.FromResult(updatedDto);
        }

        private static string HashPassword(string password)
        {
            using (var sha = System.Security.Cryptography.SHA256.Create())
            {
                var bytes = System.Text.Encoding.UTF8.GetBytes(password);
                var hash = sha.ComputeHash(bytes);
                return BitConverter.ToString(hash).Replace("-", "").ToLowerInvariant();
            }
        }
    }
}
