// --------------------------------------------------------------
// File Name: EVOwnerController.cs
// Author: Hasindu Koshitha
// Description: Handles authentication logic for the system
// Created On: 13/09/2025
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
            try
            {
                var response = await _evOwnerService.CreateEVOwner(createEVOwnerDto);
                return Ok(new
                {
                    token = response.Token
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "An unexpected error occurred." });
            }
        }
    }
}
