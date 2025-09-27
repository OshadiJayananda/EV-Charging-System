using EvBackend.Models.DTOs;
using EvBackend.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

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

        [HttpPost]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> CreateStation([FromBody] CreateStationDto dto)
        {
            //var ownerId = User.Identity?.Name ?? "system"; // Example
            var result = await _stationService.CreateStationAsync(dto);
            return Ok(result);
        }

        [HttpPut("{stationId}")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> UpdateStation(string stationId, [FromBody] UpdateStationDto dto)
        {
            var result = await _stationService.UpdateStationAsync(stationId, dto);
            if (result == null) return NotFound(new { message = "Station not found" });
            return Ok(result);
        }

        [HttpPatch("{stationId}/deactivate")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> DeactivateStation(string stationId)
        {
            var success = await _stationService.DeactivateStationAsync(stationId);
            if (!success) return BadRequest(new { message = "Cannot deactivate station (may have active bookings)" });
            return Ok(new { message = "Station deactivated" });
        }

        [HttpGet("{stationId}")]
        [Authorize(Roles = "Admin,Operator")]
        public async Task<IActionResult> GetStationById(string stationId)
        {
            var result = await _stationService.GetStationByIdAsync(stationId);
            if (result == null) return NotFound(new { message = "Station not found" });
            return Ok(result);
        }

        [HttpGet]
        [Authorize(Roles = "Admin,Operator")]
        public async Task<IActionResult> GetAllStations([FromQuery] bool onlyActive = false)
        {
            var result = await _stationService.GetAllStationsAsync(onlyActive);
            return Ok(result);
        }

        [HttpGet("search")]
        [Authorize(Roles = "Admin,Operator,Owner")]
        public async Task<IActionResult> SearchStations([FromQuery] string type, [FromQuery] string location)
        {
            var result = await _stationService.SearchStationsAsync(type, location);
            return Ok(result);
        }
    }
}
