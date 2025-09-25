// --------------------------------------------------------------
// File Name: EVOwnerController.cs
// Author: Hasindu Koshitha
// Description: Handles EV Owner registration logic for the system
// Created On: 25/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;
using EvBackend.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;
using System.Security.Authentication;

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
                // If response contains more info, return it. Otherwise, just token.
                return Ok(response); // Return the EVOwnerDto directly
            }
            catch (AuthenticationException ex)
            {
                return Unauthorized(new { message = ex.Message });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "An unexpected error occurred." });
            }
        }
    }
}
