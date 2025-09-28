// --------------------------------------------------------------
// File Name: IBookingService.cs
// Author: Miyuri Lokuhewage
// Description: Defines contract for booking management service.
// Updated to support simplified booking creation: station + connectorType
// instead of direct slotId, and enforce role-based workflows.
// Created/Updated On: 27/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;

namespace EvBackend.Services.Interfaces
{
    public interface IBookingService
    {
        /// <summary>
        /// Creates a booking for the given station + connectorType by finding
        /// a free slot internally. OwnerId is taken from JWT.
        /// </summary>
        Task<BookingDto> CreateBookingAsync(CreateBookingDto dto, string ownerId);

        /// <summary>
        /// Update an existing booking. Only allowed ≥12 hours before start.
        /// </summary>
        Task<BookingDto> UpdateBookingAsync(string bookingId, UpdateBookingDto dto, string requesterId, string requesterRole);

        /// <summary>
        /// Cancel a booking. Only allowed ≥12 hours before start.
        /// </summary>
        Task<bool> CancelBookingAsync(string bookingId, string requesterId, string requesterRole);

        /// <summary>
        /// Fetch a booking by id.
        /// </summary>
        Task<BookingDto> GetBookingByIdAsync(string bookingId);

        /// <summary>
        /// Fetch all bookings for an owner (NIC).
        /// </summary>
        Task<IEnumerable<BookingDto>> GetBookingsByOwnerAsync(string ownerId);

        /// <summary>
        /// Fetch all bookings for a station.
        /// </summary>
        Task<IEnumerable<BookingDto>> GetBookingsByStationAsync(string stationId);

        /// <summary>
        /// Approve a booking (Operator/Admin/Backoffice).
        /// </summary>
        Task<bool> ApproveBookingAsync(string bookingId, string operatorId);

        /// <summary>
        /// Finalize a booking (Operator/Admin/Backoffice).
        /// </summary>
        Task<bool> FinalizeBookingAsync(string bookingId, string operatorId);

        /// <summary>
        /// Generate QR code for booking check-in.
        /// </summary>
        Task<(string? base64Image, DateTime? expiresAt)> GenerateQrCodeAsync(string bookingId);
    }
}
