import { useState, useEffect } from "react";
import { getRequest } from "../../components/common/api";
import { useNavigate, useParams } from "react-router-dom";
import BookingInfo from "./BookingInfo";
import BookingActions from "./BookingActions";

function BookingDetailsPage() {
  const { bookingId } = useParams(); // Get bookingId from URL parameters
  const navigate = useNavigate();

  // State to store the booking details
  const [bookingDetails, setBookingDetails] = useState<any | null>(null);
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

  let content;
  if (loading) {
    content = <LoadingSpinner />;
  } else if (bookingDetails) {
    content = (
      <>
        <BookingInfo booking={bookingDetails} />
        <BookingActions booking={bookingDetails} navigate={navigate} />
      </>
    );
  } else {
    content = (
      <p className="text-gray-500">No details available for this booking.</p>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <h2 className="text-3xl font-semibold text-gray-800">Booking Details</h2>
      {content}
    </div>
  );
}

export default BookingDetailsPage;
