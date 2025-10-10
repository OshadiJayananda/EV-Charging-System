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
        //create operator
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

        //get operator by id
        [HttpGet("{id}")]
        //[Authorize(Roles = "Admin,Operator")]
        public async Task<IActionResult> GetOperatorById(string id)
        {
            try
            {
                // If user is Operator, they can only view their own details
                if (User.IsInRole("Operator"))
                {
                    var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
                    if (userId != id)
                        return Forbid();
                }

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

        //get all operators with pagination
        [HttpGet]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> GetAllOperators([FromQuery] int page = 1, [FromQuery] int pageSize = 10)
        {
            try
            {
                var response = await _csOperatorService.GetAllPaginatedOperators(page, pageSize);
                return Ok(response);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return StatusCode(500, new { message = "An unexpected error occurred." });
            }
        }

        //update operator
        [HttpPut("{id}")]
        [Authorize(Roles = "Admin,Operator")]
        public async Task<IActionResult> UpdateOperator(string id, [FromBody] UpdateCSOperatorDto csOperatorDto)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(new { message = "Invalid operator data." });
            }

            try
            {
                // If user is Operator, they can only update their own details
                if (User.IsInRole("Operator"))
                {
                    var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
                    if (userId != id)
                        return Forbid();
                }

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

        //change operator status (active/inactive)
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

        //deactivate self (operator)
        [HttpPatch("{id}/deactivate")]
        [Authorize(Roles = "Operator")]
        public async Task<IActionResult> DeactivateSelf(string id)
        {
            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (userId != id)
                return Forbid();

            try
            {
                // First clear any existing reactivation request when operator deactivates themselves
                await _csOperatorService.ClearReactivationRequest(id);
                var ok = await _csOperatorService.ChangeOperatorStatus(id, false);
                if (!ok) return NotFound(new { message = "Operator not found." });
                return Ok(new { message = "Account deactivated." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to deactivate account", error = ex.Message });
            }
        }

        //request reactivation (operator)
        [HttpPatch("{id}/request-reactivation")]
        [Authorize(Roles = "Operator")]
        public async Task<IActionResult> RequestReactivation(string id)
        {
            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (userId != id)
                return Forbid();

            try
            {
                var ok = await _csOperatorService.RequestReactivation(id);
                if (!ok) return BadRequest(new { message = "Could not request reactivation." });
                return Ok(new { message = "Reactivation request submitted successfully." });
            }
            catch (KeyNotFoundException)
            {
                return NotFound(new { message = "Operator not found." });
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        //get count of reactivation requests (admin)
        [HttpGet("reactivation-count")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> GetReactivationRequestCount()
        {
            try
            {
                var count = await _csOperatorService.GetReactivationRequestCount();
                return Ok(new { count });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to fetch reactivation count", error = ex.Message });
            }
        }

        //get all operators with reactivation requests (admin)
        [HttpGet("reactivation-requests")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> GetReactivationRequests()
        {
            try
            {
                var requests = await _csOperatorService.GetOperatorsWithReactivationRequests();
                return Ok(requests);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to fetch reactivation requests", error = ex.Message });
            }
        }

        //clear reactivation request (admin)
        [HttpPatch("{id}/clear-reactivation")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> ClearReactivationRequest(string id)
        {
            try
            {
                var ok = await _csOperatorService.ClearReactivationRequest(id);
                if (!ok) return NotFound(new { message = "Operator not found or no reactivation request to clear." });
                return Ok(new { message = "Reactivation request cleared successfully." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to clear reactivation request", error = ex.Message });
            }
        }
    }
}