// --------------------------------------------------------------
// File Name: IAdminService.cs
// Author: Hasindu Koshitha
// Description: Defines admin management service methods for the system
// Created On: 13/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;

namespace EvBackend.Services.Interfaces
{
    public interface IAdminService
    {
        Task<UserDto> CreateAdmin(CreateUserDto dto);
        Task<UserDto> GetAdminById(string id);
        Task<IEnumerable<UserDto>> GetAllAdmins(int page, int pageSize);
        Task<UserDto> UpdateAdmin(string id, UserDto dto);
        Task<bool> ChangeAdminStatus(string id, bool isActive);
    }
}
