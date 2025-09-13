// --------------------------------------------------------------
// File Name: ICSOperatorService.cs
// Author: Hasindu Koshitha
// Description: Defines charging station operator management service methods for the system
// Created On: 13/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;

namespace EvBackend.Services
{
    public interface ICSOperatorService
    {
        Task<UserDto> CreateOperator(CreateCSOperatorDto dto);
        Task<UserDto> GetOperatorById(string id);
        Task<IEnumerable<UserDto>> GetAllOperators(int page, int pageSize);
        Task<UserDto> UpdateOperator(string id, UserDto dto);
        Task<bool> ChangeOperatorStatus(string id, bool isActive);
    }
}
