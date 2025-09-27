// --------------------------------------------------------------
// File Name: UpdateEVOwnerDto.cs
// Author: Oshadi Jayananda
// Description: Data Transfer Object used for updating a electric vehicle owner
// Created On: 20/09/2025
// --------------------------------------------------------------

using System.ComponentModel.DataAnnotations;

namespace EvBackend.Models.DTOs
{
    public class UpdateEVOwnerDto
    {
        [Required] public string FullName { get; set; }
        [Required, EmailAddress] public string Email { get; set; }
        // No NIC here (cannot change PK)
    }

}
