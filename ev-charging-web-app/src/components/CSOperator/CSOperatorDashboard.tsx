import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getRequest } from "../common/api";
import Loading from "../common/Loading";
import {
  Battery,
  Clock,
  DollarSign,
  TrendingUp,
  Zap,
  CheckCircle,
  AlertCircle,
  Calendar,
  XCircle
} from "lucide-react";

interface StationMetrics {
  stationId: string;
  name: string;
  location: string;
  status: string;
  totalSlots: number;
  availableSlots: number;
  chargingSlots: number;
  bookedSlots: number;
  inactiveSlots: number;
  activeBookings: number;
  todayRevenue: number;
  utilizationRate: number;
}

function CSOperatorDashboard() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [stationMetrics, setStationMetrics] = useState<StationMetrics | null>(null);
  const [slots, setSlots] = useState<any[]>([]);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const stationsRes = await getRequest<any[]>("/station");
      if (stationsRes && stationsRes.data && stationsRes.data.length > 0) {
        const station = stationsRes.data[0];

        const detailsRes = await getRequest<any>(`/station/${station.stationId}`);
        if (detailsRes) {
          const stationDetails = detailsRes.data;
          const slotsData = stationDetails.slots || [];
          setSlots(slotsData);

          const available = slotsData.filter((s: any) => s.status === "Available").length;
          const charging = slotsData.filter((s: any) => s.status === "Charging").length;
          const booked = slotsData.filter((s: any) => s.status === "Booked").length;
          const inactive = slotsData.filter((s: any) =>
            s.status === "Under Maintenance" || s.status === "Out Of Order" || s.status === "Inactive"
          ).length;

          const bookingsRes = await getRequest<any[]>(`/bookings/station/${station.stationId}/today`);
          const activeBookingsList = bookingsRes?.data || [];

          const metrics: StationMetrics = {
            stationId: station.stationId,
            name: stationDetails.name,
            location: stationDetails.location,
            status: "Online",
            totalSlots: slotsData.length,
            availableSlots: available,
            chargingSlots: charging,
            bookedSlots: booked,
            inactiveSlots: inactive,
            activeBookings: activeBookingsList.length,
            todayRevenue: activeBookingsList.length * 24.55,
            utilizationRate: slotsData.length > 0
              ? Math.round(((charging + booked) / slotsData.length) * 100)
              : 0,
          };

          setStationMetrics(metrics);
        }
      }
    } catch (error) {
      console.error("Error fetching dashboard data:", error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status?.toLowerCase()) {
      case "available": return "text-green-600 bg-green-50 border-green-200";
      case "charging": return "text-purple-600 bg-purple-50 border-purple-200";
      case "booked": return "text-blue-600 bg-blue-50 border-blue-200";
      case "pending": return "text-yellow-600 bg-yellow-50 border-yellow-200";
      case "approved": return "text-blue-600 bg-blue-50 border-blue-200";
      case "active": case "finalized": return "text-green-600 bg-green-50 border-green-200";
      default: return "text-gray-600 bg-gray-50 border-gray-200";
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status?.toLowerCase()) {
      case "available": return <CheckCircle className="w-4 h-4" />;
      case "charging": return <Zap className="w-4 h-4" />;
      case "booked": return <Clock className="w-4 h-4" />;
      case "inactive": return <XCircle className="w-4 h-4" />;
      default: return <AlertCircle className="w-4 h-4" />;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-50">
        <Loading size="lg" color="blue" text="Loading dashboard..." />
      </div>
    );
  }

  if (!stationMetrics) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-50">
        <div className="text-center">
          <p className="text-gray-600 mb-4">No station data available</p>
          <button
            onClick={() => navigate("/operator/stations")}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            View Stations
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Dashboard Overview</h1>
            <p className="text-gray-600 mt-1">Manage your charging station operations</p>
          </div>
          <button
            onClick={() => fetchDashboardData()}
            className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            Refresh
          </button>
        </div>

        <div className="mb-8 bg-gradient-to-r from-blue-600 to-blue-700 rounded-2xl p-6 text-white shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <div className="flex items-center gap-3 mb-2">
                <div className="p-2 bg-white bg-opacity-20 rounded-lg">
                  <Zap className="w-6 h-6" />
                </div>
                <h2 className="text-2xl font-bold">{stationMetrics.name}</h2>
              </div>
              <p className="text-blue-100 flex items-center gap-2">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                {stationMetrics.location}
              </p>
            </div>
            <div className="flex items-center gap-4">
              <div className="text-right">
                <div className="text-5xl font-bold">{stationMetrics.totalSlots}</div>
                <div className="text-blue-100 text-sm">Total Slots</div>
              </div>
              <div className="flex items-center gap-2 px-4 py-2 bg-white bg-opacity-20 rounded-lg">
                <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                <span className="text-sm font-medium">{stationMetrics.status}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-xl shadow-md p-6 border-l-4 border-green-500">
            <div className="flex items-center justify-between mb-4">
              <div className="p-3 bg-green-100 rounded-lg">
                <Battery className="w-6 h-6 text-green-600" />
              </div>
              <span className="text-sm font-medium text-green-600 flex items-center gap-1">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                Available
              </span>
            </div>
            <div className="text-3xl font-bold text-gray-900 mb-1">
              {stationMetrics.availableSlots}
            </div>
            <div className="text-sm text-gray-600">of {stationMetrics.totalSlots} total slots</div>
            <div className="mt-4 bg-gray-200 rounded-full h-2">
              <div
                className="bg-green-500 h-2 rounded-full transition-all duration-500"
                style={{ width: `${(stationMetrics.availableSlots / stationMetrics.totalSlots) * 100}%` }}
              ></div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-md p-6 border-l-4 border-blue-500">
            <div className="flex items-center justify-between mb-4">
              <div className="p-3 bg-blue-100 rounded-lg">
                <Calendar className="w-6 h-6 text-blue-600" />
              </div>
              <span className="text-sm font-medium text-blue-600 flex items-center gap-1">
                <Clock className="w-4 h-4" />
                Real-time
              </span>
            </div>
            <div className="text-3xl font-bold text-gray-900 mb-1">
              {stationMetrics.activeBookings}
            </div>
            <div className="text-sm text-gray-600">Currently charging</div>
            <div className="mt-4 text-xs text-blue-600 font-medium">
              {stationMetrics.chargingSlots} active • {stationMetrics.bookedSlots} booked
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-md p-6 border-l-4 border-purple-500">
            <div className="flex items-center justify-between mb-4">
              <div className="p-3 bg-purple-100 rounded-lg">
                <DollarSign className="w-6 h-6 text-purple-600" />
              </div>
              <span className="text-sm font-medium text-purple-600 flex items-center gap-1">
                <TrendingUp className="w-4 h-4" />
                +12%
              </span>
            </div>
            <div className="text-3xl font-bold text-gray-900 mb-1">
              ${stationMetrics.todayRevenue.toFixed(2)}
            </div>
            <div className="text-sm text-gray-600">Today's revenue</div>
            <div className="mt-4 text-xs text-purple-600 font-medium">
              +12% from yesterday
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-md p-6 border-l-4 border-yellow-500">
            <div className="flex items-center justify-between mb-4">
              <div className="p-3 bg-yellow-100 rounded-lg">
                <TrendingUp className="w-6 h-6 text-yellow-600" />
              </div>
              <span className="text-sm font-medium text-yellow-600">Efficiency</span>
            </div>
            <div className="text-3xl font-bold text-gray-900 mb-1">
              {stationMetrics.utilizationRate}%
            </div>
            <div className="text-sm text-gray-600">Utilization Rate</div>
            <div className="mt-4 bg-gray-200 rounded-full h-2">
              <div
                className="bg-yellow-500 h-2 rounded-full transition-all duration-500"
                style={{ width: `${stationMetrics.utilizationRate}%` }}
              ></div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white rounded-xl shadow-md p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                <Zap className="w-5 h-5 text-green-600" />
                Station Overview
              </h3>
            </div>

            <div className="grid grid-cols-2 gap-4 mb-6">
              <div className="p-4 bg-green-50 rounded-lg text-center border border-green-200">
                <div className="flex items-center justify-center gap-2 mb-2">
                  <CheckCircle className="w-5 h-5 text-green-600" />
                  <span className="text-2xl font-bold text-green-600">
                    {stationMetrics.availableSlots}
                  </span>
                </div>
                <div className="text-sm text-green-700 font-medium">Available</div>
              </div>

              <div className="p-4 bg-purple-50 rounded-lg text-center border border-purple-200">
                <div className="flex items-center justify-center gap-2 mb-2">
                  <Zap className="w-5 h-5 text-purple-600" />
                  <span className="text-2xl font-bold text-purple-600">
                    {stationMetrics.chargingSlots}
                  </span>
                </div>
                <div className="text-sm text-purple-700 font-medium">Charging</div>
              </div>

              <div className="p-4 bg-blue-50 rounded-lg text-center border border-blue-200">
                <div className="flex items-center justify-center gap-2 mb-2">
                  <Clock className="w-5 h-5 text-blue-600" />
                  <span className="text-2xl font-bold text-blue-600">
                    {stationMetrics.bookedSlots}
                  </span>
                </div>
                <div className="text-sm text-blue-700 font-medium">Booked</div>
              </div>

              <div className="p-4 bg-gray-50 rounded-lg text-center border border-gray-200">
                <div className="flex items-center justify-center gap-2 mb-2">
                  <XCircle className="w-5 h-5 text-gray-600" />
                  <span className="text-2xl font-bold text-gray-600">
                    {stationMetrics.inactiveSlots}
                  </span>
                </div>
                <div className="text-sm text-gray-700 font-medium">Inactive</div>
              </div>
            </div>

            <div className="space-y-2 mb-4">
              <div className="flex justify-between items-center text-sm">
                <span className="text-gray-600">Utilization Rate</span>
                <span className="font-semibold text-gray-900">{stationMetrics.utilizationRate}%</span>
              </div>
              <div className="bg-gray-200 rounded-full h-3">
                <div
                  className="bg-gradient-to-r from-yellow-400 to-yellow-600 h-3 rounded-full transition-all duration-500"
                  style={{ width: `${stationMetrics.utilizationRate}%` }}
                ></div>
              </div>
            </div>

            <div className="pt-4 border-t border-gray-200">
              <div className="flex items-center justify-between text-sm">
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                  <span className="text-gray-600">Station Online</span>
                </div>
                <span className="text-green-600 font-semibold">Healthy</span>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-md p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                <Battery className="w-5 h-5 text-blue-600" />
                Slot Details
              </h3>
              <button
                onClick={() => navigate(`/operator/stations/${stationMetrics.stationId}/slots`)}
                className="text-sm text-blue-600 hover:text-blue-700 font-medium flex items-center gap-1"
              >
                View All
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </button>
            </div>

            <div className="space-y-3 max-h-96 overflow-y-auto">
              {slots.slice(0, 6).map((slot) => (
                <div
                  key={slot.slotId}
                  className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:border-blue-300 hover:bg-blue-50 transition-all"
                >
                  <div className="flex items-center gap-4">
                    <div className="flex items-center justify-center w-10 h-10 bg-blue-600 text-white rounded-lg font-bold">
                      {slot.number}
                    </div>
                    <div>
                      <div className="font-medium text-gray-900">Slot {slot.number}</div>
                      <div className="text-sm text-gray-600">{slot.connectorType}</div>
                    </div>
                  </div>
                  <div className={`flex items-center gap-2 px-3 py-1 rounded-full border text-sm font-medium ${getStatusColor(slot.status)}`}>
                    {getStatusIcon(slot.status)}
                    {slot.status}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="mt-6 grid grid-cols-1 lg:grid-cols-3 gap-4">
          <button
            onClick={() => navigate(`/operator/stations/${stationMetrics.stationId}/slots`)}
            className="p-6 bg-white rounded-xl shadow-md hover:shadow-lg transition-all border-2 border-transparent hover:border-blue-500 group"
          >
            <div className="flex items-center justify-between">
              <div>
                <h4 className="font-semibold text-gray-900 text-lg mb-1">Slot Management</h4>
                <p className="text-gray-600 text-sm">Manage slot availability</p>
              </div>
              <div className="p-3 bg-blue-100 rounded-lg group-hover:bg-blue-200 transition-colors">
                <Battery className="w-6 h-6 text-blue-600" />
              </div>
            </div>
          </button>

          <button
            onClick={() => navigate(`/operator/stations/${stationMetrics.stationId}/bookings`)}
            className="p-6 bg-white rounded-xl shadow-md hover:shadow-lg transition-all border-2 border-transparent hover:border-green-500 group"
          >
            <div className="flex items-center justify-between">
              <div>
                <h4 className="font-semibold text-gray-900 text-lg mb-1">Bookings</h4>
                <p className="text-gray-600 text-sm">View reservations</p>
                <div className="mt-2 inline-flex items-center px-2 py-1 bg-green-100 text-green-700 rounded-full text-xs font-medium">
                  {stationMetrics.activeBookings} active
                </div>
              </div>
              <div className="p-3 bg-green-100 rounded-lg group-hover:bg-green-200 transition-colors">
                <Calendar className="w-6 h-6 text-green-600" />
              </div>
            </div>
          </button>

          <button
            onClick={() => navigate("/operator/stations")}
            className="p-6 bg-white rounded-xl shadow-md hover:shadow-lg transition-all border-2 border-transparent hover:border-purple-500 group"
          >
            <div className="flex items-center justify-between">
              <div>
                <h4 className="font-semibold text-gray-900 text-lg mb-1">All Stations</h4>
                <p className="text-gray-600 text-sm">Manage all stations</p>
              </div>
              <div className="p-3 bg-purple-100 rounded-lg group-hover:bg-purple-200 transition-colors">
                <Zap className="w-6 h-6 text-purple-600" />
              </div>
            </div>
          </button>
        </div>
      </div>
    </div>
  );
}

export default CSOperatorDashboard;
