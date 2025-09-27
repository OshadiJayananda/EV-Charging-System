// --------------------------------------------------------------
// File Name: SlotController.cs
// Author: Miyuri Lokuhewage
// Description: Provides endpoints for slot management under stations.
// Roles: Admin, Backoffice, Operator, Owner (read-only).
// Includes consistent error handling (400/404/409).
// Created/Updated On: 27/09/2025
// --------------------------------------------------------------

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;
using EvBackend.Entities;
using EvBackend.Models.DTOs;

namespace EvBackend.Controllers
{
    [ApiController]
    [Route("api/stations/{stationId}/slots")]
    public class SlotController : ControllerBase
    {
        private readonly IMongoDatabase _db;

        public SlotController(IMongoDatabase db)
        {
            _db = db;
        }

        // ---------------------------
        // 🔌 Slot Endpoints
        // ---------------------------

        // Create Slot (Admin/Backoffice/Operator)
        [HttpPost]
        [Authorize(Roles = "Admin,Backoffice,Operator")]
        public async Task<IActionResult> CreateSlot(string stationId, [FromBody] CreateSlotDto dto)
        {
            if (dto == null || string.IsNullOrWhiteSpace(dto.ConnectorType))
                return BadRequest(new { message = "ConnectorType is required" });

            try
            {
                var slots = _db.GetCollection<Slot>("Slots");

                var newSlot = new Slot
                {
                    SlotId = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
                    StationId = stationId,
                    ConnectorType = dto.ConnectorType,
                    Status = "Available",
                    StartTime = null,
                    EndTime = null
                };

                await slots.InsertOneAsync(newSlot);

                var result = new SlotDto
                {
                    SlotId = newSlot.SlotId,
                    StationId = newSlot.StationId,
                    ConnectorType = newSlot.ConnectorType,
                    Status = newSlot.Status,
                    CreatedAt = DateTime.UtcNow,
                    UpdatedAt = DateTime.UtcNow
                };

                return CreatedAtAction(nameof(GetSlotsByStation), new { stationId }, result);
            }
            catch (MongoWriteException ex)
            {
                Console.WriteLine(ex);
                return Conflict(new { message = "Could not create slot (duplicate or DB error)" });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        // Get all slots for a station
        [HttpGet]
        [Authorize(Roles = "Admin,Backoffice,Operator,Owner")]
        public async Task<IActionResult> GetSlotsByStation(string stationId)
        {
            try
            {
                var slots = _db.GetCollection<Slot>("Slots");
                var list = await slots.Find(s => s.StationId == stationId).ToListAsync();

                var result = list.Select(s => new SlotDto
                {
                    SlotId = s.SlotId,
                    StationId = s.StationId,
                    ConnectorType = s.ConnectorType,
                    Status = s.Status,
                    CreatedAt = DateTime.UtcNow,
                    UpdatedAt = DateTime.UtcNow
                });

                return Ok(result);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        // Get single slot by ID
        [HttpGet("{slotId}")]
        [Authorize(Roles = "Admin,Backoffice,Operator,Owner")]
        public async Task<IActionResult> GetSlotById(string stationId, string slotId)
        {
            try
            {
                var slots = _db.GetCollection<Slot>("Slots");
                var slot = await slots.Find(s => s.SlotId == slotId && s.StationId == stationId).FirstOrDefaultAsync();

                if (slot == null)
                    return NotFound(new { message = "Slot not found" });

                return Ok(new SlotDto
                {
                    SlotId = slot.SlotId,
                    StationId = slot.StationId,
                    ConnectorType = slot.ConnectorType,
                    Status = slot.Status,
                    CreatedAt = DateTime.UtcNow,
                    UpdatedAt = DateTime.UtcNow
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        // Update slot availability
        [HttpPatch("{slotId}")]
        [Authorize(Roles = "Admin,Backoffice,Operator")]
        public async Task<IActionResult> UpdateSlotAvailability(string stationId, string slotId, [FromBody] UpdateSlotDto dto)
        {
            if (dto == null || string.IsNullOrWhiteSpace(dto.Status))
                return BadRequest(new { message = "Status is required" });

            var allowed = new[] { "Available", "Booked", "Inactive" };
            if (!allowed.Contains(dto.Status))
                return BadRequest(new { message = "Invalid status" });

            try
            {
                var slots = _db.GetCollection<Slot>("Slots");
                var filter = Builders<Slot>.Filter.Eq(s => s.SlotId, slotId) &
                             Builders<Slot>.Filter.Eq(s => s.StationId, stationId);

                var update = Builders<Slot>.Update
                    .Set(s => s.Status, dto.Status)
                    .Set(s => s.StartTime, null)
                    .Set(s => s.EndTime, null);

                var options = new FindOneAndUpdateOptions<Slot> { ReturnDocument = ReturnDocument.After };
                var updated = await slots.FindOneAndUpdateAsync(filter, update, options);

                if (updated == null)
                    return NotFound(new { message = "Slot not found" });

                return Ok(new { slotId = updated.SlotId, status = updated.Status });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        // Delete slot
        [HttpDelete("{slotId}")]
        [Authorize(Roles = "Admin,Backoffice,Operator")]
        public async Task<IActionResult> DeleteSlot(string stationId, string slotId)
        {
            try
            {
                var slots = _db.GetCollection<Slot>("Slots");
                var result = await slots.DeleteOneAsync(s => s.SlotId == slotId && s.StationId == stationId);

                if (result.DeletedCount == 0)
                    return NotFound(new { message = "Slot not found" });

                return Ok(new { message = "Slot deleted successfully" });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }
    }
}
