import { useState, useEffect } from "react";
import { getRequest } from "../../components/common/api";
import { useNavigate } from "react-router-dom";

function BookingManagementPage() {
  const navigate = useNavigate();

  const [pendingBookings, setPendingBookings] = useState<any>([]);
  const [approvedBookings, setApprovedBookings] = useState<any>([]);
  const [completedBookings, setCompletedBookings] = useState<any>([]);
  const [loading, setLoading] = useState(true); // Loading state

  const fetchBookings = async () => {
    setLoading(true);
    try {
      const pendingResponse = await getRequest("/bookings/pending");
      if (pendingResponse) setPendingBookings(pendingResponse.data);

      const approvedResponse = await getRequest("/bookings/approved");
      if (approvedResponse) setApprovedBookings(approvedResponse.data);

      const completedResponse = await getRequest("/bookings/completed");
      if (completedResponse) setCompletedBookings(completedResponse.data);
    } catch (error) {
      console.error("Error fetching bookings:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  const handleApproveBooking = async (bookingId: string) => {
    try {
      const response = await fetch(`/api/bookings/${bookingId}/approve`, {
        method: "PATCH",
      });
      if (response.ok) {
        fetchBookings();
      }
    } catch (error) {
      console.error("Error approving booking:", error);
    }
  };

  const handleCancelBooking = async (bookingId: string) => {
    try {
      const response = await fetch(`/api/bookings/${bookingId}/cancel`, {
        method: "PATCH",
      });
      if (response.ok) {
        fetchBookings();
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
      <h2 className="text-3xl font-semibold text-gray-800">
        Booking Management
      </h2>

      {loading ? (
        <LoadingSpinner />
      ) : (
        <>
          {/* Pending Bookings Section */}
          <div className="bg-white shadow-lg rounded-lg p-6 hover:shadow-xl transition-all">
            <h3 className="text-xl font-semibold text-gray-800 mb-4">
              Pending Bookings
            </h3>
            <div className="flex justify-start mb-4">
              <button
                onClick={() => navigate("/admin/bookings/pending")}
                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-all"
              >
                View All Pending Bookings
              </button>
            </div>
            <div className="space-y-4">
              {pendingBookings.length === 0 ? (
                <p className="text-gray-500">No pending bookings.</p>
              ) : (
                pendingBookings.map((booking) => (
                  <div
                    key={booking.bookingId}
                    className="flex justify-between items-center bg-gray-50 p-4 rounded-lg shadow-sm hover:shadow-md transition"
                  >
                    <p className="text-sm text-gray-700">{booking.bookingId}</p>
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
                      <button
                        onClick={() =>
                          navigate(`/admin/bookings/${booking.bookingId}`)
                        }
                        className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-all"
                      >
                        View Details
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Approved Bookings Section */}
          <div className="bg-white shadow-lg rounded-lg p-6 hover:shadow-xl transition-all mt-6">
            <h3 className="text-xl font-semibold text-gray-800 mb-4">
              Approved Bookings
            </h3>
            <p className="text-gray-600 mb-4">View approved bookings</p>
            <div className="space-y-4">
              {approvedBookings.length === 0 ? (
                <p className="text-gray-500">No approved bookings.</p>
              ) : (
                approvedBookings.map((booking) => (
                  <div
                    key={booking.bookingId}
                    className="flex justify-between items-center bg-gray-50 p-4 rounded-lg shadow-sm hover:shadow-md transition"
                  >
                    <p className="text-sm text-gray-700">{booking.bookingId}</p>
                    <button
                      onClick={() =>
                        navigate(`/admin/bookings/${booking.bookingId}`)
                      }
                      className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-all"
                    >
                      View Details
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Completed Bookings Section */}
          <div className="bg-white shadow-lg rounded-lg p-6 hover:shadow-xl transition-all mt-6">
            <h3 className="text-xl font-semibold text-gray-800 mb-4">
              Completed Bookings
            </h3>
            <p className="text-gray-600 mb-4">View completed bookings</p>
            <div className="space-y-4">
              {completedBookings.length === 0 ? (
                <p className="text-gray-500">No completed bookings.</p>
              ) : (
                completedBookings.map((booking) => (
                  <div
                    key={booking.bookingId}
                    className="flex justify-between items-center bg-gray-50 p-4 rounded-lg shadow-sm hover:shadow-md transition"
                  >
                    <p className="text-sm text-gray-700">{booking.bookingId}</p>
                    <button
                      onClick={() =>
                        navigate(`/admin/bookings/${booking.bookingId}`)
                      }
                      className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-all"
                    >
                      View Details
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}

export default BookingManagementPage;
