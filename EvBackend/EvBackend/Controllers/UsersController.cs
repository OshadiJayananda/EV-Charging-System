// --------------------------------------------------------------
// File Name: UsersController.cs
// Author: Hasindu Koshitha
// Description: Handles user-related logic for the system
// Created On: 13/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;
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
        private readonly IEVOwnerService _evOwnerService;

        public UsersController(IUserService userService, IEVOwnerService evOwnerService)
        {
            _userService = userService;
            _evOwnerService = evOwnerService;
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
            catch (ArgumentException ex)
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
                if (!MongoDB.Bson.ObjectId.TryParse(userId, out var objectId))
                    return BadRequest(new { message = "Invalid user ID" });

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
        [Authorize(Roles = "Admin, Operator")]
        public async Task<IActionResult> GetAllUsers([FromQuery] int page = 1, [FromQuery] int pageSize = 10, [FromQuery] string? role = null)
        {
            try
            {
                if (role != null && role != "Admin" && role != "Operator" && role != "Owner")
                    return BadRequest(new { message = "Invalid role filter" });
                if (role != null && role == "Owner")
                {
                    var owners = await _evOwnerService.GetAllEVOwners(page, pageSize);
                    return Ok(owners);
                }
                var users = await _userService.GetAllUsers(page, pageSize, role);

                if (role == null)
                {
                    var owners = await _evOwnerService.GetAllEVOwners(page, pageSize);
                    return Ok(new { Users = users, Owners = owners });
                }
                else
                {
                    return Ok(users);
                }
            }
            catch
            {
                return StatusCode(500, new { message = "Unexpected error occurred" });
            }
        }

        [HttpPut("{userId}")]
        [Authorize(Roles = "Operator")]
        public async Task<IActionResult> UpdateUser(string userId, [FromBody] UpdateUserDto dto)
        {
            if (!MongoDB.Bson.ObjectId.TryParse(userId, out var objectId))
                return BadRequest(new { message = "Invalid user ID" });

            if (!ModelState.IsValid)
                return BadRequest(new { message = "Invalid input data" });

            //if role is operator, they can only update their own profile
            var userRole = User.Claims.FirstOrDefault(c => c.Type == System.Security.Claims.ClaimTypes.Role)?.Value;
            var userEmail = User.Claims.FirstOrDefault(c => c.Type == System.Security.Claims.ClaimTypes.Email)?.Value;
            if (userRole == "Operator")
            {
                var user = await _userService.GetUserById(userId);
                if (user == null || user.Email != userEmail)
                    return Forbid();
            }

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
        [Authorize(Roles = "Operator")]
        public async Task<IActionResult> DeactivateUser(string userId)
        {
            try
            {
                if (!MongoDB.Bson.ObjectId.TryParse(userId, out var objectId))
                    return BadRequest(new { message = "Invalid user ID" });

                var userEmail = User.Claims.FirstOrDefault(c => c.Type == System.Security.Claims.ClaimTypes.Email)?.Value;

                var user = await _userService.GetUserById(userId);
                if (user == null || user.Email != userEmail)
                    return Forbid();

                if (!user.IsActive)
                    return BadRequest(new { message = "User is already deactivated" });

                var result = await _userService.ChangeUserStatus(userId, false);
                if (!result)
                    return NotFound(new { message = "User not found" });

                return NoContent();
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(new { message = ex.Message });
            }
            catch (Exception)
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
                if (!MongoDB.Bson.ObjectId.TryParse(userId, out var objectId))
                    return BadRequest(new { message = "Invalid user ID" });

                var user = await _userService.GetUserById(userId);
                if (user == null)
                    return NotFound(new { message = "User not found" });

                if (user.IsActive)
                    return BadRequest(new { message = "User is already active" });

                await _userService.ChangeUserStatus(userId, true);
                return NoContent();
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(new { message = ex.Message });
            }
            catch
            {
                return StatusCode(500, new { message = "Unexpected error occurred" });
            }
        }
    }
}
