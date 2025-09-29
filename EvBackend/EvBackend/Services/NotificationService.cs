using EvBackend.Entities;
using Microsoft.AspNetCore.SignalR;
using MongoDB.Driver;
using EvBackend.Hubs;
using EvBackend.Services.Interfaces;

namespace EvBackend.Services
{
    public class NotificationService : INotificationService
    {
        private readonly IMongoCollection<Notification> _notifications;
        private readonly IHubContext<NotificationHub> _hubContext;
        private readonly IUserService _userService;

        public NotificationService(IMongoDatabase db, IHubContext<NotificationHub> hubContext, IUserService userService)
        {
            _notifications = db.GetCollection<Notification>("Notifications");
            _hubContext = hubContext;
            _userService = userService;
        }

        public async Task SendNotification(string userId, string message)
        {
            var notification = new Notification
            {
                UserId = userId,
                Message = message,
                CreatedAt = DateTime.UtcNow,
                IsRead = false
            };
            await _notifications.InsertOneAsync(notification);
            await _hubContext.Clients.User(userId).SendAsync("ReceiveNotification", notification);
        }

        public async Task<List<Notification>> GetUserNotifications(string userId)
        {
            return await _notifications.Find(n => n.UserId == userId)
                .SortByDescending(n => n.CreatedAt)
                .ToListAsync();
        }

        public async Task MarkNotificationAsRead(string notificationId)
        {
            var update = Builders<Notification>.Update.Set(n => n.IsRead, true);
            await _notifications.UpdateOneAsync(n => n.Id == notificationId, update);
        }

        public async Task DeleteNotification(string notificationId)
        {
            await _notifications.DeleteOneAsync(n => n.Id == notificationId);
        }

        public async Task SendNotificationToAdmins(string message)
        {
            var admins = await _userService.GetAllUsers(page: 1, pageSize: 100, role: "Admin");

            foreach (var admin in admins)
            {
                await SendNotification(admin.Id, message);
            }
        }

        public async Task<List<Notification>> GetOwnerNotifications(string nic)
        {
            return await _notifications.Find(n => n.UserId == nic)
                .SortByDescending(n => n.CreatedAt)
                .ToListAsync();
        }
    }
}