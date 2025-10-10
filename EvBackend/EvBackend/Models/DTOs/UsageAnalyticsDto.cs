// --------------------------------------------------------------
// File Name: UsageAnalyticsDto.cs
// Author: Denuwan Sathsara
// Description: Data transfer object for usage analytics.
// Created/Updated On: 27/09/2025
// --------------------------------------------------------------

using System;

namespace EvBackend.Models.DTOs;

public class UsageAnalyticsDto
{
    public string MostPopularStation { get; set; }
    public int MostPopularStationCount { get; set; }
    public DateTime? MostPopularTimeSlotStartTime { get; set; }
    public int MostPopularTimeSlotCount { get; set; }
}
