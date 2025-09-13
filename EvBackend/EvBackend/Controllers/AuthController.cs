// --------------------------------------------------------------
// File Name: AuthController.cs
// Author: Hasindu Koshitha
// Description: Handles authentication logic for the system
// Created On: 13/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;
using EvBackend.Services;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using System.Security.Authentication;

namespace EvBackend.Controllers
{
    [ApiController]
    [Route("api/auth")]
    public class AuthController : ControllerBase
    {
        private readonly IUserService _userService;

        public AuthController(IUserService userService)
        {
            _userService = userService;
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginDto loginDto)
        {
            try
            {
                var token = await _userService.AuthenticateUser(loginDto);
                return Ok(token);
            }
            catch (AuthenticationException ex)
            {
                return Unauthorized(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                //return StatusCode(500, new { message = "An unexpected error occurred." });
                return StatusCode(500, new { message = ex.Message });
            }
        }
    }
}
