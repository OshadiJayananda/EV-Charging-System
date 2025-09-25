// --------------------------------------------------------------
// File Name: UsersController.cs
// Author: Hasindu Koshitha
// Description: Handles user-related logic for the system
// Created On: 13/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;
using EvBackend.Services;
using EvBackend.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace EvBackend.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class UsersController : ControllerBase
    {
        private readonly IUserService _userService;
        public UsersController(IUserService userService)
        {
            _userService = userService;
        }

        [HttpPost]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> CreateUser([FromBody] CreateUserDto dto)
        {
            if (!ModelState.IsValid)
                return BadRequest(new { message = "Invalid input data" });

            try
            {
                var user = await _userService.CreateUser(dto);
                return CreatedAtAction(nameof(GetUserById), new { userId = user.Id }, user);
            }
            catch (ArgumentException ex) // e.g., duplicate email
            {
                return Conflict(new { message = ex.Message });
            }
            catch
            {
                return StatusCode(500, new { message = "Unexpected error occurred" });
            }
        }

        [HttpGet("{userId}")]
        [Authorize(Roles = "Admin,Operator")]
        public async Task<IActionResult> GetUserById(string userId)
        {
            try
            {
                var user = await _userService.GetUserById(userId);
                if (user == null)
                    return NotFound(new { message = "User not found" });

                return Ok(user);
            }
            catch
            {
                return StatusCode(500, new { message = "Unexpected error occurred" });
            }
        }

        [HttpGet]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> GetAllUsers([FromQuery] int page = 1, [FromQuery] int pageSize = 10)
        {
            try
            {
                var users = await _userService.GetAllUsers(page, pageSize);
                return Ok(users);
            }
            catch
            {
                return StatusCode(500, new { message = "Unexpected error occurred" });
            }
        }

        [HttpPut("{userId}")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> UpdateUser(string userId, [FromBody] UserDto dto)
        {
            if (!ModelState.IsValid)
                return BadRequest(new { message = "Invalid input data" });

            try
            {
                var updatedUser = await _userService.UpdateUser(userId, dto);
                if (updatedUser == null)
                    return NotFound(new { message = "User not found" });

                return Ok(updatedUser);
            }
            catch
            {
                return StatusCode(500, new { message = "Unexpected error occurred" });
            }
        }

        [HttpPatch("{userId}/deactivate")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> DeactivateUser(string userId)
        {
            try
            {
                var result = await _userService.ChangeUserStatus(userId, false);
                if (!result)
                    return NotFound(new { message = "User not found" });

                return NoContent();
            }
            catch
            {
                return StatusCode(500, new { message = "Unexpected error occurred" });
            }
        }

        [HttpPatch("{userId}/activate")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> ActivateUser(string userId)
        {
            try
            {
                var result = await _userService.ChangeUserStatus(userId, true);
                if (!result)
                    return NotFound(new { message = "User not found" });

                return NoContent();
            }
            catch
            {
                return StatusCode(500, new { message = "Unexpected error occurred" });
            }
        }
    }
}
