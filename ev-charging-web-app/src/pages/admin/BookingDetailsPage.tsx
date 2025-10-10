import { useState, useEffect } from "react";
import { getRequest } from "../../components/common/api";
import { useNavigate, useParams } from "react-router-dom";

function BookingDetailsPage() {
  const { bookingId } = useParams(); // Get bookingId from URL parameters
  const navigate = useNavigate();

  // State to store the booking details
  const [bookingDetails, setBookingDetails] = useState(null);
  const [loading, setLoading] = useState(true);

  // Fetch booking details
  const fetchBookingDetails = async () => {
    setLoading(true);
    try {
      const response = await getRequest(`/bookings/${bookingId}`);
      if (response) setBookingDetails(response.data);
    } catch (error) {
      console.error("Error fetching booking details:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (bookingId) {
      fetchBookingDetails();
    }
  }, [bookingId]);

  // Loading Spinner
  const LoadingSpinner = () => (
    <div className="flex justify-center items-center py-10">
      <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-blue-600 border-solid"></div>
    </div>
  );

  return (
    <div className="p-6 space-y-6">
      <h2 className="text-3xl font-semibold text-gray-800">Booking Details</h2>

      {loading ? (
        <LoadingSpinner />
      ) : bookingDetails ? (
        <div className="space-y-6">
          {/* Booking Information */}
          <div className="bg-white shadow-lg rounded-lg p-6 hover:shadow-xl transition-all">
            <h3 className="text-xl font-semibold text-gray-800 mb-4">
              Booking Information
            </h3>
            <div className="space-y-4">
              <div className="flex justify-between">
                <p className="text-sm text-gray-600">Booking ID:</p>
                <p className="text-sm font-semibold">
                  {bookingDetails.bookingId}
                </p>
              </div>
              <div className="flex justify-between">
                <p className="text-sm text-gray-600">Station Name:</p>
                <p className="text-sm font-semibold">
                  {bookingDetails.stationName}
                </p>
              </div>
              <div className="flex justify-between">
                <p className="text-sm text-gray-600">Time Slot:</p>
                <p className="text-sm font-semibold">
                  {bookingDetails.timeSlot}
                </p>
              </div>
              <div className="flex justify-between">
                <p className="text-sm text-gray-600">Owner:</p>
                <p className="text-sm font-semibold">
                  {bookingDetails.ownerName}
                </p>
              </div>
              <div className="flex justify-between">
                <p className="text-sm text-gray-600">Booking Status:</p>
                <p className="text-sm font-semibold">{bookingDetails.status}</p>
              </div>
            </div>
          </div>

          {/* Actions Section */}
          <div className="bg-white shadow-lg rounded-lg p-6 hover:shadow-xl transition-all mt-6">
            <h3 className="text-xl font-semibold text-gray-800 mb-4">
              Actions
            </h3>
            <div className="space-y-4">
              {/* Approve or Cancel Actions */}
              {bookingDetails.status === "Pending" && (
                <div className="flex space-x-4">
                  <button
                    onClick={() =>
                      navigate(
                        `/admin/bookings/${bookingDetails.bookingId}/approve`
                      )
                    }
                    className="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700 transition-all"
                  >
                    Approve Booking
                  </button>
                  <button
                    onClick={() =>
                      navigate(
                        `/admin/bookings/${bookingDetails.bookingId}/cancel`
                      )
                    }
                    className="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 transition-all"
                  >
                    Cancel Booking
                  </button>
                </div>
              )}
              {/* Option to navigate back */}
              <div className="flex justify-start">
                <button
                  onClick={() => navigate("/admin/bookings/pending")}
                  className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-all"
                >
                  Back to Pending Bookings
                </button>
              </div>
            </div>
          </div>
        </div>
      ) : (
        <p className="text-gray-500">No details available for this booking.</p>
      )}
    </div>
  );
}

export default BookingDetailsPage;
