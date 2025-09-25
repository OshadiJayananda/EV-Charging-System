// --------------------------------------------------------------
// File Name: UserService.cs
// Author: Hasindu Koshitha
// Description: Implements business logic for user management
// Created On: 13/09/2025
// --------------------------------------------------------------

using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using MongoDB.Driver;
using MongoDB.Bson;
using EvBackend.Entities;
using EvBackend.Models.DTOs;
using EvBackend.Settings;
using System.Security.Authentication;
using EvBackend.Services.Interfaces;

namespace EvBackend.Services
{
    public class UserService : IUserService
    {
        private readonly IMongoCollection<User> _users;
        private readonly IConfiguration _config;

        public UserService(IMongoDatabase database, IConfiguration config, IOptions<MongoDbSettings> settings)
        {
            _users = database.GetCollection<User>(settings.Value.UsersCollectionName);
            _config = config;
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

        public async Task<LoginResponseDto> AuthenticateUser(LoginDto loginDto)
        {
            var user = await _users.Find(u => u.Email == loginDto.Email).FirstOrDefaultAsync();

            if (user == null || !user.IsActive)
                throw new AuthenticationException("Invalid credentials or user inactive");

            if (!BCrypt.Net.BCrypt.Verify(loginDto.Password, user.PasswordHash))
                throw new AuthenticationException("Invalid credentials");

            var tokenHandler = new JwtSecurityTokenHandler();
            var secretKey = _config["Jwt:Key"] ?? _config["Jwt__Key"];
            var issuer = _config["Jwt:Issuer"] ?? _config["Jwt__Issuer"];
            var audience = _config["Jwt:Audience"] ?? _config["Jwt__Audience"];
            
            if (string.IsNullOrEmpty(secretKey))
                throw new InvalidOperationException("JWT Key not found in configuration");
                
            var key = Encoding.ASCII.GetBytes(secretKey);
            var tokenDescriptor = new SecurityTokenDescriptor
            {
                Subject = new ClaimsIdentity(new[]
                {
                    new Claim(ClaimTypes.NameIdentifier, user.Id),
                    new Claim(ClaimTypes.Email, user.Email),
                    new Claim(ClaimTypes.Role, user.Role),
                    new Claim("FullName", user.FullName)
                }),
                Expires = DateTime.UtcNow.AddHours(2),
                Issuer = issuer,
                Audience = audience,
                SigningCredentials = new SigningCredentials(
                    new SymmetricSecurityKey(key), SecurityAlgorithms.HmacSha256Signature)
            };

            var token = tokenHandler.CreateToken(tokenDescriptor);
            var tokenString = tokenHandler.WriteToken(token);

            return new LoginResponseDto
            {
                Token = tokenString
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
