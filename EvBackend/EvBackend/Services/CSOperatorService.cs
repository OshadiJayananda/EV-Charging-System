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
        private readonly INotificationService _notificationService;

        public CSOperatorService(IMongoDatabase database, IConfiguration config, IOptions<MongoDbSettings> settings, INotificationService notificationService)
        {
            _operators = database.GetCollection<CSOperator>(settings.Value.UsersCollectionName);
            _stations = database.GetCollection<Station>("Stations");
            _config = config;
            _notificationService = notificationService;
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
                Role = "Operator",
                IsActive = dto.IsActive,
                ReactivationRequested = false,
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
                ReactivationRequested = operatorEntity.ReactivationRequested,
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
                ReactivationRequested = op.ReactivationRequested,
                CreatedAt = op.CreatedAt,
                StationId = op.StationId,
                StationName = op.StationName,
                StationLocation = op.StationLocation
            };
        }

        public async Task<PagedResultDto<CSOperatorDto>> GetAllPaginatedOperators(int page, int pageSize)
        {
            var filter = Builders<CSOperator>.Filter.Eq(op => op.Role, "Operator");

            var totalCount = await _operators.CountDocumentsAsync(filter);

            var ops = await _operators.Find(filter)
                .Skip((page - 1) * pageSize)
                .Limit(pageSize)
                .ToListAsync();

            var operatorDtos = ops.Select(op => new CSOperatorDto
            {
                Id = op.Id,
                FullName = op.FullName,
                Email = op.Email,
                Role = op.Role,
                IsActive = op.IsActive,
                ReactivationRequested = op.ReactivationRequested,
                CreatedAt = op.CreatedAt,
                StationId = op.StationId,
                StationName = op.StationName,
                StationLocation = op.StationLocation
            });

            return new PagedResultDto<CSOperatorDto>
            {
                Items = operatorDtos,
                TotalCount = totalCount
            };
        }

        public async Task<IEnumerable<CSOperatorDto>> GetAllOperators(int page, int pageSize)
        {
            var filter = Builders<CSOperator>.Filter.Eq(op => op.Role, "Operator");

            var ops = await _operators.Find(filter)
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
                ReactivationRequested = op.ReactivationRequested,
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
                ReactivationRequested = updatedOp.ReactivationRequested,
                CreatedAt = updatedOp.CreatedAt,
                StationId = updatedOp.StationId,
                StationName = updatedOp.StationName,
                StationLocation = updatedOp.StationLocation
            };
        }

        public async Task<bool> ChangeOperatorStatus(string id, bool isActive)
        {
            var updateBuilder = Builders<CSOperator>.Update;
            UpdateDefinition<CSOperator> update;

            if (isActive)
            {
                // When activating, clear the reactivation request and set active
                update = updateBuilder
                    .Set(o => o.IsActive, true)
                    .Set(o => o.ReactivationRequested, false);
            }
            else
            {
                // When deactivating, just set active to false (keep reactivation request as is)
                update = updateBuilder.Set(o => o.IsActive, false);
            }

            var result = await _operators.UpdateOneAsync(o => o.Id == id, update);

            var operatorEntity = await _operators.Find(o => o.Id == id).FirstOrDefaultAsync();
            string operatorName = operatorEntity?.FullName ?? "Unknown";

            if (isActive)
            {
                await _notificationService.SendNotification(id, "Your operator account has been activated.");
            }
            else
            {
                await _notificationService.SendNotification(id, "Your operator account has been deactivated.");
                await _notificationService.SendNotificationToAdmins($"Operator {operatorName} account deactivated.");
            }
            return result.ModifiedCount > 0;
        }

        // Reactivation methods
        public async Task<bool> RequestReactivation(string id)
        {
            var operatorEntity = await _operators.Find(o => o.Id == id).FirstOrDefaultAsync();
            if (operatorEntity == null) throw new KeyNotFoundException("Operator not found");

            // Check if account is already active
            if (operatorEntity.IsActive)
                throw new InvalidOperationException("Account is already active.");

            // Check if reactivation is already requested
            if (operatorEntity.ReactivationRequested)
                throw new InvalidOperationException("Reactivation already requested. Please wait for admin approval.");

            // Set reactivation requested to true
            var update = Builders<CSOperator>.Update.Set(o => o.ReactivationRequested, true);
            var result = await _operators.UpdateOneAsync(o => o.Id == id, update);

            // Notify admins about the reactivation request
            await _notificationService.SendNotificationToAdmins($"Operator {operatorEntity.FullName} has requested account reactivation.");

            return result.ModifiedCount > 0;
        }

        public async Task<int> GetReactivationRequestCount()
        {
            var filter = Builders<CSOperator>.Filter.And(
                Builders<CSOperator>.Filter.Eq(o => o.Role, "Operator"),
                Builders<CSOperator>.Filter.Eq(o => o.ReactivationRequested, true),
                Builders<CSOperator>.Filter.Eq(o => o.IsActive, false)
            );

            var count = await _operators.CountDocumentsAsync(filter);
            return (int)count;
        }

        public async Task<IEnumerable<CSOperatorDto>> GetOperatorsWithReactivationRequests()
        {
            var filter = Builders<CSOperator>.Filter.And(
                Builders<CSOperator>.Filter.Eq(o => o.Role, "Operator"),
                Builders<CSOperator>.Filter.Eq(o => o.ReactivationRequested, true),
                Builders<CSOperator>.Filter.Eq(o => o.IsActive, false)
            );

            var operators = await _operators.Find(filter).ToListAsync();

            return operators.Select(op => new CSOperatorDto
            {
                Id = op.Id,
                FullName = op.FullName,
                Email = op.Email,
                Role = op.Role,
                IsActive = op.IsActive,
                ReactivationRequested = op.ReactivationRequested,
                CreatedAt = op.CreatedAt,
                StationId = op.StationId,
                StationName = op.StationName,
                StationLocation = op.StationLocation
            });
        }

        public async Task<bool> ClearReactivationRequest(string id)
        {
            var update = Builders<CSOperator>.Update.Set(o => o.ReactivationRequested, false);
            var result = await _operators.UpdateOneAsync(o => o.Id == id, update);
            return result.ModifiedCount > 0;
        }
    }
}