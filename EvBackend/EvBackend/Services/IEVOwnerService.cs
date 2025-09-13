// --------------------------------------------------------------
// File Name: IEVOwnerService.cs
// Author: Hasindu Koshitha
// Description: Defines electric vehicle owner management service methods for the system
// Created On: 13/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;

namespace EvBackend.Services
{
    public interface IEVOwnerService
    {
        Task<UserDto> CreateEVOwner(CreateEVOwnerDto dto);
        Task<UserDto> GetEVOwnerByNIC(string nic);
        Task<IEnumerable<UserDto>> GetAllEVOwners(int page, int pageSize);
        Task<UserDto> UpdateEVOwner(string nic, UserDto dto);
        Task<bool> ChangeEVOwnerStatus(string nic, bool isActive);
    }
}
