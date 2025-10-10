// --------------------------------------------------------------
// File Name: IUsageAnalyticsService.cs
// Author: Denuwan Sathsara
// Description: Service interface for usage analytics.
// Created/Updated On: 27/09/2025
// --------------------------------------------------------------

using System;
using EvBackend.Models.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace EvBackend.Services.Interfaces;

public interface IUsageAnalyticsService
{
    Task<UsageAnalyticsDto> GetUsageAnalyticsAsync();


}
