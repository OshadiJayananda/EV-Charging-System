// --------------------------------------------------------------
// File Name: ICSOperatorService.cs
// Author: Hasindu Koshitha
// Description: Defines charging station operator management service methods for the system
// Created On: 13/09/2025
// --------------------------------------------------------------

using EvBackend.Models.DTOs;

namespace EvBackend.Services.Interfaces
{
    public interface ICSOperatorService
    {
        Task<CSOperatorDto> CreateOperator(CreateCSOperatorDto dto);
        Task<CSOperatorDto> GetOperatorById(string id);
        Task<IEnumerable<CSOperatorDto>> GetAllOperators(int page, int pageSize);
        Task<CSOperatorDto> UpdateOperator(string id, CSOperatorDto dto);
        Task<bool> ChangeOperatorStatus(string id, bool isActive);
    }
}
