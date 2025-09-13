// --------------------------------------------------------------
// File Name: IUserService.cs
// Author: Hasindu Koshitha
// Description: Defines user management service methods for the system
// Created On: 13/09/2025
// --------------------------------------------------------------


using EvBackend.Models.DTOs;

namespace EvBackend.Services
{
    public class IUserService
    {
        Task<UserDto> CreateUser(CreateUserDto dto);
        Task<UserDto> GetUserById(Guid userId);
        Task<IEnumerable<UserDto>> GetAllUsers(int page, int pageSize);
        Task<UserDto> UpdateUser(Guid userId, UserDto dto);
        Task ChangeUserStatus(Guid userId, bool isActive);
        Task<string> AuthenticateUser(LoginDto loginDto);
    }
}
