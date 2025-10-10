using System;
using EvBackend.Models.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace EvBackend.Services.Interfaces;

public interface IUsageAnalyticsService
{
    Task<UsageAnalyticsDto> GetUsageAnalyticsAsync();


}
