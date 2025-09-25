// --------------------------------------------------------------
// File Name: UserService.cs
// Author: Hasindu Koshitha
// Description: Implements business logic for user management
// Created On: 13/09/2025
// --------------------------------------------------------------

using Microsoft.Extensions.Options;
using MongoDB.Driver;
using MongoDB.Bson;
using EvBackend.Entities;
using EvBackend.Models.DTOs;
using EvBackend.Settings;
using EvBackend.Services.Interfaces;

namespace EvBackend.Services
{
    public class UserService : IUserService
    {
        private readonly IMongoCollection<User> _users;

        public UserService(IMongoDatabase database, IOptions<MongoDbSettings> settings)
        {
            _users = database.GetCollection<User>(settings.Value.UsersCollectionName);
        }

        public async Task<UserDto> CreateUser(CreateUserDto dto)
        {
            if (await _users.Find(u => u.Email == dto.Email).AnyAsync())
                throw new Exception("Email already in use");

            var allowedRoles = new[] { "Admin", "Operator" };
            if (!allowedRoles.Contains(dto.Role))
                throw new Exception("Invalid role assigned");

            var user = new User
            {
                Id = ObjectId.GenerateNewId().ToString(),
                FullName = dto.FullName,
                Email = dto.Email,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password),
                Role = dto.Role,
                IsActive = true
            };

            await _users.InsertOneAsync(user);

            return new UserDto
            {
                Id = user.Id,
                FullName = user.FullName,
                Email = user.Email,
                Role = user.Role,
                IsActive = user.IsActive
            };
        }

        public async Task<UserDto> GetUserById(String userId)
        {
            var user = await _users.Find(u => u.Id == userId.ToString()).FirstOrDefaultAsync();
            if (user == null) throw new Exception("User not found");

            return new UserDto
            {
                Id = user.Id,
                FullName = user.FullName,
                Email = user.Email,
                Role = user.Role,
                IsActive = user.IsActive
            };
        }

        public async Task<IEnumerable<UserDto>> GetAllUsers(int page = 1, int pageSize = 10)
        {
            return await _users.Find(u => true)
                               .Skip((page - 1) * pageSize)
                               .Limit(pageSize)
                               .Project(u => new UserDto
                               {
                                   Id = u.Id,
                                   FullName = u.FullName,
                                   Email = u.Email,
                                   Role = u.Role,
                                   IsActive = u.IsActive
                               }).ToListAsync();
        }

        public async Task<UserDto> UpdateUser(String userId, UserDto dto)
        {
            var update = Builders<User>.Update
                .Set(u => u.FullName, dto.FullName)
                .Set(u => u.Role, dto.Role);

            var result = await _users.UpdateOneAsync(u => u.Id == userId.ToString(), update);

            if (result.MatchedCount == 0) throw new Exception("User not found");

            var updatedUser = await _users.Find(u => u.Id == userId.ToString()).FirstOrDefaultAsync();

            return new UserDto
            {
                Id = updatedUser.Id,
                FullName = updatedUser.FullName,
                Email = updatedUser.Email,
                Role = updatedUser.Role,
                IsActive = updatedUser.IsActive
            };
        }

        public async Task<bool> ChangeUserStatus(string userId, bool isActive)
        {
            var update = Builders<User>.Update.Set(u => u.IsActive, isActive);

            var result = await _users.UpdateOneAsync(u => u.Id == userId, update);

            if (result.MatchedCount == 0)
                return false;

            return true;
        }
    }
}
