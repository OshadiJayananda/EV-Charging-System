// --------------------------------------------------------------
// File Name: EVOwnerController.cs
// Author: Hasindu Koshitha
// Description: Handles EV Owner registration logic for the system
// Created On: 25/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;
using EvBackend.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Authentication;
using System.Security.Claims;

namespace EvBackend.Controllers
{
    [ApiController]
    [Route("api/owners")]
    public class EVOwnerController : ControllerBase
    {
        private readonly IEVOwnerService _evOwnerService;
        private readonly INotificationService _notificationService;

        public EVOwnerController(IEVOwnerService evOwnerService, INotificationService notificationService)
        {
            _evOwnerService = evOwnerService;
            _notificationService = notificationService;
        }

        [HttpPost("register")]
        public async Task<IActionResult> RegisterOwner([FromBody] CreateEVOwnerDto createEVOwnerDto)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(new { message = "Invalid owner registration data." });
            }
            try
            {
                var response = await _evOwnerService.CreateEVOwner(createEVOwnerDto);
                return Ok(response);
            }
            catch (AuthenticationException ex)
            {
                return Unauthorized(new { message = ex.Message });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return StatusCode(500, new { message = "An unexpected error occurred." });
            }
        }

        [HttpPut("{nic}")]
        [Authorize(Roles = "Owner")]
        public async Task<IActionResult> UpdateOwner(string nic, [FromBody] UpdateEVOwnerDto dto)
        {
            if (!ModelState.IsValid) return BadRequest(new { message = "Invalid data." });

            var userNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!string.Equals(userNic, nic, StringComparison.OrdinalIgnoreCase))
                return Forbid();

            try
            {
                var updated = await _evOwnerService.UpdateEVOwner(nic, dto);
                return Ok(updated);
            }
            catch (KeyNotFoundException)
            {
                return NotFound(new { message = "EV Owner not found." });
            }
        }

        [HttpGet("{nic}")]
        [Authorize(Roles = "Owner,Admin")]
        public async Task<IActionResult> GetOwner(string nic)
        {
            var isOwner = User.IsInRole("Owner");
            if (isOwner)
            {
                var userNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
                if (!string.Equals(userNic, nic, StringComparison.OrdinalIgnoreCase))
                    return Forbid();
            }

            try
            {
                var dto = await _evOwnerService.GetEVOwnerByNIC(nic);
                return Ok(dto);
            }
            catch (KeyNotFoundException)
            {
                return NotFound(new { message = "EV Owner not found." });
            }
        }

        [HttpPatch("{nic}/deactivate")]
        [Authorize(Roles = "Owner")]
        public async Task<IActionResult> DeactivateSelf(string nic)
        {
            var userNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!string.Equals(userNic, nic, StringComparison.OrdinalIgnoreCase))
                return Forbid();

            var ok = await _evOwnerService.ChangeEVOwnerStatus(nic, false);
            if (!ok) return NotFound(new { message = "EV Owner not found." });
            return Ok(new { message = "Account deactivated." });
        }

        [HttpPatch("{nic}/activate")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> ActivateByBackoffice(string nic)
        {
            try
            {
                var owner = await _evOwnerService.GetEVOwnerByNIC(nic);
                if (owner == null)
                    return NotFound(new { message = "EV Owner not found." });

                if (owner.IsActive)
                    return BadRequest(new { message = "Account is already active." });

                var ok = await _evOwnerService.ChangeEVOwnerStatus(nic, true);
                if (!ok)
                    return BadRequest(new { message = "Failed to activate account." });

                return Ok(new { message = $"EV Owner {owner.FullName} activated successfully." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Unexpected server error", error = ex.Message });
            }
        }

        [HttpPatch("{nic}/request-reactivation")]
        [Authorize(Roles = "Owner")]
        public async Task<IActionResult> RequestReactivation(string nic)
        {
            var userNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!string.Equals(userNic, nic, StringComparison.OrdinalIgnoreCase))
                return Forbid();

            try
            {
                var ok = await _evOwnerService.RequestReactivation(nic);
                if (!ok) return BadRequest(new { message = "Could not request reactivation." });
                return Ok(new { message = "Reactivation request submitted successfully." });
            }
            catch (KeyNotFoundException)
            {
                return NotFound(new { message = "EV Owner not found." });
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpGet("reactivation-count")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> GetReactivationRequestCount()
        {
            try
            {
                var count = await _evOwnerService.GetReactivationRequestCount();
                return Ok(new { count });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to fetch reactivation count", error = ex.Message });
            }
        }

        [HttpGet("reactivation-requests")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> GetReactivationRequests()
        {
            try
            {
                var requests = await _evOwnerService.GetEVOwnersWithReactivationRequests();
                return Ok(requests);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to fetch reactivation requests", error = ex.Message });
            }
        }


        // [HttpGet("reactivation-requests")]
        // [Authorize(Roles = "Admin")]
        // public async Task<IActionResult> GetReactivationRequests()
        // {
        //     try
        //     {
        //         // Get EV Owners with reactivation requests
        //         var ownersWithRequests = await _evOwnerService.GetEVOwnersWithReactivationRequests();

        //         _logger.LogInformation("GetReactivationRequests: Fetched {Count} reactivation requests.", ownersWithRequests.Count());
        //         return Ok(ownersWithRequests);
        //     }
        //     catch (Exception ex)
        //     {
        //         _logger.LogError(ex, "GetReactivationRequests: Unexpected error occurred.");
        //         return StatusCode(500, new { message = "Unexpected error occurred" });
        //     }
        // }

        [HttpPatch("{nic}/clear-reactivation")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> ClearReactivationRequest(string nic)
        {
            try
            {
                var ok = await _evOwnerService.ClearReactivationRequest(nic);
                if (!ok) return NotFound(new { message = "EV Owner not found or no reactivation request to clear." });
                return Ok(new { message = "Reactivation request cleared successfully." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to clear reactivation request", error = ex.Message });
            }
        }
    }
}