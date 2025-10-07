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
        [Authorize(Roles = "Admin,Operator")]
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



        // ---------------------------
        // 🔌 Slot Endpoints
        // ---------------------------

        // Create a new slot for a station
        [HttpPost("{stationId}/slots")]
        [Authorize(Roles = "Admin,Backoffice,Operator")]
        public async Task<IActionResult> CreateSlot(string stationId, [FromBody] SlotDto dto)
        {
            if (dto == null) return BadRequest(new { message = "Slot data required" });
            if (string.IsNullOrWhiteSpace(dto.ConnectorType)) return BadRequest(new { message = "ConnectorType is required" });

            try
            {
                var db = HttpContext.RequestServices.GetService(typeof(IMongoDatabase)) as IMongoDatabase;
                var slots = db.GetCollection<EvBackend.Entities.Slot>("Slots");

                var newSlot = new EvBackend.Entities.Slot
                {
                    SlotId = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
                    StationId = stationId,
                    ConnectorType = dto.ConnectorType,
                    Status = string.IsNullOrWhiteSpace(dto.Status) ? "Available" : dto.Status
                };

                await slots.InsertOneAsync(newSlot);

                var created = new SlotDto { SlotId = newSlot.SlotId, StationId = newSlot.StationId, ConnectorType = newSlot.ConnectorType, Status = newSlot.Status };
                return CreatedAtAction(nameof(GetSlotsByStation), new { stationId = stationId }, created);
            }
            catch (MongoWriteException mwx) { Console.WriteLine(mwx); return Conflict(new { message = "Could not create slot" }); }
            catch (Exception ex) { Console.WriteLine(ex); return StatusCode(500, new { message = "Unexpected error" }); }
        }

        // Get all slots for a station
        [HttpGet("{stationId}/slots")]
        [Authorize]
        public async Task<IActionResult> GetSlotsByStation(string stationId)
        {
            try
            {
                var db = HttpContext.RequestServices.GetService(typeof(IMongoDatabase)) as IMongoDatabase;
                var slots = db.GetCollection<EvBackend.Entities.Slot>("Slots");
                var list = await slots.Find(s => s.StationId == stationId).ToListAsync();
                var dtos = list.Select(s => new SlotDto { SlotId = s.SlotId, StationId = s.StationId, ConnectorType = s.ConnectorType, Status = s.Status });
                return Ok(dtos);
            }
            catch (Exception ex) { Console.WriteLine(ex); return StatusCode(500, new { message = "Unexpected error" }); }
        }

        // Get a single slot by id
        [HttpGet("{stationId}/slots/{slotId}")]
        [Authorize]
        public async Task<IActionResult> GetSlotById(string stationId, string slotId)
        {
            try
            {
                var db = HttpContext.RequestServices.GetService(typeof(IMongoDatabase)) as IMongoDatabase;
                var slots = db.GetCollection<EvBackend.Entities.Slot>("Slots");

                var slot = await slots.Find(s => s.SlotId == slotId && s.StationId == stationId).FirstOrDefaultAsync();
                if (slot == null) return NotFound(new { message = "Slot not found" });

                return Ok(new SlotDto
                {
                    SlotId = slot.SlotId,
                    StationId = slot.StationId,
                    ConnectorType = slot.ConnectorType,
                    Status = slot.Status
                });
            }
            catch (Exception ex) { Console.WriteLine(ex); return StatusCode(500, new { message = "Unexpected error" }); }
        }

        // Update slot availability
        [HttpPatch("{stationId}/slots/{slotId}")]
        [Authorize(Roles = "Admin,Backoffice,Operator")]
        public async Task<IActionResult> UpdateSlotAvailability(string stationId, string slotId, [FromBody] UpdateSlotDto dto)
        {
            if (dto == null || string.IsNullOrWhiteSpace(dto.Status)) return BadRequest(new { message = "Status is required" });

            var allowed = new[] { "Available", "Booked", "Inactive" };
            if (!allowed.Contains(dto.Status)) return BadRequest(new { message = "Invalid status" });

            try
            {
                var db = HttpContext.RequestServices.GetService(typeof(IMongoDatabase)) as IMongoDatabase;
                var slots = db.GetCollection<EvBackend.Entities.Slot>("Slots");
                var filter = Builders<EvBackend.Entities.Slot>.Filter.Eq(s => s.SlotId, slotId) &
                             Builders<EvBackend.Entities.Slot>.Filter.Eq(s => s.StationId, stationId);
                var update = Builders<EvBackend.Entities.Slot>.Update.Set(s => s.Status, dto.Status);
                var options = new FindOneAndUpdateOptions<EvBackend.Entities.Slot> { ReturnDocument = ReturnDocument.After };
                var updated = await slots.FindOneAndUpdateAsync(filter, update, options);

                if (updated == null) return NotFound(new { message = "Slot not found" });
                return Ok(new { slotId = updated.SlotId, status = updated.Status });
            }
            catch (Exception ex) { Console.WriteLine(ex); return StatusCode(500, new { message = "Unexpected error" }); }
        }

        // Delete a slot
        [HttpDelete("{stationId}/slots/{slotId}")]
        [Authorize(Roles = "Admin,Backoffice,Operator")]
        public async Task<IActionResult> DeleteSlot(string stationId, string slotId)
        {
            try
            {
                var db = HttpContext.RequestServices.GetService(typeof(IMongoDatabase)) as IMongoDatabase;
                var slots = db.GetCollection<EvBackend.Entities.Slot>("Slots");

                var result = await slots.DeleteOneAsync(s => s.SlotId == slotId && s.StationId == stationId);

                if (result.DeletedCount == 0)
                    return NotFound(new { message = "Slot not found" });

                return Ok(new { message = "Slot deleted successfully" });
            }
            catch (Exception ex) { Console.WriteLine(ex); return StatusCode(500, new { message = "Unexpected error" }); }
        }
    }

    //need to assign operator to station

}
