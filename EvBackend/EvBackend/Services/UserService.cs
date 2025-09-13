// --------------------------------------------------------------
// File Name: UserService.cs
// Author: Hasindu Koshitha
// Description: Implements business logic for user management
// Created On: 13/09/2025
// --------------------------------------------------------------


namespace EvBackend.Services
{
    public class UserService : IUserService
    {
        private readonly ApplicationDbContext _context;
        private readonly IConfiguration _config;

        public UserService(ApplicationDbContext context, IConfiguration config)
        {
            _context = context;
            _config = config;
        }
    }
}
