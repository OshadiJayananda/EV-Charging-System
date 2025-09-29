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
        private readonly GeocodingService _geocoding;


        public StationService(IMongoDatabase database, GeocodingService geocoding)
        {
            _stations = database.GetCollection<Station>("Stations");
            _geocoding = geocoding;
        }

        public async Task<StationDto> CreateStationAsync(CreateStationDto dto)
        {
            var coords = await _geocoding.GetCoordinatesAsync(dto.Location);

            var station = new Station
            {
                Name = dto.Name,
                Location = dto.Location,
                Type = dto.Type,
                Capacity = dto.Capacity,
                AvailableSlots = dto.Capacity,
                IsActive = true,
                Latitude = coords?.lat ?? 0,
                Longitude = coords?.lng ?? 0
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
            if (await HasActiveBookingsAsync(stationId))
                throw new InvalidOperationException("Cannot deactivate station with active bookings");

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
            var bookings = _stations.Database.GetCollection<Booking>("Bookings");

            // Active = Pending or Approved
            var filter = Builders<Booking>.Filter.Eq(b => b.StationId, stationId) &
                         Builders<Booking>.Filter.In(b => b.Status, new[] { "Pending", "Approved" });

            return await bookings.Find(filter).AnyAsync();
        }

        public async Task<IEnumerable<StationDto>> GetNearbyStationsAsync(double latitude, double longitude, double radiusKm)
        {
            var list = await _stations.Find(s => s.IsActive).ToListAsync();

            // Simple haversine formula filter
            return list.Where(s => GetDistanceKm(latitude, longitude, s.Latitude, s.Longitude) <= radiusKm)
                       .Select(ToDto);
        }

        private double GetDistanceKm(double lat1, double lon1, double lat2, double lon2)
        {
            const double R = 6371; // km
            var dLat = (lat2 - lat1) * Math.PI / 180.0;
            var dLon = (lon2 - lon1) * Math.PI / 180.0;

            var a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                    Math.Cos(lat1 * Math.PI / 180.0) * Math.Cos(lat2 * Math.PI / 180.0) *
                    Math.Sin(dLon / 2) * Math.Sin(dLon / 2);

            return R * 2 * Math.Asin(Math.Sqrt(a));
        }


        private static StationDto ToDto(Station station) =>
            new StationDto
            {
                StationId = station.StationId,
                Name = station.Name,
                Location = station.Location,
                Latitude = station.Latitude,
                Longitude = station.Longitude,
                Type = station.Type,
                Capacity = station.Capacity,
                AvailableSlots = station.AvailableSlots,
                IsActive = station.IsActive
            };
    }
}
