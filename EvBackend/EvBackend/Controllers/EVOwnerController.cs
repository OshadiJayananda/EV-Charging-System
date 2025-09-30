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
            // Inline: Validates model and delegates to EV Owner service to create owner account.
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

        [HttpPatch("status/{nic?}")]
        [Authorize(Roles = "Owner,Admin")]
        public async Task<IActionResult> ChangeEVOwnerStatus(string? nic, [FromQuery] bool isActivate)
        {
            // Inline: Handles role-based activation/deactivation rules for owners and admins.
            // Admins can only activate
            if (User.IsInRole("Admin"))
            {
                if (!isActivate)
                {
                    return StatusCode(403, new { message = "Admin can only activate accounts." });
                }

                if (string.IsNullOrWhiteSpace(nic))
                {
                    return BadRequest(new { message = "NIC is required for admin." });
                }
            }
            // Owners can only deactivate their own account
            else if (User.IsInRole("Owner"))
            {
                if (isActivate)
                {
                    return StatusCode(403, new { message = "Owners cannot activate accounts." });
                }

                var userNic = User.FindFirstValue(ClaimTypes.NameIdentifier);
                nic = userNic;

                if (string.IsNullOrWhiteSpace(nic))
                {
                    return BadRequest(new { message = "Unable to identify your account." });
                }
            }
            else
            {
                return Forbid();
            }

            try
            {
                var result = await _evOwnerService.ChangeEVOwnerStatus(nic, isActivate);

                if (!result)
                {
                    return NotFound(new { message = "EV Owner not found." });
                }

                if (!result)
                {
                    return BadRequest(new
                    {
                        message = isActivate
                        ? "EV Owner is already activated."
                        : "EV Owner is already deactivated."
                    });
                }
                if (!result)
                {
                    return NotFound(new { message = "EV Owner not found." });
                }

                return Ok(new { message = "EV Owner status updated successfully." });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
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
            var isOwner = User.IsInRole("OWNER");
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
            var ok = await _evOwnerService.ChangeEVOwnerStatus(nic, true);
            if (!ok) return NotFound(new { message = "EV Owner not found." });
            return Ok(new { message = "Account activated." });
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


    }
}
