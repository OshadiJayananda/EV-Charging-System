import { useEffect, useState } from "react";
import { getRequest, patchRequest } from "../../components/common/api";
import { useParams, useNavigate } from "react-router-dom";
import Loading from "../../components/common/Loading";
import toast from "react-hot-toast";
import {
  Calendar,
  Clock,
  Zap,
  CheckCircle,
  XCircle,
  User,
  ArrowLeft,
  RefreshCw,
  AlertCircle,
  Play,
  Square,
} from "lucide-react";
import type { Booking } from "../../types";

const OperatorBookings = () => {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [view, setView] = useState<"today" | "upcoming">("today");
  const [processingBooking, setProcessingBooking] = useState<string | null>(
    null
  );
  const { stationId } = useParams();
  const navigate = useNavigate();

  const fetchBookings = async () => {
    if (!stationId) return;

    setLoading(true);
    try {
      const endpoint =
        view === "today"
          ? `/bookings/station/${stationId}/today`
          : `/bookings/station/${stationId}/upcoming`;

      const res = await getRequest<Booking[]>(endpoint);
      if (res && res.data) {
        setBookings(res.data);
      } else {
        setBookings([]);
      }
    } catch (error) {
      console.error("Error fetching bookings:", error);
      toast.error("Failed to load bookings");
      setBookings([]);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (bookingId: string) => {
    setProcessingBooking(bookingId);
    try {
      const res = await patchRequest(`/bookings/${bookingId}/approve`, {});
      if (res) {
        toast.success("Booking approved successfully");
        await fetchBookings();
      }
    } catch (error) {
      console.error("Error approving booking:", error);
      toast.error("Failed to approve booking");
    } finally {
      setProcessingBooking(null);
    }
  };

  const handleStartCharging = async (bookingId: string) => {
    setProcessingBooking(bookingId);
    try {
      const res = await patchRequest(`/bookings/${bookingId}/start`, {});
      if (res) {
        toast.success("Charging started");
        await fetchBookings();
      }
    } catch (error) {
      console.error("Error starting charging:", error);
      toast.error("Failed to start charging");
    } finally {
      setProcessingBooking(null);
    }
  };

  const handleFinalize = async (bookingId: string) => {
    setProcessingBooking(bookingId);
    try {
      const res = await patchRequest(`/bookings/${bookingId}/finalize`, {});
      if (res) {
        toast.success("Booking finalized successfully");
        await fetchBookings();
      }
    } catch (error) {
      console.error("Error finalizing booking:", error);
      toast.error("Failed to finalize booking");
    } finally {
      setProcessingBooking(null);
    }
  };

  const getStatusInfo = (status: string) => {
    switch (status?.toLowerCase()) {
      case "pending":
        return {
          icon: <Clock className="w-4 h-4" />,
          color: "bg-yellow-100 text-yellow-700 border-yellow-300",
          badge: "bg-yellow-50 text-yellow-700 border-yellow-200",
        };
      case "approved":
        return {
          icon: <CheckCircle className="w-4 h-4" />,
          color: "bg-blue-100 text-blue-700 border-blue-300",
          badge: "bg-blue-50 text-blue-700 border-blue-200",
        };
      case "charging":
        return {
          icon: <Zap className="w-4 h-4" />,
          color: "bg-green-100 text-green-700 border-green-300",
          badge: "bg-green-50 text-green-700 border-green-200",
        };
      case "finalized":
        return {
          icon: <CheckCircle className="w-4 h-4" />,
          color: "bg-gray-100 text-gray-700 border-gray-300",
          badge: "bg-gray-50 text-gray-700 border-gray-200",
        };
      case "cancelled":
        return {
          icon: <XCircle className="w-4 h-4" />,
          color: "bg-red-100 text-red-700 border-red-300",
          badge: "bg-red-50 text-red-700 border-red-200",
        };
      default:
        return {
          icon: <AlertCircle className="w-4 h-4" />,
          color: "bg-gray-100 text-gray-700 border-gray-300",
          badge: "bg-gray-50 text-gray-700 border-gray-200",
        };
    }
  };

  const formatTime = (dateString: string) => {
    try {
      const date = new Date(dateString);
      return date.toLocaleTimeString("en-US", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: true,
      });
    } catch (error) {
      return "Invalid time";
    }
  };

  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric",
      });
    } catch (error) {
      return "Invalid date";
    }
  };

  const getDuration = (start: string, end: string) => {
    try {
      const startDate = new Date(start);
      const endDate = new Date(end);
      const hours = Math.abs(endDate.getTime() - startDate.getTime()) / 36e5;
      return `${hours.toFixed(1)}h`;
    } catch (error) {
      return "N/A";
    }
  };

  useEffect(() => {
    fetchBookings();
  }, [stationId, view]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <button
            onClick={() => navigate("/operator/dashboard")}
            className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-4 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span className="font-medium">Back to Dashboard</span>
          </button>

          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">
                Bookings Management
              </h1>
              <p className="text-gray-600 mt-1">
                Manage your charging station operations
              </p>
            </div>
            <button
              onClick={() => fetchBookings()}
              className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
              disabled={loading}
            >
              <RefreshCw
                className={`w-5 h-5 ${loading ? "animate-spin" : ""}`}
              />
              Refresh
            </button>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-md overflow-hidden">
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                <Calendar className="w-5 h-5 text-blue-600" />
                Bookings Management
              </h3>
            </div>
          </div>

          <div className="border-b border-gray-200 bg-gray-50">
            <div className="flex px-6">
              <button
                onClick={() => setView("today")}
                className={`flex items-center gap-2 px-6 py-4 font-medium border-b-2 transition-all ${
                  view === "today"
                    ? "border-blue-600 text-blue-600 bg-white"
                    : "border-transparent text-gray-600 hover:text-gray-900"
                }`}
              >
                <Clock className="w-4 h-4" />
                Today's Bookings
                {bookings.filter((b) => {
                  const today = new Date().toDateString();
                  const bookingDate = new Date(b.startTime).toDateString();
                  return today === bookingDate;
                }).length > 0 && (
                  <span className="px-2 py-0.5 bg-blue-100 text-blue-700 rounded-full text-xs font-semibold">
                    {
                      bookings.filter((b) => {
                        const today = new Date().toDateString();
                        const bookingDate = new Date(
                          b.startTime
                        ).toDateString();
                        return today === bookingDate;
                      }).length
                    }
                  </span>
                )}
              </button>

              <button
                onClick={() => setView("upcoming")}
                className={`flex items-center gap-2 px-6 py-4 font-medium border-b-2 transition-all ${
                  view === "upcoming"
                    ? "border-blue-600 text-blue-600 bg-white"
                    : "border-transparent text-gray-600 hover:text-gray-900"
                }`}
              >
                <Calendar className="w-4 h-4" />
                Upcoming
                {bookings.filter((b) => {
                  const today = new Date();
                  const bookingDate = new Date(b.startTime);
                  return bookingDate > today;
                }).length > 0 && (
                  <span className="px-2 py-0.5 bg-gray-100 text-gray-700 rounded-full text-xs font-semibold">
                    {
                      bookings.filter((b) => {
                        const today = new Date();
                        const bookingDate = new Date(b.startTime);
                        return bookingDate > today;
                      }).length
                    }
                  </span>
                )}
              </button>
            </div>
          </div>

          {loading ? (
            <div className="flex justify-center items-center py-16">
              <Loading size="lg" color="blue" text="Loading bookings..." />
            </div>
          ) : (
            <div className="p-6">
              {bookings.length === 0 ? (
                <div className="text-center py-16">
                  <Calendar className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                  <h3 className="text-lg font-medium text-gray-900 mb-2">
                    No bookings found
                  </h3>
                  <p className="text-gray-600">
                    There are no {view} bookings for this station.
                  </p>
                </div>
              ) : (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                  {bookings.map((booking) => {
                    const statusInfo = getStatusInfo(booking.status);
                    const isProcessing =
                      processingBooking === booking.bookingId;

                    return (
                      <div
                        key={booking.bookingId}
                        className="border-2 border-gray-200 rounded-xl p-6 hover:border-blue-300 hover:shadow-lg transition-all bg-white"
                      >
                        <div className="flex items-start justify-between mb-4">
                          <div className="flex items-center gap-3">
                            <div className="flex items-center justify-center w-12 h-12 bg-blue-600 text-white rounded-lg font-bold text-xl">
                              {booking.slotNumber}
                            </div>
                            <div>
                              <div className="font-semibold text-gray-900">
                                Slot {booking.slotNumber}
                              </div>
                              <div className="text-sm text-gray-600">
                                {booking.connectorType || "Standard"}
                              </div>
                            </div>
                          </div>
                          <div
                            className={`flex items-center gap-2 px-3 py-1 rounded-full border text-sm font-medium ${statusInfo.badge}`}
                          >
                            {statusInfo.icon}
                            {booking.status}
                          </div>
                        </div>

                        <div className="space-y-3 mb-4">
                          <div className="flex items-center gap-2 text-sm text-gray-600">
                            <User className="w-4 h-4" />
                            <span className="font-medium">ID:</span>
                            <span>
                              {booking.ownerName ||
                                booking.ownerId.substring(0, 8)}
                            </span>
                          </div>

                          <div className="flex items-center gap-2 text-sm text-gray-600">
                            <Clock className="w-4 h-4" />
                            <span className="font-medium">Time:</span>
                            <span>
                              {formatTime(booking.startTime)} -{" "}
                              {formatTime(booking.endTime)}
                            </span>
                          </div>

                          <div className="flex items-center gap-2 text-sm text-gray-600">
                            <Calendar className="w-4 h-4" />
                            <span className="font-medium">Date:</span>
                            <span>{formatDate(booking.startTime)}</span>
                          </div>

                          <div className="flex items-center gap-2 text-sm text-gray-600">
                            <Zap className="w-4 h-4" />
                            <span className="font-medium">Duration:</span>
                            <span>
                              {getDuration(booking.startTime, booking.endTime)}
                            </span>
                          </div>

                          <div className="pt-2 border-t border-gray-200">
                            <div className="text-xs text-gray-500">
                              Booking ID: {booking.bookingId.substring(0, 12)}
                              ...
                            </div>
                          </div>
                        </div>

                        <div className="flex gap-2">
                          {booking.status.toLowerCase() === "pending" && (
                            <button
                              onClick={() => handleApprove(booking.bookingId)}
                              disabled={isProcessing}
                              className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed text-sm font-medium"
                            >
                              {isProcessing ? (
                                <>
                                  <RefreshCw className="w-4 h-4 animate-spin" />
                                  Processing...
                                </>
                              ) : (
                                <>
                                  <CheckCircle className="w-4 h-4" />
                                  Approve
                                </>
                              )}
                            </button>
                          )}

                          {booking.status.toLowerCase() === "approved" && (
                            <button
                              onClick={() =>
                                handleStartCharging(booking.bookingId)
                              }
                              disabled={isProcessing}
                              className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed text-sm font-medium"
                            >
                              {isProcessing ? (
                                <>
                                  <RefreshCw className="w-4 h-4 animate-spin" />
                                  Starting...
                                </>
                              ) : (
                                <>
                                  <Play className="w-4 h-4" />
                                  Start Charging
                                </>
                              )}
                            </button>
                          )}

                          {booking.status.toLowerCase() === "charging" && (
                            <button
                              onClick={() => handleFinalize(booking.bookingId)}
                              disabled={isProcessing}
                              className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed text-sm font-medium"
                            >
                              {isProcessing ? (
                                <>
                                  <RefreshCw className="w-4 h-4 animate-spin" />
                                  Finalizing...
                                </>
                              ) : (
                                <>
                                  <Square className="w-4 h-4" />
                                  Finalize
                                </>
                              )}
                            </button>
                          )}

                          {(booking.status.toLowerCase() === "finalized" ||
                            booking.status.toLowerCase() === "completed") && (
                            <div className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-gray-100 text-gray-600 rounded-lg text-sm font-medium">
                              <CheckCircle className="w-4 h-4" />
                              Completed
                            </div>
                          )}

                          {booking.status.toLowerCase() === "cancelled" && (
                            <div className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-red-100 text-red-600 rounded-lg text-sm font-medium">
                              <XCircle className="w-4 h-4" />
                              Cancelled
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          )}
        </div>

        <div className="mt-6 bg-blue-50 border border-blue-200 rounded-xl p-6">
          <h3 className="text-sm font-semibold text-blue-900 mb-3 flex items-center gap-2">
            <AlertCircle className="w-4 h-4" />
            Booking Status Guide
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 text-sm">
            <div className="flex items-start gap-2">
              <Clock className="w-4 h-4 text-yellow-600 mt-0.5 flex-shrink-0" />
              <div>
                <strong className="block text-yellow-700">Pending</strong>
                <span className="text-gray-600">Awaiting approval</span>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <CheckCircle className="w-4 h-4 text-blue-600 mt-0.5 flex-shrink-0" />
              <div>
                <strong className="block text-blue-700">Approved</strong>
                <span className="text-gray-600">Ready to start</span>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <Zap className="w-4 h-4 text-green-600 mt-0.5 flex-shrink-0" />
              <div>
                <strong className="block text-green-700">Charging</strong>
                <span className="text-gray-600">In progress</span>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <CheckCircle className="w-4 h-4 text-gray-600 mt-0.5 flex-shrink-0" />
              <div>
                <strong className="block text-gray-700">Finalized</strong>
                <span className="text-gray-600">Completed</span>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <XCircle className="w-4 h-4 text-red-600 mt-0.5 flex-shrink-0" />
              <div>
                <strong className="block text-red-700">Cancelled</strong>
                <span className="text-gray-600">Booking cancelled</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OperatorBookings;