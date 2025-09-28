using Xunit;
using Moq;
using EvBackend.Controllers;
using EvBackend.Services.Interfaces;
using EvBackend.Models.DTOs;
using Microsoft.AspNetCore.Mvc;
using System.Threading.Tasks;
using System.Security.Claims;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Logging;

namespace EvBackend.Tests.Unit
{
    public class UsersControllerTests
    {
        private Mock<IUserService> mockUserService;
        private Mock<IEVOwnerService> mockEvOwnerService;
        private UsersController controller;
        private readonly Mock<ILogger<UsersController>> mockLogger;


        public UsersControllerTests()
        {
            mockUserService = new Mock<IUserService>();
            mockEvOwnerService = new Mock<IEVOwnerService>();
            mockLogger = new Mock<ILogger<UsersController>>();

            controller = new UsersController(mockUserService.Object, mockEvOwnerService.Object, mockLogger.Object);
        }

        [Fact]
        public async Task GetUserById_ReturnsOk_WhenUserExists()
        {
            // Arrange
            var expectedUser = new UserDto
            {
                Id = "651234abcd5678ef90123456",
                FullName = "Hasindu",
                Email = "hasindu@example.com",
                Role = "Admin",
                IsActive = true
            };

            mockUserService.Setup(s => s.GetUserById(expectedUser.Id))
                           .ReturnsAsync(expectedUser);

            // Act
            var result = await controller.GetUserById(expectedUser.Id);

            // Assert
            var okResult = Assert.IsType<OkObjectResult>(result);
            var returnValue = Assert.IsType<UserDto>(okResult.Value);
            Assert.Equal("Hasindu", returnValue.FullName);
        }

        [Fact]
        public async Task GetUserById_ReturnsBadRequest_ForInvalidId()
        {
            // Arrange
            string invalidId = "123-invalid-id";

            // Act
            var result = await controller.GetUserById(invalidId);

            // Assert
            var badRequestResult = Assert.IsType<BadRequestObjectResult>(result);

            var messageProperty = badRequestResult.Value?.GetType().GetProperty("message");
            Assert.NotNull(messageProperty);
            var message = messageProperty.GetValue(badRequestResult.Value)?.ToString();
            Assert.Equal("Invalid user ID", message);
        }

        [Fact]
        public async Task GetUserById_ReturnsNotFound_WhenUserDoesNotExist()
        {
            // Arrange
            string validId = "651234abcd5678ef90123456";
            mockUserService.Setup(s => s.GetUserById(validId))
                           .ReturnsAsync((UserDto?)null);

            // Act
            var result = await controller.GetUserById(validId);

            // Assert
            var notFoundResult = Assert.IsType<NotFoundObjectResult>(result);

            var messageProperty = notFoundResult.Value?.GetType().GetProperty("message");
            Assert.NotNull(messageProperty);
            var message = messageProperty.GetValue(notFoundResult.Value)?.ToString();
            Assert.Equal("User not found", message);
        }

        [Fact]
        public async Task CreateUser_ReturnsCreated_WhenUserIsValid()
        {
            // Arrange
            var createUserDto = new CreateUserDto
            {
                FullName = "Hasindu",
                Email = "hasindu@example.com",
                Role = "Admin",
                Password = "Password123!"
            };

            var createdUser = new UserDto
            {
                Id = "651234abcd5678ef90123456",
                FullName = createUserDto.FullName,
                Email = createUserDto.Email,
                Role = createUserDto.Role,
                IsActive = true
            };

            mockUserService.Setup(s => s.CreateUser(createUserDto))
                           .ReturnsAsync(createdUser);

            // Act
            var result = await controller.CreateUser(createUserDto);

            // Assert
            var createdResult = Assert.IsType<CreatedAtActionResult>(result);
            Assert.Equal(nameof(UsersController.GetUserById), createdResult.ActionName);
            var returnedUser = Assert.IsType<UserDto>(createdResult.Value);
            Assert.Equal("Hasindu", returnedUser.FullName);
            Assert.Equal("hasindu@example.com", returnedUser.Email);
        }

        [Fact]
        public async Task CreateUser_ReturnsConflict_WhenEmailAlreadyExists()
        {
            // Arrange
            var createUserDto = new CreateUserDto
            {
                FullName = "Hasindu",
                Email = "hasindu@example.com",
                Role = "Admin",
                Password = "Password123!"
            };

            // Mock CreateUser to throw ArgumentException for duplicate email
            mockUserService.Setup(s => s.CreateUser(createUserDto))
                           .ThrowsAsync(new ArgumentException("Email already exists"));

            // Act
            var result = await controller.CreateUser(createUserDto);

            // Assert
            var conflictResult = Assert.IsType<ConflictObjectResult>(result);

            var messageProperty = conflictResult.Value?.GetType().GetProperty("message");
            Assert.NotNull(messageProperty);
            var message = messageProperty.GetValue(conflictResult.Value)?.ToString();

            Assert.Equal("Email already exists", message);
        }

        [Fact]
        public async Task ActivateUser_ReturnsNoContent_WhenUserIsSuccessfullyActivated()
        {
            // Arrange
            string userId = "651234abcd5678ef90123456";

            var userDto = new UserDto
            {
                Id = userId,
                FullName = "Hasindu",
                Email = "hasindu@example.com",
                Role = "Operator",
                IsActive = false
            };

            // Mock GetUserById to return a deactivated user
            mockUserService.Setup(s => s.GetUserById(userId))
                           .ReturnsAsync(userDto);

            // Mock ChangeUserStatus to succeed
            mockUserService.Setup(s => s.ChangeUserStatus(userId, true))
                           .ReturnsAsync(true);

            // Act
            var result = await controller.ActivateUser(userId);

            // Assert
            Assert.IsType<NoContentResult>(result);
        }

        [Fact]
        public async Task ActivateUser_ReturnsBadRequest_WhenUserIsAlreadyActive()
        {
            // Arrange
            string userId = "651234abcd5678ef90123456";

            var userDto = new UserDto
            {
                Id = userId,
                FullName = "Hasindu",
                Email = "hasindu@example.com",
                Role = "Operator",
                IsActive = true // already active
            };

            // Mock GetUserById to return an active user
            mockUserService.Setup(s => s.GetUserById(userId))
                           .ReturnsAsync(userDto);

            // Act
            var result = await controller.ActivateUser(userId);

            // Assert
            var badRequestResult = Assert.IsType<BadRequestObjectResult>(result);

            var messageProperty = badRequestResult.Value?.GetType().GetProperty("message");
            Assert.NotNull(messageProperty);
            var message = messageProperty.GetValue(badRequestResult.Value)?.ToString();

            Assert.Equal("User is already active", message);
        }

        [Fact]
        public async Task ActivateUser_ReturnsNotFound_WhenUserDoesNotExist()
        {
            // Arrange
            string userId = "651234abcd5678ef90123456";

            // Mock GetUserById to return null (user not found)
            mockUserService.Setup(s => s.GetUserById(userId))
                           .ReturnsAsync((UserDto?)null);

            // Mock the user claims (optional, depends on your controller setup)
            var userEmail = "test@example.com";
            var userClaims = new ClaimsPrincipal(new ClaimsIdentity(new Claim[]
            {
                new Claim(ClaimTypes.Email, userEmail)
            }, "mock"));

            controller.ControllerContext = new ControllerContext
            {
                HttpContext = new DefaultHttpContext { User = userClaims }
            };

            // Act
            var result = await controller.DeactivateUser(userId);

            // Assert
            Assert.IsType<ForbidResult>(result);
        }

        [Fact]
        public async Task DeactivateUser_ReturnsNoContent_WhenUserIsSuccessfullyDeactivated()
        {
            // Arrange
            string userId = "651234abcd5678ef90123456";

            // Mock user data
            var user = new UserDto
            {
                Id = userId,
                Email = "test@example.com",
                IsActive = true // user is currently active
            };

            // Mock GetUserById to return the user
            mockUserService.Setup(s => s.GetUserById(userId))
                           .ReturnsAsync(user);

            // Mock ChangeUserStatus to succeed
            mockUserService.Setup(s => s.ChangeUserStatus(userId, false))
                           .ReturnsAsync(true);

            // Mock the user claims to match the user email
            var userClaims = new ClaimsPrincipal(new ClaimsIdentity(new Claim[]
            {
                new Claim(ClaimTypes.Email, "test@example.com")
            }, "mock"));

            controller.ControllerContext = new ControllerContext
            {
                HttpContext = new DefaultHttpContext { User = userClaims }
            };

            // Act
            var result = await controller.DeactivateUser(userId);

            // Assert
            // Use IsAssignableFrom for more robust type checking
            var noContentResult = Assert.IsAssignableFrom<NoContentResult>(result);
            Assert.Equal(204, noContentResult.StatusCode);
        }

        [Fact]
        public async Task DeactivateUser_ReturnsBadRequest_WhenUserIsAlreadyDeactivated()
        {
            // Arrange
            string userId = "651234abcd5678ef90123456";

            var userDto = new UserDto
            {
                Id = userId,
                FullName = "Hasindu",
                Email = "hasindu@example.com",
                Role = "Operator",
                IsActive = false // already deactivated
            };

            // Mock GetUserById to return a deactivated user
            mockUserService.Setup(s => s.GetUserById(userId))
                           .ReturnsAsync(userDto);

            // Mock the user claims to match the user email
            var userClaims = new ClaimsPrincipal(new ClaimsIdentity(new Claim[]
            {
                new Claim(ClaimTypes.Email, "hasindu@example.com")
            }, "mock"));

            controller.ControllerContext = new ControllerContext
            {
                HttpContext = new DefaultHttpContext { User = userClaims }
            };

            // Act
            var result = await controller.DeactivateUser(userId);

            // Assert
            var badRequestResult = Assert.IsAssignableFrom<BadRequestObjectResult>(result);
            Assert.Equal(400, badRequestResult.StatusCode);

            var messageProperty = badRequestResult.Value?.GetType().GetProperty("message");
            Assert.NotNull(messageProperty);
            var message = messageProperty.GetValue(badRequestResult.Value)?.ToString();
            Assert.Equal("User is already deactivated", message);
        }

        [Fact]
        public async Task DeactivateUser_ReturnsForbid_WhenUserDoesNotExistOrEmailMismatch()
        {
            // Arrange
            string userId = "651234abcd5678ef90123456";

            // Mock GetUserById to return null (user not found)
            mockUserService.Setup(s => s.GetUserById(userId))
                           .ReturnsAsync((UserDto?)null);

            // Mock the user claims
            var userClaims = new ClaimsPrincipal(new ClaimsIdentity(new Claim[]
            {
                new Claim(ClaimTypes.Email, "hasindu@example.com")
            }, "mock"));

            controller.ControllerContext = new ControllerContext
            {
                HttpContext = new DefaultHttpContext { User = userClaims }
            };

            // Act
            var result = await controller.DeactivateUser(userId);

            // Assert
            Assert.IsType<ForbidResult>(result);
        }
    }
}
