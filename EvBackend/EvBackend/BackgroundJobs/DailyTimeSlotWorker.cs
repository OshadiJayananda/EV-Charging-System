using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using System;
using System.Threading;
using System.Threading.Tasks;
using EvBackend.Services;

namespace EvBackend.BackgroundJobs
{
    public class DailyTimeSlotWorker : BackgroundService
    {
        private readonly IServiceProvider _services;
        private readonly ILogger<DailyTimeSlotWorker> _logger;

        public DailyTimeSlotWorker(IServiceProvider services, ILogger<DailyTimeSlotWorker> logger)
        {
            _services = services;
            _logger = logger;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            while (!stoppingToken.IsCancellationRequested)
            {
                try
                {
                    var now = DateTime.Now;
                    var nextRun = DateTime.Today.AddDays(1);
                    var delay = nextRun - now;

                    _logger.LogInformation("⏳ Next daily timeslot maintenance scheduled at {NextRun}", nextRun);

                    // Wait until next midnight
                    await Task.Delay(delay, stoppingToken);

                    using (var scope = _services.CreateScope())
                    {
                        var scheduler = scope.ServiceProvider.GetRequiredService<TimeSlotSchedulerService>();
                        await scheduler.CleanupAndGenerateNextDayAsync();
                    }

                    _logger.LogInformation("✅ TimeSlot cleanup and generation completed at {Time}", DateTime.Now);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "❌ Error in DailyTimeSlotWorker");
                }
            }
        }
    }
}
