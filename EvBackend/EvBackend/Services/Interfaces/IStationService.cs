// --------------------------------------------------------------
// File Name: IStationService.cs
// Author: Denuwan Sathsara
// Description: Station management service methods for the system
// Created On: 13/09/2025
// --------------------------------------------------------------

using System;
using EvBackend.Models.DTOs;

namespace EvBackend.Services.Interfaces;

public interface IStationService
{
        Task<StationDto> CreateStationAsync(CreateStationDto dto);
        Task<StationDto> UpdateStationAsync(string stationId, UpdateStationDto dto);
        Task<bool> DeactivateStationAsync(string stationId);
        Task<StationDto> GetStationByIdAsync(string stationId);
        Task<IEnumerable<StationDto>> GetAllStationsAsync(bool onlyActive = false);
        Task<IEnumerable<StationDto>> SearchStationsAsync(string type, string location);
        Task<IEnumerable<StationDto>> GetNearbyStationsAsync(double latitude, double longitude, double radiusKm);
        Task<bool> HasActiveBookingsAsync(string stationId); // extra logic
        Task<IEnumerable<StationNameDto>> GetStationNameSuggestionsAsync(string? type = null, string? location = null);

}
