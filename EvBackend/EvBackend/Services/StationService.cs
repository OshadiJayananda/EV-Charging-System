using System;
using EvBackend.Entities;
using EvBackend.Models.DTOs;
using EvBackend.Services.Interfaces;
using MongoDB.Driver;

namespace EvBackend.Services
{
    public class StationService : IStationService
    {
        private readonly IMongoCollection<Station> _stations;

        public StationService(IMongoDatabase database)
        {
            _stations = database.GetCollection<Station>("Stations");
        }

        public async Task<StationDto> CreateStationAsync(CreateStationDto dto)
        {
            var station = new Station
            {
                Name = dto.Name,
                Location = dto.Location,
                Type = dto.Type,
                Capacity = dto.Capacity,
                AvailableSlots = dto.Capacity,
                IsActive = true
            };

            await _stations.InsertOneAsync(station);
            return ToDto(station);
        }

        public async Task<StationDto> UpdateStationAsync(string stationId, UpdateStationDto dto)
        {
            var filter = Builders<Station>.Filter.Eq(s => s.StationId, stationId);
            var update = Builders<Station>.Update
                .Set(s => s.Name, dto.Name)
                .Set(s => s.Location, dto.Location)
                .Set(s => s.Type, dto.Type)
                .Set(s => s.Capacity, dto.Capacity)
                .Set(s => s.AvailableSlots, dto.AvailableSlots);

            var options = new FindOneAndUpdateOptions<Station>
            {
                ReturnDocument = ReturnDocument.After // return updated doc
            };

            var updatedStation = await _stations.FindOneAndUpdateAsync(filter, update, options);
            return updatedStation != null ? ToDto(updatedStation) : null;
        }

        public async Task<bool> DeactivateStationAsync(string stationId)
        {
            // Business rule: cannot deactivate if active bookings exist
            if (await HasActiveBookingsAsync(stationId))
                return false;

            var filter = Builders<Station>.Filter.Eq(s => s.StationId, stationId);
            var update = Builders<Station>.Update.Set(s => s.IsActive, false);

            var result = await _stations.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        public async Task<StationDto> GetStationByIdAsync(string stationId)
        {
            var station = await _stations.Find(s => s.StationId == stationId).FirstOrDefaultAsync();
            return station != null ? ToDto(station) : null;
        }

        public async Task<IEnumerable<StationDto>> GetAllStationsAsync(bool onlyActive = false)
        {
            var filter = onlyActive
                ? Builders<Station>.Filter.Eq(s => s.IsActive, true)
                : Builders<Station>.Filter.Empty;

            var stations = await _stations.Find(filter).ToListAsync();
            return stations.Select(ToDto);
        }

        public async Task<IEnumerable<StationDto>> SearchStationsAsync(string type, string location)
        {
            var filter = Builders<Station>.Filter.Empty;

            if (!string.IsNullOrEmpty(type))
                filter &= Builders<Station>.Filter.Eq(s => s.Type, type);

            if (!string.IsNullOrEmpty(location))
                filter &= Builders<Station>.Filter.Regex(
                    s => s.Location, new MongoDB.Bson.BsonRegularExpression(location, "i")
                );

            var stations = await _stations.Find(filter).ToListAsync();
            return stations.Select(ToDto);
        }

        public async Task<bool> HasActiveBookingsAsync(string stationId)
        {
            // TODO: integrate with BookingService
            return await Task.FromResult(false);
        }

        private static StationDto ToDto(Station station) =>
            new StationDto
            {
                StationId = station.StationId,
                Name = station.Name,
                Location = station.Location,
                Type = station.Type,
                Capacity = station.Capacity,
                AvailableSlots = station.AvailableSlots,
                IsActive = station.IsActive
            };
    }
}
