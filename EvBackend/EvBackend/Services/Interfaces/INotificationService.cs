// --------------------------------------------------------------
// File Name: INotificationService.cs
// Author: Hasindu Koshitha
// Description: Defines the contract for sending notifications within the system.
// Created On: 28/09/2025
// --------------------------------------------------------------

namespace EvBackend.Services.Interfaces
{
    public interface INotificationService
    {
        Task SendNotification(string userId, string message);
        Task<List<Notification>> GetUserNotifications(string userId);
        // Task<List<Notification>> GetOwnerNotifications(string nic);
        Task MarkNotificationAsRead(string notificationId);
        Task DeleteNotification(string notificationId);
        Task SendNotificationToAdmins(string message);
    }
}
