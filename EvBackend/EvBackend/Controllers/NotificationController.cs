using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using MongoDB.Driver;
using EvBackend.Hubs;

namespace EvBackend.Controllers
{
    [ApiController]
    [Route("api/notifications")]
    public class NotificationController : ControllerBase
    {
        private readonly IMongoCollection<Notification> _notifications;
        private readonly IHubContext<NotificationHub> _hubContext;

        public NotificationController(IMongoDatabase db, IHubContext<NotificationHub> hubContext)
        {
            _notifications = db.GetCollection<Notification>("Notifications");
            _hubContext = hubContext;
        }

        // POST /api/notifications
        [HttpPost]
        public async Task<IActionResult> SendNotification([FromBody] Notification notification)
        {
            notification.CreatedAt = DateTime.UtcNow;
            notification.IsRead = false;
            await _notifications.InsertOneAsync(notification);

            // Send to user via SignalR
            await _hubContext.Clients.User(notification.UserId).SendAsync("ReceiveNotification", notification);

            return Ok(notification);
        }

        // GET /api/notifications/user/{userId}
        [HttpGet("user/{userId}")]
        public async Task<IActionResult> GetUserNotifications(string userId)
        {
            var notifications = await _notifications.Find(n => n.UserId == userId)
                .SortByDescending(n => n.CreatedAt)
                .ToListAsync();
            return Ok(notifications);
        }

        // PATCH /api/notifications/{notificationId}/read
        [HttpPatch("{notificationId}/read")]
        public async Task<IActionResult> MarkNotificationAsRead(string notificationId)
        {
            var update = Builders<Notification>.Update.Set(n => n.IsRead, true);
            await _notifications.UpdateOneAsync(n => n.Id == notificationId, update);
            return Ok();
        }

        // DELETE /api/notifications/{notificationId}
        [HttpDelete("{notificationId}")]
        public async Task<IActionResult> DeleteNotification(string notificationId)
        {
            await _notifications.DeleteOneAsync(n => n.Id == notificationId);
            return Ok();
        }
    }
}