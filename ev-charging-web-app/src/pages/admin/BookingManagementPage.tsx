import { useState, useEffect } from "react";
import { getRequest, patchRequest } from "../../components/common/api";
import { useNavigate } from "react-router-dom";

// Types for better type safety
interface Booking {
  bookingId: string;
  customerName?: string;
  serviceType?: string;
  bookingDate?: string;
  status?: string;
  amount?: number;
}

function BookingManagementPage() {
  const navigate = useNavigate();
  const [pendingBookings, setPendingBookings] = useState<Booking[]>([]);
  const [approvedBookings, setApprovedBookings] = useState<Booking[]>([]);
  const [completedBookings, setCompletedBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<
    "pending" | "approved" | "completed"
  >("pending");

  const fetchBookings = async () => {
    setLoading(true);
    try {
      const [pendingResponse, approvedResponse, completedResponse] =
        await Promise.all([
          getRequest("/bookings/pending"),
          getRequest("/bookings/approved"),
          getRequest("/bookings/completed"),
        ]);

      if (pendingResponse) setPendingBookings(pendingResponse.data);
      if (approvedResponse) setApprovedBookings(approvedResponse.data);
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
      setActionLoading(bookingId);

      const response = await patchRequest(`/bookings/${bookingId}/approve`, {});

      if (!response || response.status !== 200) {
        console.error("Failed to approve booking:", response);
        throw new Error("Failed to approve booking");
      }

      await fetchBookings();
    } catch (error) {
      console.error("Error approving booking:", error);
    } finally {
      setActionLoading(null);
    }
  };

  const handleCancelBooking = async (bookingId: string) => {
    setActionLoading(bookingId);
    try {
      const response = await patchRequest(`/bookings/${bookingId}/cancel`, {});

      if (!response || response.status !== 200) {
        console.error("Failed to approve booking:", response);
        throw new Error("Failed to approve booking");
      }

      await fetchBookings();
    } catch (error) {
      console.error("Error cancelling booking:", error);
    } finally {
      setActionLoading(null);
    }
  };

  const getStatusBadge = (status: string) => {
    const statusConfig = {
      pending: { color: "bg-yellow-100 text-yellow-800", label: "Pending" },
      approved: { color: "bg-green-100 text-green-800", label: "Approved" },
      completed: { color: "bg-blue-100 text-blue-800", label: "Completed" },
      cancelled: { color: "bg-red-100 text-red-800", label: "Cancelled" },
    };

    const config =
      statusConfig[status as keyof typeof statusConfig] || statusConfig.pending;

    return (
      <span
        className={`px-3 py-1 rounded-full text-xs font-medium ${config.color}`}
      >
        {config.label}
      </span>
    );
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  const LoadingSpinner = ({ size = "medium" }) => {
    const sizeClass = {
      small: "h-6 w-6",
      medium: "h-16 w-16",
      large: "h-24 w-24",
    }[size];

    return (
      <div className="flex justify-center items-center py-10">
        <div
          className={`animate-spin rounded-full border-t-4 border-blue-600 border-solid ${sizeClass}`}
        ></div>
      </div>
    );
  };

  const BookingCard = ({
    booking,
    showActions = false,
  }: {
    booking: Booking;
    showActions?: boolean;
  }) => (
    <div className="bg-white rounded-xl border border-gray-200 p-6 hover:shadow-lg transition-all duration-300 hover:border-blue-200">
      <div className="flex justify-between items-start mb-4">
        <div>
          <h4 className="font-semibold text-gray-900 text-lg mb-1">
            {booking.customerName || `Booking #${booking.bookingId.slice(-8)}`}
          </h4>
          <p className="text-gray-600 text-sm">
            {booking.serviceType || "General Service"}
          </p>
        </div>
        {getStatusBadge(booking.status || "pending")}
      </div>

      <div className="grid grid-cols-2 gap-4 mb-4 text-sm">
        <div>
          <p className="text-gray-500">Booking ID</p>
          <p className="font-medium text-gray-900">{booking.bookingId}</p>
        </div>
        <div>
          <p className="text-gray-500">Date</p>
          <p className="font-medium text-gray-900">
            {booking.bookingDate ? formatDate(booking.bookingDate) : "N/A"}
          </p>
        </div>
        {booking.amount && (
          <div className="col-span-2">
            <p className="text-gray-500">Amount</p>
            <p className="font-bold text-lg text-green-600">
              ${booking.amount.toFixed(2)}
            </p>
          </div>
        )}
      </div>

      <div className="flex justify-between items-center pt-4 border-t border-gray-100">
        <button
          onClick={() => navigate(`/admin/bookings/${booking.bookingId}`)}
          className="text-blue-600 hover:text-blue-800 font-medium text-sm flex items-center gap-1 transition-colors"
        >
          View Details
          <svg
            className="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 5l7 7-7 7"
            />
          </svg>
        </button>

        {showActions && (
          <div className="flex gap-2">
            <button
              onClick={() => handleApproveBooking(booking.bookingId)}
              disabled={actionLoading === booking.bookingId}
              className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 text-sm font-medium"
            >
              {actionLoading === booking.bookingId ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              ) : (
                <svg
                  className="w-4 h-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              )}
              Approve
            </button>
            <button
              onClick={() => handleCancelBooking(booking.bookingId)}
              disabled={actionLoading === booking.bookingId}
              className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 text-sm font-medium"
            >
              {actionLoading === booking.bookingId ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              ) : (
                <svg
                  className="w-4 h-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
              )}
              Cancel
            </button>
          </div>
        )}
      </div>
    </div>
  );

  const TabNavigation = () => (
    <div className="bg-white rounded-xl p-2 shadow-sm border border-gray-200 mb-6">
      <div className="flex space-x-2">
        {[
          { key: "pending", label: "Pending", count: pendingBookings.length },
          {
            key: "approved",
            label: "Approved",
            count: approvedBookings.length,
          },
          {
            key: "completed",
            label: "Completed",
            count: completedBookings.length,
          },
        ].map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key as typeof activeTab)}
            className={`flex items-center gap-3 px-6 py-3 rounded-lg font-medium transition-all ${
              activeTab === tab.key
                ? "bg-blue-600 text-white shadow-md"
                : "text-gray-600 hover:text-gray-900 hover:bg-gray-100"
            }`}
          >
            <span>{tab.label}</span>
            <span
              className={`px-2 py-1 text-xs rounded-full ${
                activeTab === tab.key
                  ? "bg-blue-500 text-white"
                  : "bg-gray-200 text-gray-600"
              }`}
            >
              {tab.count}
            </span>
          </button>
        ))}
      </div>
    </div>
  );

  const getActiveBookings = () => {
    switch (activeTab) {
      case "pending":
        return pendingBookings;
      case "approved":
        return approvedBookings;
      case "completed":
        return completedBookings;
      default:
        return pendingBookings;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50/30 p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Booking Management
          </h1>
          <p className="text-gray-600">
            Manage and review all booking requests
          </p>
        </div>

        {/* Stats Overview */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-600 text-sm font-medium">
                  Pending Review
                </p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {pendingBookings.length}
                </p>
              </div>
              <div className="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
                <svg
                  className="w-6 h-6 text-yellow-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-600 text-sm font-medium">Approved</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {approvedBookings.length}
                </p>
              </div>
              <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                <svg
                  className="w-6 h-6 text-green-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-600 text-sm font-medium">Completed</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {completedBookings.length}
                </p>
              </div>
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <svg
                  className="w-6 h-6 text-blue-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
              </div>
            </div>
          </div>
        </div>

        {loading ? (
          <LoadingSpinner />
        ) : (
          <>
            <TabNavigation />

            {/* Bookings Grid */}
            <div className="mb-6 flex justify-between items-center">
              <h2 className="text-xl font-semibold text-gray-900 capitalize">
                {activeTab} Bookings
              </h2>
              <button
                onClick={() => navigate(`/admin/bookings/${activeTab}`)}
                className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-all font-medium flex items-center gap-2"
              >
                <svg
                  className="w-5 h-5"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
                  />
                </svg>
                View All{" "}
                {activeTab.charAt(0).toUpperCase() + activeTab.slice(1)}{" "}
                Bookings
              </button>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
              {getActiveBookings().length === 0 ? (
                <div className="col-span-full text-center py-12">
                  <div className="w-24 h-24 mx-auto mb-4 bg-gray-100 rounded-full flex items-center justify-center">
                    <svg
                      className="w-10 h-10 text-gray-400"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                      />
                    </svg>
                  </div>
                  <h3 className="text-lg font-medium text-gray-900 mb-2">
                    No {activeTab} bookings
                  </h3>
                  <p className="text-gray-500 max-w-sm mx-auto">
                    {activeTab === "pending"
                      ? "All booking requests have been processed."
                      : `No ${activeTab} bookings found at the moment.`}
                  </p>
                </div>
              ) : (
                getActiveBookings().map((booking) => (
                  <BookingCard
                    key={booking.bookingId}
                    booking={booking}
                    showActions={activeTab === "pending"}
                  />
                ))
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default BookingManagementPage;
