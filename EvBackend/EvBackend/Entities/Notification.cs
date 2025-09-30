namespace EvBackend.Entities
{
    public class Notification
    {
        public string Id { get; set; }
        public string UserId { get; set; }
        public string Message { get; set; }
        public bool IsRead { get; set; }
        public DateTime CreatedAt { get; set; }
    }
}