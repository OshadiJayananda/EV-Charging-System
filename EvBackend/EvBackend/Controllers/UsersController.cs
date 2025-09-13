// --------------------------------------------------------------
// File Name: UserController.cs
// Author: Hasindu Koshitha
// Description: Handles user-related logic for the system
// Created On: 13/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;
using EvBackend.Services;
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
            var user = await _userService.CreateUser(dto);
            return CreatedAtAction(nameof(GetUserById), new { userId = user.Id }, user);
        }

        [HttpGet("{userId}")]
        [Authorize(Roles = "Admin,Operator")]
        public async Task<IActionResult> GetUserById(String userId)
        {
            var user = await _userService.GetUserById(userId);
            return Ok(user);
        }

        [HttpGet]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> GetAllUsers([FromQuery] int page = 1, [FromQuery] int pageSize = 10)
        {
            var users = await _userService.GetAllUsers(page, pageSize);
            return Ok(users);
        }

        [HttpPut("{userId}")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> UpdateUser(String userId, [FromBody] UserDto dto)
        {
            var user = await _userService.UpdateUser(userId, dto);
            return Ok(user);
        }

        [HttpPatch("{userId}/deactivate")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> DeactivateUser(String userId)
        {
            await _userService.ChangeUserStatus(userId, false);
            return NoContent();
        }

        [HttpPatch("{userId}/activate")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> ActivateUser(String userId)
        {
            await _userService.ChangeUserStatus(userId, true);
            return NoContent();
        }
    }
}
