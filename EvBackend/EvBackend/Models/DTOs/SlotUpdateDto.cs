using System;

namespace EvBackend.Models.DTOs;

public enum SlotAction { Update, Remove, Add }

public class SlotUpdateDto
{
    public string SlotId { get; set; }
    public string ConnectorType { get; set; }
    public string Status { get; set; }
    public SlotAction Action { get; set; }
}

