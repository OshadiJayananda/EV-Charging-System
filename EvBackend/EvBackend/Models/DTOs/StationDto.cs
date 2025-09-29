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
}
