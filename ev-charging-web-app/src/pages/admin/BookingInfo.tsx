import {
  CheckCircleIcon,
  ClockIcon,
  UserIcon,
  TicketIcon,
  BuildingLibraryIcon,
  MapPinIcon,
  CalendarIcon,
  Squares2X2Icon,
} from "@heroicons/react/24/outline";

interface BookingInfoProps {
  booking: any;
}

const BookingInfo = ({ booking }: BookingInfoProps) => {
  const statusColors: Record<string, string> = {
    Pending: "text-yellow-600 bg-yellow-100",
    Approved: "text-green-600 bg-green-100",
    Completed: "text-blue-600 bg-blue-100",
    Cancelled: "text-red-600 bg-red-100",
  };

  const formatTime = (isoString?: string) => {
    if (!isoString) return "N/A";
    const date = new Date(isoString);
    return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  };

  return (
    <div className="bg-white shadow-md rounded-xl p-6 hover:shadow-xl transition-all duration-300">
      <h3 className="text-xl font-semibold text-gray-800 mb-6">
        Booking Information
      </h3>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <InfoRow
          label="Booking ID"
          value={booking.bookingId}
          icon={<TicketIcon className="w-5 h-5 text-gray-400" />}
        />
        <InfoRow
          label="Station Name"
          value={booking.stationName}
          icon={<BuildingLibraryIcon className="w-5 h-5 text-gray-400" />}
        />
        <InfoRow
          label="Station Location"
          value={booking.stationLocation}
          icon={<MapPinIcon className="w-5 h-5 text-gray-400" />}
        />
        <InfoRow
          label="Slot Name"
          value={booking.slotName || booking.timeSlotRange}
          icon={<Squares2X2Icon className="w-5 h-5 text-gray-400" />}
        />
        <InfoRow
          label="Owner"
          value={booking.ownerName}
          icon={<UserIcon className="w-5 h-5 text-gray-400" />}
        />
        <InfoRow
          label="Booking Status"
          value={booking.status}
          icon={<CheckCircleIcon className="w-5 h-5 text-gray-400" />}
          badge
          badgeColor={
            statusColors[booking.status] || "text-gray-600 bg-gray-100"
          }
        />
        <InfoRow
          label="Booking Date"
          value={booking.formattedDate}
          icon={<CalendarIcon className="w-5 h-5 text-gray-400" />}
        />
        <InfoRow
          label="Time Slot"
          icon={<ClockIcon className="w-5 h-5 text-gray-400" />}
          value={`${formatTime(booking.startTime)} - ${formatTime(
            booking.endTime
          )}`}
        />
        {booking.cancellationReason && (
          <InfoRow
            label="Cancellation Reason"
            value={booking.cancellationReason}
          />
        )}
      </div>
    </div>
  );
};

interface InfoRowProps {
  label: string;
  value?: string;
  icon?: React.ReactNode;
  badge?: boolean;
  badgeColor?: string;
}

const InfoRow = ({
  label,
  value,
  icon,
  badge = false,
  badgeColor,
}: InfoRowProps) => (
  <div className="flex justify-between items-center">
    <div className="flex items-center gap-2">
      {icon && <span>{icon}</span>}
      <p className="text-sm text-gray-600">{label}:</p>
    </div>
    {badge ? (
      <span
        className={`px-3 py-1 rounded-full text-xs font-semibold ${badgeColor}`}
      >
        {value || "N/A"}
      </span>
    ) : (
      <p className="text-sm font-semibold">{value || "N/A"}</p>
    )}
  </div>
);

export default BookingInfo;
