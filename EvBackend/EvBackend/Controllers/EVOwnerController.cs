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
    [Route("api/owner")]
    public class EVOwnerController : ControllerBase
    {
        private readonly IEVOwnerService _evOwnerService;

        public EVOwnerController(IEVOwnerService evOwnerService)
        {
            _evOwnerService = evOwnerService;
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

        [HttpPatch("status/{nic?}")]
        [Authorize(Roles = "Owner,Admin")]
        public async Task<IActionResult> ChangeEVOwnerStatus(string? nic, [FromQuery] bool isActivate)
        {
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

                if (result == null)
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
    }
}
