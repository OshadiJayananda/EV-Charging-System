// --------------------------------------------------------------
// File Name: IUserService.cs
// Author: Hasindu Koshitha
// Description: Defines user management service methods for the system
// Created On: 13/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;

namespace EvBackend.Services.Interfaces
{
    public interface IUserService
    {
        Task<UserDto> CreateUser(CreateUserDto dto);
        Task<UserDto> GetUserById(String userId);
        Task<IEnumerable<UserDto>> GetAllUsers(int page, int pageSize);
        Task<UserDto> UpdateUser(String userId, UserDto dto);
        Task<bool> ChangeUserStatus(String userId, bool isActive);
    }
}
