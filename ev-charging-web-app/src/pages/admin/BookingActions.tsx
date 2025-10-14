import { useState } from "react";
import type { useNavigate } from "react-router-dom";
import { patchRequest } from "../../components/common/api";
import ConfirmModal from "../../components/common/ConfirmModal";

interface BookingActionsProps {
  booking: any;
  navigate: ReturnType<typeof useNavigate>;
}

const BookingActions = ({ booking, navigate }: BookingActionsProps) => {
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [actionType, setActionType] = useState<"approve" | "cancel" | null>(
    null
  );
  const [loading, setLoading] = useState(false);

  const handleAction = async () => {
    if (!actionType) return;
    setLoading(true);
    try {
      await patchRequest(`/bookings/${booking.bookingId}/${actionType}`, {});
      navigate("/admin/bookings");
    } catch (error) {
      console.error(`Error ${actionType} booking:`, error);
    } finally {
      setLoading(false);
      setShowConfirmModal(false);
    }
  };

  const openModal = (type: "approve" | "cancel") => {
    setActionType(type);
    setShowConfirmModal(true);
  };

  return (
    <>
      <div className="bg-white shadow-lg rounded-lg p-6 hover:shadow-xl transition-all mt-6">
        <h3 className="text-xl font-semibold text-gray-800 mb-4">Actions</h3>
        <div className="space-y-4">
          {booking.status === "Pending" && (
            <div className="flex space-x-4">
              <button
                onClick={() => openModal("approve")}
                className="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700 transition-all"
              >
                Approve Booking
              </button>
              <button
                onClick={() => openModal("cancel")}
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

      {showConfirmModal && actionType && (
        <ConfirmModal
          title={
            actionType === "approve"
              ? "Confirm Approval"
              : "Confirm Cancellation"
          }
          message={
            actionType === "approve"
              ? "Are you sure you want to approve this booking?"
              : "Are you sure you want to cancel this booking?"
          }
          confirmText={
            actionType === "approve" ? "Yes, Approve" : "Yes, Cancel"
          }
          confirmColor={actionType === "approve" ? "green" : "red"}
          onConfirm={handleAction}
          onCancel={() => setShowConfirmModal(false)}
          loading={loading}
        />
      )}
    </>
  );
};

export default BookingActions;
