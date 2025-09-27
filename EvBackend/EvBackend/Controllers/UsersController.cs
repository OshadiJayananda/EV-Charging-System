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
        private readonly ILogger<UsersController> _logger;

        public UsersController(IUserService userService, IEVOwnerService evOwnerService, ILogger<UsersController> logger)
        {
            _userService = userService;
            _evOwnerService = evOwnerService;
            _logger = logger;
        }

        [HttpPost]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> CreateUser([FromBody] CreateUserDto dto)
        {
            if (!ModelState.IsValid)
            {
                _logger.LogWarning("CreateUser: Invalid input data received.");
                return BadRequest(new { message = "Invalid input data" });
            }

            try
            {
                var user = await _userService.CreateUser(dto);
                _logger.LogInformation("CreateUser: User created successfully. UserId={UserId}", user.Id);
                return CreatedAtAction(nameof(GetUserById), new { userId = user.Id }, user);
            }
            catch (ArgumentException ex)
            {
                _logger.LogWarning("CreateUser: Conflict - {Message}", ex.Message);
                return Conflict(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "CreateUser: Unexpected error occurred.");
                return StatusCode(500, new { message = "Unexpected error occurred" });
            }
        }

        [HttpGet("{userId}")]
        [Authorize(Roles = "Admin,Operator")]
                // --------------------------------------------------------------
        public async Task<IActionResult> GetUserById(string userId)
        {
            try
            {
                if (!MongoDB.Bson.ObjectId.TryParse(userId, out var objectId))
                {
                    _logger.LogWarning("GetUserById: Invalid user ID format. UserId={UserId}", userId);
                    return BadRequest(new { message = "Invalid user ID" });
                }

                var user = await _userService.GetUserById(userId);
                if (user == null)
                {
                    _logger.LogWarning("GetUserById: User not found. UserId={UserId}", userId);
                    return NotFound(new { message = "User not found" });
                }

                _logger.LogInformation("GetUserById: User fetched successfully. UserId={UserId}", userId);
                return Ok(user);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "GetUserById: Unexpected error occurred.");
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
                {
                    _logger.LogWarning("GetAllUsers: Invalid role filter. Role={Role}", role);
                    return BadRequest(new { message = "Invalid role filter" });
                }
                if (role != null && role == "Owner")
                {
                    var owners = await _evOwnerService.GetAllEVOwners(page, pageSize);
                    _logger.LogInformation("GetAllUsers: Fetched {Count} owners.", owners.Count());
                    return Ok(owners);
                }
                var users = await _userService.GetAllUsers(page, pageSize, role);

                if (role == null)
                {
                    var owners = await _evOwnerService.GetAllEVOwners(page, pageSize);
                    _logger.LogInformation("GetAllUsers: Fetched {UserCount} users and {OwnerCount} owners.", users.Count(), owners.Count());
                    return Ok(new { Users = users, Owners = owners });
                }
                else
                {
                    _logger.LogInformation("GetAllUsers: Fetched {Count} users with role {Role}.", users.Count(), role);
                    return Ok(users);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "GetAllUsers: Unexpected error occurred while fetching users.");
                return StatusCode(500, new { message = "Unexpected error occurred" });
            }
        }

        [HttpPut("{userId}")]
        [Authorize(Roles = "Operator")]
        public async Task<IActionResult> UpdateUser(string userId, [FromBody] UpdateUserDto dto)
        {
            if (!MongoDB.Bson.ObjectId.TryParse(userId, out var objectId))
            {
                _logger.LogWarning("UpdateUser: Invalid user ID format. UserId={UserId}", userId);
                return BadRequest(new { message = "Invalid user ID" });
            }

            if (!ModelState.IsValid)
            {
                _logger.LogWarning("UpdateUser: Invalid input data received.");
                return BadRequest(new { message = "Invalid input data" });
            }

            var userRole = User.Claims.FirstOrDefault(c => c.Type == System.Security.Claims.ClaimTypes.Role)?.Value;
            var userEmail = User.Claims.FirstOrDefault(c => c.Type == System.Security.Claims.ClaimTypes.Email)?.Value;
            if (userRole == "Operator")
            {
                var user = await _userService.GetUserById(userId);
                if (user == null || user.Email != userEmail)
                {
                    _logger.LogWarning("UpdateUser: Operator tried to update another user's profile. UserId={UserId}, OperatorEmail={OperatorEmail}", userId, userEmail);
                    return Forbid();
                }
            }

            try
            {
                var updatedUser = await _userService.UpdateUser(userId, dto);
                if (updatedUser == null)
                {
                    _logger.LogWarning("UpdateUser: User not found. UserId={UserId}", userId);
                    return NotFound(new { message = "User not found" });
                }

                _logger.LogInformation("UpdateUser: User updated successfully. UserId={UserId}", userId);
                return Ok(updatedUser);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "UpdateUser: Unexpected error occurred.");
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
                {
                    _logger.LogWarning("DeactivateUser: Invalid user ID format. UserId={UserId}", userId);
                    return BadRequest(new { message = "Invalid user ID" });
                }

                var userEmail = User.Claims.FirstOrDefault(c => c.Type == System.Security.Claims.ClaimTypes.Email)?.Value;

                var user = await _userService.GetUserById(userId);
                _logger.LogInformation("DeactivateUser: Fetched user. Email={Email}, IsActive={IsActive}", user?.Email, user?.IsActive);
                if (user == null || user.Email != userEmail)
                {
                    _logger.LogWarning("DeactivateUser: Operator tried to deactivate another user. UserId={UserId}, OperatorEmail={OperatorEmail}", userId, userEmail);
                    return Forbid();
                }

                if (!user.IsActive)
                {
                    _logger.LogWarning("DeactivateUser: User already deactivated. UserId={UserId}", userId);
                    return BadRequest(new { message = "User is already deactivated" });
                }

                var result = await _userService.ChangeUserStatus(userId, false);
                if (!result)
                {
                    _logger.LogWarning("DeactivateUser: User not found during status change. UserId={UserId}", userId);
                    return NotFound(new { message = "User not found" });
                }

                _logger.LogInformation("DeactivateUser: User deactivated successfully. UserId={UserId}", userId);
                return NoContent();
            }
            catch (KeyNotFoundException ex)
            {
                _logger.LogWarning("DeactivateUser: KeyNotFoundException - {Message}", ex.Message);
                return NotFound(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "DeactivateUser: Unexpected error occurred.");
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
                {
                    _logger.LogWarning("ActivateUser: Invalid user ID format. UserId={UserId}", userId);
                    return BadRequest(new { message = "Invalid user ID" });
                }

                var user = await _userService.GetUserById(userId);
                if (user == null)
                {
                    _logger.LogWarning("ActivateUser: User not found. UserId={UserId}", userId);
                    return NotFound(new { message = "User not found" });
                }

                if (user.IsActive)
                {
                    _logger.LogWarning("ActivateUser: User already active. UserId={UserId}", userId);
                    return BadRequest(new { message = "User is already active" });
                }

                await _userService.ChangeUserStatus(userId, true);
                _logger.LogInformation("ActivateUser: User activated successfully. UserId={UserId}", userId);
                return NoContent();
            }
            catch (KeyNotFoundException ex)
            {
                _logger.LogWarning("ActivateUser: KeyNotFoundException - {Message}", ex.Message);
                return NotFound(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "ActivateUser: Unexpected error occurred.");
                return StatusCode(500, new { message = "Unexpected error occurred" });
            }
        }
    }
}
