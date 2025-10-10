// --------------------------------------------------------------
// File Name: UsageAnalyticsService.cs
// Author: Denuwan Sathsara
// Description: Service for retrieving usage analytics data.
// Created/Updated On: 27/09/2025
// --------------------------------------------------------------
using System;
using MongoDB.Driver;
using EvBackend.Entities;
using EvBackend.Models.DTOs;
using System.Linq;
using EvBackend.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;
namespace EvBackend.Services;


public class UsageAnalyticsService : IUsageAnalyticsService
{
    private readonly IMongoCollection<Booking> _bookings;
    private readonly IMongoCollection<Station> _stations;

    public UsageAnalyticsService(IMongoDatabase database)
    {
        _bookings = database.GetCollection<Booking>("Bookings");
        _stations = database.GetCollection<Station>("Stations");
    }

    public Task<IActionResult> GetUsageAnalytics()
    {
        throw new NotImplementedException();
    }

    public async Task<UsageAnalyticsDto> GetUsageAnalyticsAsync()
    {
        // Get all bookings and group by StationId and TimeSlotId
        var stationAggregation = await _bookings.Aggregate()
            .Group(b => b.StationId, g => new { StationId = g.Key, Count = g.Count() })
            .SortByDescending(x => x.Count)
            .Limit(1)
            .ToListAsync();

        var timeSlotAggregation = await _bookings.Aggregate()
            .Group(b => b.TimeSlotId, g => new { TimeSlotId = g.Key, Count = g.Count() })
            .SortByDescending(x => x.Count)
            .Limit(1)
            .ToListAsync();

        // Fetch most popular station details
        var mostPopularStation = await _stations.Find(s => s.StationId == stationAggregation.First().StationId)
            .FirstOrDefaultAsync();

        // Fetch most popular time slot details
        var mostPopularTimeSlot = await _bookings.Find(b => b.TimeSlotId == timeSlotAggregation.First().TimeSlotId)
            .FirstOrDefaultAsync();

        return new UsageAnalyticsDto
        {
            MostPopularStation = mostPopularStation?.Name,
            MostPopularStationCount = stationAggregation.First().Count,
            MostPopularTimeSlotStartTime = mostPopularTimeSlot?.StartTime,
            MostPopularTimeSlotCount = timeSlotAggregation.First().Count
        };
    }
}