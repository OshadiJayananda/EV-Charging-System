// --------------------------------------------------------------
// File Name: CSOperatorService.cs
// Author: Hasindu Koshitha
// Description: Implements business logic for charging station operator management
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
    public class CSOperatorService : ICSOperatorService
    {
        private readonly IMongoCollection<CSOperator> _operators;
        private readonly IMongoCollection<Station> _stations;
        private readonly IConfiguration _config;

        public CSOperatorService(IMongoDatabase database, IConfiguration config, IOptions<MongoDbSettings> settings)
        {
            _operators = database.GetCollection<CSOperator>(settings.Value.UsersCollectionName);
            _stations = database.GetCollection<Station>("Stations");
            _config = config;
        }

        public async Task<CSOperatorDto> CreateOperator(CreateCSOperatorDto dto)
        {
            // Check if email already exists
            if (await _operators.Find(o => o.Email == dto.Email).AnyAsync())
                throw new InvalidOperationException("Email already in use");

            // Validate that the station exists and is active
            var station = await _stations.Find(s => s.StationId == dto.StationId && s.IsActive).FirstOrDefaultAsync();
            if (station == null)
                throw new ArgumentException("Station not found or inactive");

            // Verify station details match
            if (station.Name != dto.StationName || station.Location != dto.StationLocation)
                throw new ArgumentException("Station details do not match");

            var operatorEntity = new CSOperator
            {
                Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
                FullName = dto.FullName,
                Email = dto.Email,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password),
                Role = "CSOperator",
                IsActive = dto.IsActive,
                StationId = dto.StationId,
                StationName = dto.StationName,
                StationLocation = dto.StationLocation,
                CreatedAt = DateTime.UtcNow
            };

            await _operators.InsertOneAsync(operatorEntity);

            return new CSOperatorDto
            {
                Id = operatorEntity.Id,
                FullName = operatorEntity.FullName,
                Email = operatorEntity.Email,
                Role = operatorEntity.Role,
                IsActive = operatorEntity.IsActive,
                CreatedAt = operatorEntity.CreatedAt,
                StationId = operatorEntity.StationId,
                StationName = operatorEntity.StationName,
                StationLocation = operatorEntity.StationLocation
            };
        }

        public async Task<CSOperatorDto> GetOperatorById(string id)
        {
            var op = await _operators.Find(o => o.Id == id).FirstOrDefaultAsync();
            if (op == null) throw new KeyNotFoundException("Operator not found");
            return new CSOperatorDto
            {
                Id = op.Id,
                FullName = op.FullName,
                Email = op.Email,
                Role = op.Role,
                IsActive = op.IsActive,
                CreatedAt = op.CreatedAt,
                StationId = op.StationId,
                StationName = op.StationName,
                StationLocation = op.StationLocation
            };
        }

        public async Task<IEnumerable<CSOperatorDto>> GetAllOperators(int page, int pageSize)
        {
            var ops = await _operators.Find(_ => true)
                .Skip((page - 1) * pageSize)
                .Limit(pageSize)
                .ToListAsync();
            return ops.Select(op => new CSOperatorDto
            {
                Id = op.Id,
                FullName = op.FullName,
                Email = op.Email,
                Role = op.Role,
                IsActive = op.IsActive,
                CreatedAt = op.CreatedAt,
                StationId = op.StationId,
                StationName = op.StationName,
                StationLocation = op.StationLocation
            });
        }

        public async Task<CSOperatorDto> UpdateOperator(string id, UpdateCSOperatorDto dto)
        {
            // Validate station if being updated
            if (!string.IsNullOrEmpty(dto.StationId))
            {
                var station = await _stations.Find(s => s.StationId == dto.StationId && s.IsActive).FirstOrDefaultAsync();
                if (station == null)
                    throw new ArgumentException("Station not found or inactive");

                if (station.Name != dto.StationName || station.Location != dto.StationLocation)
                    throw new ArgumentException("Station details do not match");
            }

            var update = Builders<CSOperator>.Update
                .Set(o => o.FullName, dto.FullName)
                .Set(o => o.Email, dto.Email)
                .Set(o => o.IsActive, dto.IsActive)
                .Set(o => o.StationId, dto.StationId)
                .Set(o => o.StationName, dto.StationName)
                .Set(o => o.StationLocation, dto.StationLocation);

            var result = await _operators.UpdateOneAsync(o => o.Id == id, update);
            if (result.MatchedCount == 0) throw new KeyNotFoundException("Operator not found");

            var updatedOp = await _operators.Find(o => o.Id == id).FirstOrDefaultAsync();
            return new CSOperatorDto
            {
                Id = updatedOp.Id,
                FullName = updatedOp.FullName,
                Email = updatedOp.Email,
                Role = updatedOp.Role,
                IsActive = updatedOp.IsActive,
                CreatedAt = updatedOp.CreatedAt,
                StationId = updatedOp.StationId,
                StationName = updatedOp.StationName,
                StationLocation = updatedOp.StationLocation
            };
        }

        public async Task<bool> ChangeOperatorStatus(string id, bool isActive)
        {
            var update = Builders<CSOperator>.Update.Set(o => o.IsActive, isActive);
            var result = await _operators.UpdateOneAsync(o => o.Id == id, update);
            return result.ModifiedCount > 0;
        }
    }
}