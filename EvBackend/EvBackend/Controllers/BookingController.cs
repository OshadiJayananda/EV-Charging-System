// --------------------------------------------------------------
// File Name: BookingController.cs
// Author: Miyuri Lokuhewage
// Description: Booking management endpoints (Owners, Operators, Admins).
// Owners can create/update/cancel their own bookings,
// Operators approve/finalize bookings in their station,
// Admin/Backoffice have full access. Includes consistent
// error handling (400/404/409) and rule enforcement.
// Created/Updated On: 27/09/2025
// --------------------------------------------------------------

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using EvBackend.Services.Interfaces;
using EvBackend.Models.DTOs;

namespace EvBackend.Controllers
{
    [ApiController]
    [Route("api/bookings")]
    public class BookingController : ControllerBase
    {
        private readonly IBookingService _bookingService;

        public BookingController(IBookingService bookingService)
        {
            _bookingService = bookingService;
        }

        // ---------------------------
        // 📌 Owner Endpoints
        // ---------------------------

        // CreateBooking: Owner books by selecting station + connectorType + times
        [HttpPost]
        [Authorize(Roles = "Owner,Admin,Backoffice")]
        public async Task<IActionResult> CreateBooking([FromBody] CreateBookingDto dto)
        {
            if (dto == null) return BadRequest(new { message = "Booking data required" });
            if (string.IsNullOrWhiteSpace(dto.StationId) || string.IsNullOrWhiteSpace(dto.ConnectorType))
                return BadRequest(new { message = "StationId and ConnectorType are required" });

            // Owner NIC from JWT
            var requesterId = User?.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrWhiteSpace(requesterId))
                return Unauthorized(new { message = "OwnerId required" });

            try
            {
                var created = await _bookingService.CreateBookingAsync(dto, requesterId);
                return CreatedAtAction(nameof(GetBookingById), new { bookingId = created.BookingId }, created);
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message }); // Invalid station/slot
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message }); // 7-day limit, past date, station inactive
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        // UpdateBooking: Owner may update only >=12 hours before start
        [HttpPut("{bookingId}")]
        [Authorize(Roles = "Owner,Admin,Backoffice")]
        public async Task<IActionResult> UpdateBooking(string bookingId, [FromBody] UpdateBookingDto dto)
        {
            var requesterId = User?.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;
            var requesterRole = User?.FindFirst(System.Security.Claims.ClaimTypes.Role)?.Value;

            try
            {
                var updated = await _bookingService.UpdateBookingAsync(bookingId, dto, requesterId, requesterRole);
                if (updated == null) return NotFound(new { message = "Booking not found" });

                return Ok(updated);
            }
            catch (UnauthorizedAccessException)
            {
                return Forbid();
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message }); // <12h before start
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        // CancelBooking: Owner may cancel >=12 hours before start
        [HttpPatch("{bookingId}/cancel")]
        [Authorize(Roles = "Owner,Admin,Backoffice")]
        public async Task<IActionResult> CancelBooking(string bookingId)
        {
            var requesterId = User?.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;
            var requesterRole = User?.FindFirst(System.Security.Claims.ClaimTypes.Role)?.Value;

            try
            {
                var ok = await _bookingService.CancelBookingAsync(bookingId, requesterId, requesterRole);
                if (!ok) return NotFound(new { message = "Booking not found" });

                return Ok(new { message = "Booking cancelled" });
            }
            catch (UnauthorizedAccessException)
            {
                return Forbid();
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message }); // <12h before start
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        // ---------------------------
        // 📌 Shared / General Endpoints
        // ---------------------------

        [HttpGet("{bookingId}")]
        [Authorize]
        public async Task<IActionResult> GetBookingById(string bookingId)
        {
            try
            {
                var dto = await _bookingService.GetBookingByIdAsync(bookingId);
                if (dto == null) return NotFound(new { message = "Booking not found" });

                return Ok(dto);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        [HttpGet("owner/{ownerId}")]
        [Authorize(Roles = "Owner,Admin,Backoffice")]
        public async Task<IActionResult> GetBookingsByOwner(string ownerId)
        {
            try
            {
                var list = await _bookingService.GetBookingsByOwnerAsync(ownerId);
                return Ok(list);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        [HttpGet("station/{stationId}")]
        [Authorize(Roles = "Admin,Backoffice,Operator")]
        public async Task<IActionResult> GetBookingsByStation(string stationId)
        {
            try
            {
                var list = await _bookingService.GetBookingsByStationAsync(stationId);
                return Ok(list);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        // ---------------------------
        // 📌 Operator Endpoints
        // ---------------------------

        [HttpPatch("{bookingId}/approve")]
        [Authorize(Roles = "Operator,Admin,Backoffice")]
        public async Task<IActionResult> ApproveBooking(string bookingId)
        {
            var operatorId = User?.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;

            try
            {
                var ok = await _bookingService.ApproveBookingAsync(bookingId, operatorId);
                if (!ok) return NotFound(new { message = "Booking not found or already processed" });

                return Ok(new { message = "Booking approved" });
            }
            catch (UnauthorizedAccessException)
            {
                return Forbid();
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        [HttpPatch("{bookingId}/finalize")]
        [Authorize(Roles = "Operator,Admin,Backoffice")]
        public async Task<IActionResult> FinalizeBooking(string bookingId)
        {
            var operatorId = User?.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;

            try
            {
                var ok = await _bookingService.FinalizeBookingAsync(bookingId, operatorId);
                if (!ok) return NotFound(new { message = "Booking not found or already finalized" });

                return Ok(new { message = "Booking finalized" });
            }
            catch (UnauthorizedAccessException)
            {
                return Forbid();
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        // Generate QR Code for booking check-in
        [HttpGet("{bookingId}/qrcode")]
        [Authorize]
        public async Task<IActionResult> GenerateQrCode(string bookingId)
        {
            try
            {
                var (base64, expiresAt) = await _bookingService.GenerateQrCodeAsync(bookingId);
                if (base64 == null)
                    return NotFound(new { message = "Booking not found or QR generation failed" });

                return Ok(new { qrcodeBase64 = base64, expiresAt });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        [HttpGet("count/pending")]
        //[Authorize(Roles = "Admin,Backoffice,Operator")]
        public async Task<IActionResult> CountPendingBookings()
        {
            try
            {
                var count = await _bookingService.CountPendingBookingsAsync();
                return Ok(new { pendingCount = count });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

        [HttpGet("count/approved")]
        //[Authorize(Roles = "Admin,Backoffice,Operator")]
        public async Task<IActionResult> CountApprovedFutureBookings()
        {
            try
            {
                var count = await _bookingService.CountApprovedFutureBookingsAsync();
                return Ok(new { approvedCount = count });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return StatusCode(500, new { message = "Unexpected error" });
            }
        }

    }
}
