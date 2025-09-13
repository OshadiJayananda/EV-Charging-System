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
        Task<EVOwnerDto> CreateEVOwner(CreateEVOwnerDto dto);
        Task<EVOwnerDto> GetEVOwnerByNIC(string nic);
        Task<IEnumerable<EVOwnerDto>> GetAllEVOwners(int page, int pageSize);
        Task<EVOwnerDto> UpdateEVOwner(string nic, CreateEVOwnerDto dto);
        Task<bool> ChangeEVOwnerStatus(string nic, bool isActive);
    }
}
