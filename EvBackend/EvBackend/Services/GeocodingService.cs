using System;
using System.Text.Json;

namespace EvBackend.Services;


public class GeocodingService
{
    private readonly HttpClient _httpClient;
    private readonly string _apiKey;

    public GeocodingService(HttpClient httpClient, IConfiguration config)
    {
        _httpClient = httpClient;
        _apiKey = config["GoogleMaps:ApiKey"];
    }

    public async Task<(double lat, double lng)?> GetCoordinatesAsync(string address)
    {
        var url = $"https://maps.googleapis.com/maps/api/geocode/json?address={Uri.EscapeDataString(address)}&key={_apiKey}";
        var response = await _httpClient.GetStringAsync(url);

        using var doc = JsonDocument.Parse(response);
        var root = doc.RootElement;

        if (root.GetProperty("status").GetString() != "OK")
            return null;

        var location = root
            .GetProperty("results")[0]
            .GetProperty("geometry")
            .GetProperty("location");

        double lat = location.GetProperty("lat").GetDouble();
        double lng = location.GetProperty("lng").GetDouble();

        return (lat, lng);
    }
}
