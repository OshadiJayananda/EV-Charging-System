using Microsoft.AspNetCore.SignalR;
using System.Security.Claims;

namespace EvBackend.Hubs
{
    public class CustomUserIdProvider : IUserIdProvider
    {
        public string GetUserId(HubConnectionContext connection)
        {
            // This gets the "sub" or "nameidentifier" claim from the JWT
            return connection.User?.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        }
    }
}
