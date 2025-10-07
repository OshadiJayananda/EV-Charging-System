// --------------------------------------------------------------
// File Name: PagedResultDto.cs
// Author: Oshadi Jayananda
// Description: Implements a generic paged result DTO for paginated responses
// Created On: 07/10/2025
// --------------------------------------------------------------


namespace EvBackend.Models.DTOs
{
    public class PagedResultDto<T>
    {
        public IEnumerable<T> Items { get; set; }
        public long TotalCount { get; set; }
    }
}
