import type { useNavigate } from "react-router-dom";

interface BookingActionsProps {
  booking: any;
  navigate: ReturnType<typeof useNavigate>;
}

const BookingActions = ({ booking, navigate }: BookingActionsProps) => (
  <div className="bg-white shadow-lg rounded-lg p-6 hover:shadow-xl transition-all mt-6">
    <h3 className="text-xl font-semibold text-gray-800 mb-4">Actions</h3>
    <div className="space-y-4">
      {booking.status === "Pending" && (
        <div className="flex space-x-4">
          <button
            onClick={() =>
              navigate(`/admin/bookings/${booking.bookingId}/approve`)
            }
            className="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700 transition-all"
          >
            Approve Booking
          </button>
          <button
            onClick={() =>
              navigate(`/admin/bookings/${booking.bookingId}/cancel`)
            }
            className="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 transition-all"
          >
            Cancel Booking
          </button>
        </div>
      )}
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
);

export default BookingActions;
