import { useEffect, useState } from "react";
import { getRequest, patchRequest } from "../../components/common/api";
import { useParams, useNavigate } from "react-router-dom";
import Loading from "../../components/common/Loading";
import toast from "react-hot-toast";

interface Booking {
  bookingId: string;
  ownerId: string;
  ownerName?: string;
  stationId: string;
  stationName?: string;
  slotId: string;
  slotNumber: number;
  connectorType: string;
  startTime: string;
  endTime: string;
  status: string;
  createdAt: string;
}

const OperatorBookings = () => {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [view, setView] = useState<"today" | "upcoming">("today");
  const { stationId } = useParams();
  const navigate = useNavigate();

  const fetchBookings = async () => {
    if (!stationId) return;

    setLoading(true);
    const endpoint =
      view === "today"
        ? `/bookings/station/${stationId}/today`
        : `/bookings/station/${stationId}/upcoming`;

    const res = await getRequest<Booking[]>(endpoint);
    if (res) {
      setBookings(res.data);
    }
    setLoading(false);
  };

  const handleApprove = async (bookingId: string) => {
    setLoading(true);
    const res = await patchRequest(`/bookings/${bookingId}/approve`);
    if (res) {
      toast.success("Booking approved successfully");
      await fetchBookings();
    }
    setLoading(false);
  };

  const handleStartCharging = async (bookingId: string) => {
    setLoading(true);
    const res = await patchRequest(`/bookings/${bookingId}/start`);
    if (res) {
      toast.success("Charging started");
      await fetchBookings();
    }
    setLoading(false);
  };

  const handleFinalize = async (bookingId: string) => {
    setLoading(true);
    const res = await patchRequest(`/bookings/${bookingId}/finalize`);
    if (res) {
      toast.success("Booking finalized successfully");
      await fetchBookings();
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchBookings();
  }, [stationId, view]);

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case "pending":
        return "bg-yellow-100 text-yellow-800";
      case "approved":
        return "bg-blue-100 text-blue-800";
      case "charging":
        return "bg-green-100 text-green-800";
      case "finalized":
      case "completed":
        return "bg-gray-100 text-gray-800";
      case "canceled":
        return "bg-red-100 text-red-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <button
            onClick={() => navigate("/operator/stations")}
            className="text-sm text-gray-600 hover:text-gray-800 mb-2 flex items-center gap-1"
          >
            ← Back to Stations
          </button>
          <h2 className="text-2xl font-semibold text-gray-800">
            Station Bookings
          </h2>
        </div>
      </div>

      <div className="flex gap-2 mb-4">
        <button
          onClick={() => setView("today")}
          className={`px-4 py-2 rounded-md font-medium transition ${
            view === "today"
              ? "bg-green-600 text-white"
              : "bg-gray-200 text-gray-700 hover:bg-gray-300"
          }`}
        >
          Today's Bookings
        </button>
        <button
          onClick={() => setView("upcoming")}
          className={`px-4 py-2 rounded-md font-medium transition ${
            view === "upcoming"
              ? "bg-green-600 text-white"
              : "bg-gray-200 text-gray-700 hover:bg-gray-300"
          }`}
        >
          Upcoming Bookings
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center items-center h-64">
          <Loading size="lg" color="green" text="Loading bookings..." />
        </div>
      ) : (
        <div className="overflow-x-auto bg-white shadow rounded-lg">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-100">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                  Booking ID
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                  Owner
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                  Slot #
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                  Connector
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                  Start Time
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                  End Time
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                  Status
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {bookings.map((booking) => (
                <tr key={booking.bookingId} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm text-gray-800 font-mono">
                    {booking.bookingId.substring(0, 8)}...
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {booking.ownerName || booking.ownerId}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {booking.slotNumber}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {booking.connectorType}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {formatDateTime(booking.startTime)}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {formatDateTime(booking.endTime)}
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <span
                      className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(
                        booking.status
                      )}`}
                    >
                      {booking.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <div className="flex gap-2">
                      {booking.status.toLowerCase() === "pending" && (
                        <button
                          onClick={() => handleApprove(booking.bookingId)}
                          className="px-3 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition text-xs"
                        >
                          Approve
                        </button>
                      )}
                      {booking.status.toLowerCase() === "approved" && (
                        <button
                          onClick={() => handleStartCharging(booking.bookingId)}
                          className="px-3 py-1 bg-green-600 text-white rounded-md hover:bg-green-700 transition text-xs"
                        >
                          Start Charging
                        </button>
                      )}
                      {booking.status.toLowerCase() === "charging" && (
                        <button
                          onClick={() => handleFinalize(booking.bookingId)}
                          className="px-3 py-1 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition text-xs"
                        >
                          Finalize
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {bookings.length === 0 && (
                <tr>
                  <td
                    colSpan={8}
                    className="px-4 py-6 text-center text-gray-500 text-sm"
                  >
                    No {view} bookings found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default OperatorBookings;
