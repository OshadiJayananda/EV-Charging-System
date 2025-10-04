// --------------------------------------------------------------
// File Name: StationService.cs
// Author: Denuwan Sathsara
// Description: Implements business logic for station management
// Created On: 13/09/2025
// --------------------------------------------------------------

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
        private readonly IMongoCollection<Slot> _slots;

        private readonly GeocodingService _geocoding;


        public StationService(IMongoDatabase database, GeocodingService geocoding)
        {
            _stations = database.GetCollection<Station>("Stations");
            _slots = database.GetCollection<Slot>("Slots");
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

            // Insert station first
            await _stations.InsertOneAsync(station);

            // Generate slots
            var slotIds = new List<Slot>();
            for (int i = 1; i <= dto.Capacity; i++)
            {
                var slot = new Slot
                {
                    StationId = station.StationId,
                    SlotId = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
                    ConnectorType = dto.Type, // default same as station type
                    Number = i,
                    Status = "Available"
                };

                slotIds.Add(slot);
            }

            // Insert slots collection
            await _slots.InsertManyAsync(slotIds);

            // Save slot IDs inside station for quick lookup
            station.SlotIds = slotIds.Select(s => s.SlotId).ToList();

            // Update station with slot references
            var filter = Builders<Station>.Filter.Eq(s => s.StationId, station.StationId);
            var update = Builders<Station>.Update.Set(s => s.SlotIds, station.SlotIds);
            await _stations.UpdateOneAsync(filter, update);

            return ToDto(station);
        }


        // public async Task<StationDto> UpdateStationAsync(string stationId, UpdateStationDto dto)
        // {
        //     var station = await _stations.Find(s => s.StationId == stationId).FirstOrDefaultAsync();
        //     if (station == null) return null;

        //     // 1. Update basic station fields
        //     var filter = Builders<Station>.Filter.Eq(s => s.StationId, stationId);
        //     var update = Builders<Station>.Update
        //         .Set(s => s.Name, dto.Name)
        //         .Set(s => s.Location, dto.Location)
        //         .Set(s => s.Type, dto.Type)
        //         .Set(s => s.Capacity, dto.Capacity)
        //         .Set(s => s.AvailableSlots, dto.AvailableSlots);

        //     await _stations.UpdateOneAsync(filter, update);

        //     // 2. Process explicit slot updates (status/connector/remove)
        //     if (dto.SlotUpdates != null && dto.SlotUpdates.Any())
        //     {
        //         foreach (var slotUpdate in dto.SlotUpdates)
        //         {
        //             if (slotUpdate.Action == SlotAction.Remove)
        //             {
        //                 var deleteFilter = Builders<Slot>.Filter.Eq(s => s.SlotId, slotUpdate.SlotId);
        //                 await _slots.DeleteOneAsync(deleteFilter);
        //                 station.SlotIds.Remove(slotUpdate.SlotId);
        //             }
        //             else if (slotUpdate.Action == SlotAction.Update)
        //             {
        //                 var filterSlot = Builders<Slot>.Filter.Eq(s => s.SlotId, slotUpdate.SlotId);
        //                 var updateSlot = Builders<Slot>.Update
        //                     .Set(s => s.ConnectorType, slotUpdate.ConnectorType)
        //                     .Set(s => s.Status, slotUpdate.Status);

        //                 await _slots.UpdateOneAsync(filterSlot, updateSlot);
        //             }
        //         }

        //         // sync SlotIds back
        //         await _stations.UpdateOneAsync(
        //             Builders<Station>.Filter.Eq(s => s.StationId, stationId),
        //             Builders<Station>.Update.Set(s => s.SlotIds, station.SlotIds)
        //         );
        //     }

        //     // 3. Always enforce capacity balancing
        //     var existingSlots = await _slots.Find(s => s.StationId == stationId).ToListAsync();
        //     var currentCount = existingSlots.Count;

        //     if (dto.Capacity > currentCount)
        //     {
        //         // ➕ Add missing slots
        //         var newSlots = new List<Slot>();
        //         for (int i = currentCount + 1; i <= dto.Capacity; i++)
        //         {
        //             var slot = new Slot
        //             {
        //                 StationId = stationId,
        //                 Number = i,
        //                 ConnectorType = dto.Type ?? station.Type,
        //                 Status = "Available"
        //             };
        //             newSlots.Add(slot);
        //         }

        //         if (newSlots.Any())
        //         {
        //             await _slots.InsertManyAsync(newSlots);
        //             station.SlotIds.AddRange(newSlots.Select(s => s.SlotId));
        //             await _stations.UpdateOneAsync(filter,
        //                 Builders<Station>.Update.Set(s => s.SlotIds, station.SlotIds));
        //         }
        //     }
        //     else if (dto.Capacity < currentCount)
        //     {
        //         // ➖ Remove extra slots (from the end)
        //         var slotsToRemove = existingSlots
        //             .OrderByDescending(s => s.Number)
        //             .Take(currentCount - dto.Capacity)
        //             .ToList();

        //         if (slotsToRemove.Any())
        //         {
        //             var slotIdsToRemove = slotsToRemove.Select(s => s.SlotId).ToList();

        //             await _slots.DeleteManyAsync(
        //                 Builders<Slot>.Filter.In(s => s.SlotId, slotIdsToRemove));

        //             station.SlotIds = station.SlotIds.Except(slotIdsToRemove).ToList();
        //             await _stations.UpdateOneAsync(filter,
        //                 Builders<Station>.Update.Set(s => s.SlotIds, station.SlotIds));
        //         }
        //     }

        //     // 4. Return updated DTO
        //     var updatedStation = await _stations.Find(s => s.StationId == stationId).FirstOrDefaultAsync();
        //     return updatedStation != null ? ToDto(updatedStation) : null;
        // }

        public async Task<StationDto> UpdateStationAsync(string stationId, UpdateStationDto dto)
        {
            var station = await _stations.Find(s => s.StationId == stationId).FirstOrDefaultAsync();
            if (station == null) return null;

            var coords = await _geocoding.GetCoordinatesAsync(dto.Location);

            // 1. Update basic station fields (capacity excluded, we’ll recalc later)
            var filter = Builders<Station>.Filter.Eq(s => s.StationId, stationId);
            var update = Builders<Station>.Update
                .Set(s => s.Name, dto.Name)
                .Set(s => s.Location, dto.Location)
                .Set(s => s.Latitude, coords?.lat ?? station.Latitude)
                .Set(s => s.Longitude, coords?.lng ?? station.Longitude)
                .Set(s => s.Type, dto.Type)
                .Set(s => s.AvailableSlots, dto.AvailableSlots);

            await _stations.UpdateOneAsync(filter, update);

            // 2. Handle slot updates
            if (dto.SlotUpdates != null && dto.SlotUpdates.Any())
            {
                foreach (var slotUpdate in dto.SlotUpdates)
                {
                    if (slotUpdate.Action == SlotAction.Remove)
                    {
                        var deleteFilter = Builders<Slot>.Filter.Eq(s => s.SlotId, slotUpdate.SlotId);
                        await _slots.DeleteOneAsync(deleteFilter);
                        station.SlotIds.Remove(slotUpdate.SlotId);
                    }
                    else if (slotUpdate.Action == SlotAction.Update)
                    {
                        var filterSlot = Builders<Slot>.Filter.Eq(s => s.SlotId, slotUpdate.SlotId);
                        var updateSlot = Builders<Slot>.Update
                            .Set(s => s.ConnectorType, slotUpdate.ConnectorType)
                            .Set(s => s.Status, slotUpdate.Status);

                        await _slots.UpdateOneAsync(filterSlot, updateSlot);
                    }
                    else if (slotUpdate.Action == SlotAction.Add)
                    {
                        // Get the highest slot number for this station
                        var lastSlot = await _slots.Find(s => s.StationId == stationId)
                                                   .SortByDescending(s => s.Number)
                                                   .FirstOrDefaultAsync();

                        int nextNumber = lastSlot != null ? lastSlot.Number + 1 : 1;

                        var newSlot = new Slot
                        {
                            StationId = stationId,
                            Number = nextNumber,  // 👈 now based on max existing Number
                            ConnectorType = slotUpdate.ConnectorType,
                            Status = slotUpdate.Status
                        };

                        await _slots.InsertOneAsync(newSlot);

                        station.SlotIds.Add(newSlot.SlotId);

                        // Also update station’s slotIds and capacity
                        await _stations.UpdateOneAsync(
                            Builders<Station>.Filter.Eq(s => s.StationId, stationId),
                            Builders<Station>.Update
                                .Set(s => s.SlotIds, station.SlotIds)
                                .Set(s => s.Capacity, station.SlotIds.Count)
                        );
                    }

                }

                // Sync SlotIds back
                await _stations.UpdateOneAsync(
                    Builders<Station>.Filter.Eq(s => s.StationId, stationId),
                    Builders<Station>.Update.Set(s => s.SlotIds, station.SlotIds)
                );
            }

            // 3. Recalculate capacity = number of slots
            var updatedSlots = await _slots.Find(s => s.StationId == stationId).ToListAsync();
            var capacity = updatedSlots.Count;
            var availableSlots = updatedSlots.Count(s => s.Status == "Available");


            await _stations.UpdateOneAsync(filter,
                Builders<Station>.Update
                .Set(s => s.Capacity, capacity)
                .Set(s => s.AvailableSlots, availableSlots));


            // 4. Return DTO with updated slots
            var updatedStation = await _stations.Find(s => s.StationId == stationId).FirstOrDefaultAsync();
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


        private StationDto ToDto(Station station)
        {
            var slotEntities = _slots.Find(s => s.StationId == station.StationId).ToList();

            return new StationDto
            {
                StationId = station.StationId,
                Name = station.Name,
                Location = station.Location,
                Latitude = station.Latitude,
                Longitude = station.Longitude,
                Type = station.Type,
                Capacity = station.Capacity,
                AvailableSlots = station.AvailableSlots,
                IsActive = station.IsActive,
                Slots = slotEntities.Select(s => new SlotDto
                {
                    SlotId = s.SlotId,
                    StationId = s.StationId,
                    Number = s.Number,
                    ConnectorType = s.ConnectorType,
                    Status = s.Status
                }).ToList()
            };
        }

    }
}
