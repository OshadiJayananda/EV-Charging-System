﻿// --------------------------------------------------------------
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
        private readonly INotificationService _notificationService;

        public EVOwnerService(IMongoDatabase database, IOptions<MongoDbSettings> settings, INotificationService notificationService)
        {
            _owners = database.GetCollection<EVOwner>(settings.Value.EVOwnersCollectionName);
            _notificationService = notificationService;
        }

        public async Task<bool> ChangeEVOwnerStatus(string nic, bool isActive)
        {
            var update = Builders<EVOwner>.Update.Set(o => o.IsActive, isActive);
            var result = await _owners.UpdateOneAsync(o => o.NIC == nic, update);

            var owner = await _owners.Find(o => o.NIC == nic).FirstOrDefaultAsync();
            string evOwnerName = owner?.FullName ?? "Unknown";

            if (isActive)
            {
                await _notificationService.SendNotification(nic, "Your account has been activated.");
            }
            else
            {
                await _notificationService.SendNotification(nic, "Your account has been deactivated.");
                await _notificationService.SendNotificationToAdmins($"EV owner {evOwnerName} account deactivated.");
            }
            return result.ModifiedCount > 0;
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
                Phone = dto.Phone,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password),
                CreatedAt = DateTime.UtcNow
            };

            await _owners.InsertOneAsync(owner);

            return new EVOwnerDto
            {
                FullName = owner.FullName,
                Email = owner.Email,
                Phone = owner.Phone,
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
                Phone = owner.Phone,
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
                Phone = owner.Phone,
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
                .Set(o => o.Email, dto.Email)
                .Set(o => o.Phone, dto.Phone);
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
                Phone = result.Phone,
                IsActive = result.IsActive,
                CreatedAt = result.CreatedAt
            };
        }

        public async Task<bool> RequestReactivation(string nic)
        {
            var owner = await _owners.Find(o => o.NIC == nic).FirstOrDefaultAsync();
            if (owner == null) throw new KeyNotFoundException("EV Owner not found");

            if (owner.IsActive)
                throw new InvalidOperationException("Account is already active.");

            if (owner.ReactivationRequested)
                throw new InvalidOperationException("Reactivation already requested.");

            var update = Builders<EVOwner>.Update.Set(o => o.ReactivationRequested, true);
            var result = await _owners.UpdateOneAsync(o => o.NIC == nic, update);
            return result.ModifiedCount > 0;
        }

        public async Task<int> GetReactivationRequestCount()
        {
            var count = await _owners.CountDocumentsAsync(o => o.ReactivationRequested == true);
            return (int)count;
        }



    }
}
