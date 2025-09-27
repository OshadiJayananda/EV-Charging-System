// --------------------------------------------------------------
// File Name: EVOwnerService.cs
// Author: Hasindu Koshitha
// Description: Implements business logic for electric vehicle owner management
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
    public class EVOwnerService : IEVOwnerService
    {
        private readonly IMongoCollection<EVOwner> _owners;

        public EVOwnerService(IMongoDatabase database, IOptions<MongoDbSettings> settings)
        {
            _owners = database.GetCollection<EVOwner>(settings.Value.EVOwnersCollectionName);
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
                throw new ArgumentException("NIC already in use");

            if (await _owners.Find(u => u.Email == dto.Email).AnyAsync())
                throw new ArgumentException("Email already in use");

            var owner = new EVOwner
            {
                NIC = dto.NIC,
                FullName = dto.FullName,
                Email = dto.Email,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password),
                CreatedAt = DateTime.UtcNow
            };

            await _owners.InsertOneAsync(owner);

            return new EVOwnerDto
            {
                FullName = owner.FullName,
                Email = owner.Email,
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
                FullName = owner.FullName,
                Email = owner.Email,
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
                FullName = owner.FullName,
                Email = owner.Email,
                IsActive = owner.IsActive,
                CreatedAt = owner.CreatedAt,
                NIC = owner.NIC
            };
            return Task.FromResult(dto);
        }

        public async Task<EVOwnerDto> UpdateEVOwner(string nic, UpdateEVOwnerDto dto)
        {
            var update = Builders<EVOwner>.Update
                .Set(o => o.FullName, dto.FullName)
                .Set(o => o.Email, dto.Email);

            var result = await _owners.FindOneAndUpdateAsync(
                o => o.NIC == nic,
                update,
                new FindOneAndUpdateOptions<EVOwner> { ReturnDocument = ReturnDocument.After });

            if (result == null)
                throw new KeyNotFoundException("EV Owner not found");

            return new EVOwnerDto
            {
                NIC = result.NIC,
                FullName = result.FullName,
                Email = result.Email,
                IsActive = result.IsActive,
                CreatedAt = result.CreatedAt
            };
        }

    }
}
