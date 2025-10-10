using System;

namespace EvBackend.Models.DTOs;

public class UsageAnalyticsDto
{
    public string MostPopularStation { get; set; }
    public int MostPopularStationCount { get; set; }
    public DateTime? MostPopularTimeSlotStartTime { get; set; }
    public int MostPopularTimeSlotCount { get; set; }
}
