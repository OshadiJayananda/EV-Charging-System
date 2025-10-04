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

        // ✅ Create Operator
        [HttpPost]
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

        // ✅ Get Operator by ID
        [HttpGet("{id}")]
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

        // ✅ Get All Operators (Paginated)
        [HttpGet]
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

        // ✅ Update Operator
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateOperator(string id, [FromBody] CSOperatorDto csOperatorDto)
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

        // ✅ Change Operator Status (Activate / Deactivate)
        [HttpPatch("{id}/status")]
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
