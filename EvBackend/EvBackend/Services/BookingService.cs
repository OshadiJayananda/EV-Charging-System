// --------------------------------------------------------------
// File Name: BookingService.cs
// Author: Miyuri Lokuhewage
// Description: Implements booking management logic.
// Owners request station + connectorType, service finds free slot,
// creates booking, and enforces business rules.
// Created/Updated On: 27/09/2025
// --------------------------------------------------------------

using EvBackend.Entities;
using EvBackend.Models.DTOs;
using EvBackend.Services.Interfaces;
using MongoDB.Driver;
using QRCoder;

namespace EvBackend.Services
{
    public class BookingService : IBookingService
    {
        private readonly IMongoDatabase _db;

        public BookingService(IMongoDatabase db)
        {
            _db = db;
        }

        // ---------------------------
        // 📌 Create Booking
        // ---------------------------
        public async Task<BookingDto> CreateBookingAsync(CreateBookingDto dto, string ownerId)
        {
            if (dto.StartTime >= dto.EndTime)
                throw new ArgumentException("StartTime must be before EndTime");

            // ✅ 7-day validation
            if (dto.StartTime > DateTime.UtcNow.AddDays(7))
                throw new InvalidOperationException("Bookings can only be made within 7 days from today");

            if (dto.StartTime < DateTime.UtcNow)
                throw new InvalidOperationException("Booking start time cannot be in the past");

            var stations = _db.GetCollection<Station>("Stations");
            var station = await stations.Find(s => s.StationId == dto.StationId).FirstOrDefaultAsync();

            if (station == null) throw new ArgumentException("Station not found");
            if (!station.IsActive) throw new InvalidOperationException("Station is not active");

            var slots = _db.GetCollection<Slot>("Slots");

            // ✅ Capacity vs slot count validation
            var totalSlots = await slots.CountDocumentsAsync(s => s.StationId == dto.StationId);
            if (totalSlots < station.Capacity)
            {
                throw new InvalidOperationException(
                    $"Station capacity is {station.Capacity}, but only {totalSlots} slots have been created. Please contact operator.");
            }

            // 🔎 Find an Available slot of requested type
            var freeSlot = await slots.Find(s =>
        s.StationId == dto.StationId &&
        s.ConnectorType == dto.ConnectorType &&
        s.Status == "Available").FirstOrDefaultAsync();

            if (freeSlot == null)
                throw new InvalidOperationException($"No available {dto.ConnectorType} slots in this station. Try another time or slot type.");

            // TODO: check overlapping bookings for same slot/time

            var now = DateTime.UtcNow;
            var bookings = _db.GetCollection<Booking>("Bookings");

            var newBooking = new Booking
            {
                BookingId = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
                StationId = dto.StationId,
                SlotId = freeSlot.SlotId,
                OwnerId = ownerId,
                Status = "Pending",
                StartTime = dto.StartTime,
                EndTime = dto.EndTime,
                CreatedAt = now,
                UpdatedAt = now
            };

            // ✅ Generate QR Code only if slot found
            var token = Guid.NewGuid().ToString();
            var expiresAt = dto.StartTime > DateTime.UtcNow.AddMinutes(15)
                ? DateTime.UtcNow.AddMinutes(15)
                : dto.StartTime;

            using var qrGen = new QRCoder.QRCodeGenerator();
            var qrData = qrGen.CreateQrCode(token, QRCoder.QRCodeGenerator.ECCLevel.Q);
            var pngQr = new QRCoder.PngByteQRCode(qrData);
            var bytes = pngQr.GetGraphic(20);
            string base64 = Convert.ToBase64String(bytes);

            newBooking.QrCode = token;
            newBooking.QrExpiresAt = expiresAt;
            newBooking.QrImageBase64 = base64;

            await bookings.InsertOneAsync(newBooking);

            // Mark slot as booked
            var update = Builders<Slot>.Update
                .Set(s => s.Status, "Booked")
                .Set(s => s.StartTime, dto.StartTime)
                .Set(s => s.EndTime, dto.EndTime);

            await slots.UpdateOneAsync(s => s.SlotId == freeSlot.SlotId, update);

            return new BookingDto
            {
                BookingId = newBooking.BookingId,
                StationId = newBooking.StationId,
                SlotId = newBooking.SlotId,
                OwnerId = newBooking.OwnerId,
                Status = newBooking.Status,
                StartTime = newBooking.StartTime,
                EndTime = newBooking.EndTime,
                CreatedAt = newBooking.CreatedAt,
                UpdatedAt = newBooking.UpdatedAt,
                QrCode = newBooking.QrCode,
                QrExpiresAt = newBooking.QrExpiresAt,
                QrImageBase64 = newBooking.QrImageBase64
            };
        }

        // ---------------------------
        // 📌 Update Booking
        // ---------------------------
        public async Task<BookingDto> UpdateBookingAsync(string bookingId, UpdateBookingDto dto, string requesterId, string requesterRole)
        {
            var bookings = _db.GetCollection<Booking>("Bookings");
            var booking = await bookings.Find(b => b.BookingId == bookingId).FirstOrDefaultAsync();
            if (booking == null) return null;

            // Only owner may update own booking and only >=12 hours before start
            if (requesterRole == "Owner" && booking.OwnerId != requesterId)
                throw new UnauthorizedAccessException();

            var cutoff = booking.StartTime.AddHours(-12);
            if (DateTime.UtcNow > cutoff)
                throw new InvalidOperationException("Cannot update booking within 12 hours of start");

            var update = Builders<Booking>.Update
                .Set(b => b.StartTime, dto.StartTime)
                .Set(b => b.EndTime, dto.EndTime)
                .Set(b => b.UpdatedAt, DateTime.UtcNow);

            var options = new FindOneAndUpdateOptions<Booking> { ReturnDocument = ReturnDocument.After };
            var updated = await bookings.FindOneAndUpdateAsync(b => b.BookingId == bookingId, update, options);

            return new BookingDto
            {
                BookingId = updated.BookingId,
                StationId = updated.StationId,
                SlotId = updated.SlotId,
                OwnerId = updated.OwnerId,
                Status = updated.Status,
                StartTime = updated.StartTime,
                EndTime = updated.EndTime,
                CreatedAt = updated.CreatedAt,
                UpdatedAt = updated.UpdatedAt
            };
        }

        // ---------------------------
        // 📌 Cancel Booking
        // ---------------------------
        public async Task<bool> CancelBookingAsync(string bookingId, string requesterId, string requesterRole)
        {
            var bookings = _db.GetCollection<Booking>("Bookings");
            var booking = await bookings.Find(b => b.BookingId == bookingId).FirstOrDefaultAsync();
            if (booking == null) throw new ArgumentException("Booking not found");

            if (requesterRole == "Owner" && booking.OwnerId != requesterId)
                throw new UnauthorizedAccessException();

            var cutoff = booking.StartTime.AddHours(-12);
            if (DateTime.UtcNow > cutoff)
                throw new InvalidOperationException("Cannot cancel booking within 12 hours of start");

            var update = Builders<Booking>.Update
                .Set(b => b.Status, "Cancelled")
                .Set(b => b.UpdatedAt, DateTime.UtcNow);

            await bookings.UpdateOneAsync(b => b.BookingId == bookingId, update);

            // free slot
            var slots = _db.GetCollection<Slot>("Slots");
            await slots.UpdateOneAsync(s => s.SlotId == booking.SlotId,
                Builders<Slot>.Update.Set(s => s.Status, "Available"));

            return true;
        }

        // ---------------------------
        // 📌 Get Booking(s)
        // ---------------------------
        public async Task<BookingDto> GetBookingByIdAsync(string bookingId)
        {
            var bookings = _db.GetCollection<Booking>("Bookings");
            var b = await bookings.Find(bk => bk.BookingId == bookingId).FirstOrDefaultAsync();
            if (b == null) return null;

            return new BookingDto
            {
                BookingId = b.BookingId,
                StationId = b.StationId,
                SlotId = b.SlotId,
                OwnerId = b.OwnerId,
                Status = b.Status,
                StartTime = b.StartTime,
                EndTime = b.EndTime,
                CreatedAt = b.CreatedAt,
                UpdatedAt = b.UpdatedAt,
                QrCode = b.QrCode,
                QrExpiresAt = b.QrExpiresAt,
                QrImageBase64 = b.QrImageBase64
            };
        }

        public async Task<IEnumerable<BookingDto>> GetBookingsByOwnerAsync(string ownerId)
        {
            var bookings = _db.GetCollection<Booking>("Bookings");
            var list = await bookings.Find(b => b.OwnerId == ownerId).ToListAsync();

            return list.Select(b => new BookingDto
            {
                BookingId = b.BookingId,
                StationId = b.StationId,
                SlotId = b.SlotId,
                OwnerId = b.OwnerId,
                Status = b.Status,
                StartTime = b.StartTime,
                EndTime = b.EndTime,
                CreatedAt = b.CreatedAt,
                UpdatedAt = b.UpdatedAt,
                QrCode = b.QrCode,
                QrExpiresAt = b.QrExpiresAt,
                QrImageBase64 = b.QrImageBase64
            });
        }

        public async Task<IEnumerable<BookingDto>> GetBookingsByStationAsync(string stationId)
        {
            var bookings = _db.GetCollection<Booking>("Bookings");
            var list = await bookings.Find(b => b.StationId == stationId).ToListAsync();

            return list.Select(b => new BookingDto
            {
                BookingId = b.BookingId,
                StationId = b.StationId,
                SlotId = b.SlotId,
                OwnerId = b.OwnerId,
                Status = b.Status,
                StartTime = b.StartTime,
                EndTime = b.EndTime,
                CreatedAt = b.CreatedAt,
                UpdatedAt = b.UpdatedAt,
                QrCode = b.QrCode,
                QrExpiresAt = b.QrExpiresAt,
                QrImageBase64 = b.QrImageBase64
            });
        }

        // ---------------------------
        // 📌 Operator Actions
        // ---------------------------
        public async Task<bool> ApproveBookingAsync(string bookingId, string operatorId)
        {
            var bookings = _db.GetCollection<Booking>("Bookings");
            var booking = await bookings.Find(b => b.BookingId == bookingId).FirstOrDefaultAsync();
            if (booking == null) return false;

            var update = Builders<Booking>.Update
                .Set(b => b.Status, "Approved")
                .Set(b => b.UpdatedAt, DateTime.UtcNow);

            await bookings.UpdateOneAsync(b => b.BookingId == bookingId, update);
            return true;
        }

        public async Task<bool> FinalizeBookingAsync(string bookingId, string operatorId)
        {
            var bookings = _db.GetCollection<Booking>("Bookings");
            var booking = await bookings.Find(b => b.BookingId == bookingId).FirstOrDefaultAsync();
            if (booking == null) return false;

            var update = Builders<Booking>.Update
                .Set(b => b.Status, "Finalized")
                .Set(b => b.UpdatedAt, DateTime.UtcNow);

            await bookings.UpdateOneAsync(b => b.BookingId == bookingId, update);

            // free slot again
            var slots = _db.GetCollection<Slot>("Slots");
            await slots.UpdateOneAsync(s => s.SlotId == booking.SlotId,
                Builders<Slot>.Update.Set(s => s.Status, "Available"));

            return true;
        }

        // ---------------------------
        // 📌 QR Code
        // ---------------------------
        public async Task<(string? base64Image, DateTime? expiresAt)> GenerateQrCodeAsync(string bookingId)
        {
            var bookings = _db.GetCollection<Booking>("Bookings");
            var booking = await bookings.Find(b => b.BookingId == bookingId).FirstOrDefaultAsync();
            if (booking == null) return (null, null);

            var token = Guid.NewGuid().ToString();
            var expiresAt = booking.StartTime > DateTime.UtcNow.AddMinutes(15)
                ? DateTime.UtcNow.AddMinutes(15)
                : booking.StartTime;

            using var qrGen = new QRCodeGenerator();
            var qrData = qrGen.CreateQrCode(token, QRCodeGenerator.ECCLevel.Q);
            var pngQr = new PngByteQRCode(qrData);
            var bytes = pngQr.GetGraphic(20);
            string base64 = Convert.ToBase64String(bytes);

            var update = Builders<Booking>.Update
                .Set(b => b.QrCode, token)
                .Set(b => b.QrExpiresAt, expiresAt)
                .Set(b => b.QrImageBase64, base64)
                .Set(b => b.UpdatedAt, DateTime.UtcNow);

            await bookings.UpdateOneAsync(b => b.BookingId == bookingId, update);

            return (base64, expiresAt);
        }

        public async Task<long> CountPendingBookingsAsync()
        {
            var bookings = _db.GetCollection<Booking>("Bookings");
            return await bookings.CountDocumentsAsync(b => b.Status == "Pending");
        }

        public async Task<long> CountApprovedFutureBookingsAsync()
        {
            var bookings = _db.GetCollection<Booking>("Bookings");
            return await bookings.CountDocumentsAsync(
                b => b.Status == "Approved" && b.StartTime > DateTime.UtcNow
            );
        }

    }
}
