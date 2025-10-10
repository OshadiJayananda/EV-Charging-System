// --------------------------------------------------------------
// File Name: StationController.cs
// Author: Denuwan Sathsara
// Description: Unified controller for station and slot management.
// Includes station CRUD, slot CRUD, and slot availability updates.
// Created/Updated On: 27/09/2025
// --------------------------------------------------------------

using System;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;
using EvBackend.Services.Interfaces;
using EvBackend.Models.DTOs;

namespace EvBackend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class StationController : ControllerBase
    {
        private readonly IStationService _stationService;

        public StationController(IStationService stationService)
        {
            _stationService = stationService;
        }

        // ---------------------------
        // 🚗 Station Endpoints
        // ---------------------------

        // Create station - Admin or Backoffice only
        [HttpPost]
        //[Authorize(Roles = "Admin")]
        public async Task<IActionResult> CreateStation([FromBody] CreateStationDto dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Name) || string.IsNullOrWhiteSpace(dto.Location))
                return BadRequest(new { message = "Name and Location are required" });
            if (dto.Capacity <= 0) return BadRequest(new { message = "Capacity must be > 0" });

            try
            {
                var created = await _stationService.CreateStationAsync(dto);
                return CreatedAtAction(nameof(GetStationById), new { stationId = created.StationId }, created);
            }
            catch (ArgumentException ex) { return Conflict(new { message = ex.Message }); }
            catch (Exception ex) { Console.WriteLine(ex); return StatusCode(500, new { message = "Unexpected error" }); }
        }

        // Update station
        [HttpPut("{stationId}")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> UpdateStation(string stationId, [FromBody] UpdateStationDto dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Name) || string.IsNullOrWhiteSpace(dto.Location))
                return BadRequest(new { message = "Name and Location are required" });
            if (dto.Capacity <= 0) return BadRequest(new { message = "Capacity must be > 0" });

            try
            {
                var updated = await _stationService.UpdateStationAsync(stationId, dto);
                if (updated == null) return NotFound(new { message = "Station not found" });
                return Ok(updated);
            }
            catch (ArgumentException ex) { return Conflict(new { message = ex.Message }); }
            catch (Exception ex) { Console.WriteLine(ex); return StatusCode(500, new { message = "Unexpected error" }); }
        }

        [HttpPatch("{stationId}/deactivate")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> DeactivateStation(string stationId)
        {
            try
            {
                var success = await _stationService.DeactivateStationAsync(stationId);
                if (!success) return BadRequest(new { message = "Cannot deactivate station (may have active bookings)" });
                return Ok(new { message = "Station deactivated" });
            }
            catch (InvalidOperationException ex)
            {
                // Business rule: active bookings prevent deactivation
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        // Get station by id
        [HttpGet("{stationId}")]
        [Authorize(Roles = "Owner,Admin,Operator")]
        public async Task<IActionResult> GetStationById(string stationId)
        {
            try
            {
                var dto = await _stationService.GetStationByIdAsync(stationId);
                if (dto == null) return NotFound(new { message = "Station not found" });
                return Ok(dto);
            }
            catch (Exception ex) { Console.WriteLine(ex); return StatusCode(500, new { message = "Unexpected error" }); }
        }

        // Get all stations
        [HttpGet]
        //[Authorize]
        public async Task<IActionResult> GetAllStations([FromQuery] bool onlyActive = false)
        {
            try
            {
                var list = await _stationService.GetAllStationsAsync(onlyActive);
                return Ok(list);
            }
            catch (Exception ex) { Console.WriteLine(ex); return StatusCode(500, new { message = "Unexpected error" }); }
        }

        // Search stations
        [HttpGet("search")]
        [Authorize]
        public async Task<IActionResult> SearchStations([FromQuery] string? type = null, [FromQuery] string? location = null)
        {
            try
            {
                var results = await _stationService.SearchStationsAsync(type, location);
                return Ok(results);
            }
            catch (Exception ex) { Console.WriteLine(ex); return StatusCode(500, new { message = "Unexpected error" }); }
        }

        [HttpGet("names")]
        [Authorize]
        public async Task<IActionResult> GetStationNames([FromQuery] string? type = null, [FromQuery] string? location = null)
        {
            try
            {
                var results = await _stationService.GetStationNameSuggestionsAsync(type, location);
                return Ok(results);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error occurred." });
            }
        }


        [HttpGet("nearby")]
        [Authorize] // Optional: can restrict to logged-in users
        public async Task<IActionResult> GetNearbyStations([FromQuery] double latitude, [FromQuery] double longitude, [FromQuery] double radiusKm = 5)
        {
            try
            {
                var result = await _stationService.GetNearbyStationsAsync(latitude, longitude, radiusKm);
                return Ok(result);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        // GET: /api/station/nearby-by-type?type=AC&latitude=6.9271&longitude=79.8612&radiusKm=10
        [HttpGet("nearby-by-type")]
       [Authorize(Roles = "Admin,Operator,Owner")]
        public async Task<IActionResult> GetNearbyStationsByType(
            [FromQuery] string type,
            [FromQuery] double latitude,
            [FromQuery] double longitude,
            [FromQuery] double radiusKm = 5)
        {
            try
            {
                if (string.IsNullOrEmpty(type))
                    return BadRequest(new { message = "Type is required (AC/DC)" });

                // Step 1: Fetch nearby stations
                var nearby = await _stationService.GetNearbyStationsAsync(latitude, longitude, radiusKm);
                if (nearby == null || !nearby.Any())
                    return Ok(Enumerable.Empty<object>());

                // Step 2: Filter by type (case-insensitive)
                var filtered = nearby
                    .Where(s => string.Equals(s.Type, type, StringComparison.OrdinalIgnoreCase))
                    .ToList();

                return Ok(filtered);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error while fetching stations by type" });
            }
        }


        [HttpDelete("{stationId}")]
        [Authorize(Roles = "Admin,Backoffice")]
        public async Task<IActionResult> DeleteStation(string stationId)
        {
            try
            {
                var ok = await _stationService.DeleteStationWithRelationsAsync(stationId);
                if (!ok) return NotFound(new { message = "Station not found" });
                return Ok(new { message = "Station and related data deleted successfully" });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Error deleting station" });
            }
        }

    }
}
