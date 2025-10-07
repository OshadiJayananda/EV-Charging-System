namespace EvBackend.DTOs
{
    public class NotificationDto
    {
        public string UserId { get; set; }   // The recipient's user ID
        public string Message { get; set; }  // Notification message text
    }
}