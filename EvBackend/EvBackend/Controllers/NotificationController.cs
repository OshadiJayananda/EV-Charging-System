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

        public NotificationController(INotificationService notificationService)
        {
            _notificationService = notificationService;
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