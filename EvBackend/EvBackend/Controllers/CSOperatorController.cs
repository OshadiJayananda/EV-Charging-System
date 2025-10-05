// --------------------------------------------------------------
// File Name: CSOperatorController.cs
// Author: Oshadi Jayananda
// Description: Handles Charging Station Operator management logic for the system
// Created On: 04/10/2025
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
    [Route("api/operators")]
    public class CSOperatorController : ControllerBase
    {
        private readonly ICSOperatorService _csOperatorService;
        private readonly INotificationService _notificationService;

        public CSOperatorController(ICSOperatorService csOperatorService, INotificationService notificationService)
        {
            _csOperatorService = csOperatorService;
            _notificationService = notificationService;
        }

        [HttpPost]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> CreateOperator([FromBody] CreateCSOperatorDto createCSOperatorDto)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(new { message = "Invalid operator registration data." });
            }

            try
            {
                var response = await _csOperatorService.CreateOperator(createCSOperatorDto);
                return Ok(response);
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
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

        [HttpGet("{id}")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> GetOperatorById(string id)
        {
            try
            {
                var response = await _csOperatorService.GetOperatorById(id);
                if (response == null)
                {
                    return NotFound(new { message = "Operator not found." });
                }
                return Ok(response);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return StatusCode(500, new { message = "An unexpected error occurred." });
            }
        }

        [HttpGet]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> GetAllOperators([FromQuery] int page = 1, [FromQuery] int pageSize = 10)
        {
            try
            {
                var response = await _csOperatorService.GetAllOperators(page, pageSize);
                return Ok(response);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return StatusCode(500, new { message = "An unexpected error occurred." });
            }
        }

        [HttpPut("{id}")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> UpdateOperator(string id, [FromBody] UpdateCSOperatorDto csOperatorDto)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(new { message = "Invalid operator data." });
            }

            try
            {
                var response = await _csOperatorService.UpdateOperator(id, csOperatorDto);
                if (response == null)
                {
                    return NotFound(new { message = "Operator not found." });
                }
                return Ok(response);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return StatusCode(500, new { message = "An unexpected error occurred." });
            }
        }

        [HttpPatch("{id}/status")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> ChangeOperatorStatus(string id, [FromQuery] bool isActive)
        {
            try
            {
                var success = await _csOperatorService.ChangeOperatorStatus(id, isActive);
                if (!success)
                {
                    return NotFound(new { message = "Operator not found or status update failed." });
                }

                return Ok(new { message = $"Operator status updated to {(isActive ? "Active" : "Inactive")}." });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return StatusCode(500, new { message = "An unexpected error occurred." });
            }
        }
    }
}
