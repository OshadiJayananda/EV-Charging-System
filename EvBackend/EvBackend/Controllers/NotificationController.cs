using Microsoft.AspNetCore.Mvc;
using EvBackend.Services.Interfaces;
using EvBackend.Entities;
using Microsoft.AspNetCore.Authorization;

namespace EvBackend.Controllers
{
    [ApiController]
    [Route("api/notifications")]
    public class NotificationController : ControllerBase
    {
        private readonly INotificationService _notificationService;
        private readonly ILogger<NotificationController> _logger;

        public NotificationController(INotificationService notificationService, ILogger<NotificationController> logger)
        {
            _notificationService = notificationService;
            _logger = logger;
        }

        // POST /api/notifications
        [HttpPost]
        [Authorize]
        public async Task<IActionResult> SendNotification([FromBody] Notification notification)
        {
            await _notificationService.SendNotification(notification.UserId, notification.Message);
            return Ok(notification);
        }

        // GET /api/notifications/user/{userId}
        [HttpGet("user/{userId}")]
        [Authorize]
        public async Task<IActionResult> GetUserNotifications(string userId)
        {
            var notifications = await _notificationService.GetUserNotifications(userId);
            return Ok(notifications);
        }

        // GET /api/notifications/user
        [HttpGet("user")]
        [Authorize]
        public async Task<IActionResult> GetUserNotifications()
        {
            try
            {
                var userId = User.Claims.FirstOrDefault(c => c.Type == System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;

                if (string.IsNullOrEmpty(userId))
                {
                    return Unauthorized(new { message = "User ID not found." });
                }

                var notifications = await _notificationService.GetUserNotifications(userId);

                if (notifications == null || !notifications.Any())
                {
                    return Ok(new List<object>());
                }

                return Ok(notifications);
            }
            catch (Exception ex)
            {
                _logger.LogInformation("Error fetching notifications: {Message}", ex.Message);
                return StatusCode(500, new { message = "An error occurred while fetching notifications.", details = ex.Message });
            }
        }

        // PATCH /api/notifications/{notificationId}/read
        [HttpPatch("{notificationId}/read")]
        [Authorize]
        public async Task<IActionResult> MarkNotificationAsRead(string notificationId)
        {
            await _notificationService.MarkNotificationAsRead(notificationId);
            return Ok();
        }

        // DELETE /api/notifications/{notificationId}
        [HttpDelete("{notificationId}")]
        [Authorize]
        public async Task<IActionResult> DeleteNotification(string notificationId)
        {
            await _notificationService.DeleteNotification(notificationId);
            return Ok();
        }
    }
}