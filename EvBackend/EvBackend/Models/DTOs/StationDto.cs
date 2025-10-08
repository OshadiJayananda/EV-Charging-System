// --------------------------------------------------------------
// File Name: StationDto.cs
// Author: Denuwan Sathsara
// Description: DTOs for station management. Cleaned to separate creation,
// updates, and response DTOs.
// Created/Updated On: 27/09/2025
// --------------------------------------------------------------

using System;

namespace EvBackend.Models.DTOs;

public class StationDto
{
    public string StationId { get; set; }
    public string Name { get; set; }
    public string Location { get; set; }
    public double Latitude { get; set; }
    public double Longitude { get; set; }
    public string Type { get; set; }
    public int Capacity { get; set; }
    public int AvailableSlots { get; set; }
    public bool IsActive { get; set; }
    public List<SlotDto> Slots { get; set; } = new();
}

public class CreateStationDto
{
    public string Name { get; set; }
    public string Location { get; set; }
    public string Type { get; set; }
    public int Capacity { get; set; }

}

public class UpdateStationDto
{
    public string Name { get; set; }
    public string Location { get; set; }
    public string Type { get; set; }
    public int Capacity { get; set; }
    public int AvailableSlots { get; set; }

    // Optional per-slot updates
    public List<SlotUpdateDto> SlotUpdates { get; set; } = new();
}

public class StationNameDto
{
    public string StationId { get; set; }
    public string Name { get; set; }
    public string Location { get; set; }
    public double Latitude { get; set; }
    public double Longitude { get; set; }
}

