import { useState, useEffect } from "react";
import { getRequest } from "../../components/common/api";
import { useNavigate } from "react-router-dom";

function PendingBookingsPage() {
  const navigate = useNavigate();

  // State to store the pending bookings data
  const [pendingBookings, setPendingBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  // Fetch pending bookings
  const fetchPendingBookings = async () => {
    setLoading(true);
    try {
      const response = await getRequest("/bookings/pending");
      if (response) setPendingBookings(response.data);
    } catch (error) {
      console.error("Error fetching pending bookings:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPendingBookings();
  }, []);

  // Function to handle approval of a booking
  const handleApproveBooking = async (bookingId: string) => {
    try {
      const response = await fetch(`/api/bookings/${bookingId}/approve`, {
        method: "PATCH",
      });
      if (response.ok) {
        fetchPendingBookings(); // Refetch pending bookings after approval
      }
    } catch (error) {
      console.error("Error approving booking:", error);
    }
  };

  // Function to handle cancellation of a booking
  const handleCancelBooking = async (bookingId: string) => {
    try {
      const response = await fetch(`/api/bookings/${bookingId}/cancel`, {
        method: "PATCH",
      });
      if (response.ok) {
        fetchPendingBookings(); // Refetch pending bookings after cancellation
      }
    } catch (error) {
      console.error("Error cancelling booking:", error);
    }
  };

  // Loading Spinner
  const LoadingSpinner = () => (
    <div className="flex justify-center items-center py-10">
      <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-blue-600 border-solid"></div>
    </div>
  );

  return (
    <div className="p-6 space-y-6">
      <h2 className="text-3xl font-semibold text-gray-800">Pending Bookings</h2>

      {loading ? (
        <LoadingSpinner /> // Show loading spinner while fetching data
      ) : (
        <div className="space-y-6">
          {pendingBookings.length === 0 ? (
            <p className="text-gray-500">No pending bookings available.</p>
          ) : (
            pendingBookings.map((booking) => (
              <div
                key={booking.bookingId}
                className="bg-white shadow-lg rounded-lg p-6 hover:shadow-xl transition-all"
              >
                <div className="flex justify-between items-center mb-4">
                  <p className="text-lg font-semibold text-gray-700">
                    {booking.bookingId}
                  </p>
                  <div className="flex space-x-4">
                    <button
                      onClick={() => handleApproveBooking(booking.bookingId)}
                      className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-all"
                    >
                      Approve
                    </button>
                    <button
                      onClick={() => handleCancelBooking(booking.bookingId)}
                      className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-all"
                    >
                      Cancel
                    </button>
                  </div>
                </div>

                <div className="space-y-2">
                  <div className="flex justify-between">
                    <p className="text-sm text-gray-600">Station:</p>
                    <p className="text-sm font-semibold">
                      {booking.stationName}
                    </p>
                  </div>
                  <div className="flex justify-between">
                    <p className="text-sm text-gray-600">Time Slot:</p>
                    <p className="text-sm font-semibold">{booking.timeSlot}</p>
                  </div>
                  <div className="flex justify-between">
                    <p className="text-sm text-gray-600">Owner:</p>
                    <p className="text-sm font-semibold">{booking.ownerName}</p>
                  </div>
                </div>

                <button
                  onClick={() =>
                    navigate(`/admin/bookings/${booking.bookingId}`)
                  }
                  className="mt-4 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-all"
                >
                  View Details
                </button>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export default PendingBookingsPage;
