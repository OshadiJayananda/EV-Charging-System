import { useEffect, useState } from "react";
import { getRequest } from "../../components/common/api";
import { useNavigate } from "react-router-dom";
import { Bar, Pie } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement, // Ensure ArcElement is imported for Pie charts
} from "chart.js";

// Register necessary Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement, // Register ArcElement for Pie charts
  Title,
  Tooltip,
  Legend
);

function AdminDashboard() {
  const navigate = useNavigate();
  const [activeStations, setActiveStations] = useState<number>(0);
  const [inactiveStations, setInactiveStations] = useState<number>(0);
  const [pendingReservations, setPendingReservations] = useState<number>(0);
  const [approvedReservations, setApprovedReservations] = useState<number>(0);
  const [completedReservations, setCompletedReservations] = useState<number>(0);
  const [chargingReservations, setChargingReservations] = useState<number>(0);
  const [mostPopularStation, setMostPopularStation] = useState<string>("");
  const [mostPopularTimeSlot, setMostPopularTimeSlot] = useState<string>("");

  // Fetch data for active stations count
  const fetchActiveInactiveStations = async () => {
    const res = await getRequest<{
      activeStations: number;
      inactiveStations: number;
    }>("/station/count");
    if (res) {
      setActiveStations(res.data.activeStations);
      setInactiveStations(res.data.inactiveStations);
    }
  };

  // Fetch data for reservation overview
  const fetchReservationOverview = async () => {
    const res = await getRequest<{
      pendingReservations: number;
      approvedReservations: number;
      completedReservations: number;
      chargingReservations: number;
    }>("/bookings/overview");
    if (res) {
      setPendingReservations(res.data.pendingReservations);
      setApprovedReservations(res.data.approvedReservations);
      setChargingReservations(res.data.chargingReservations);
      setCompletedReservations(res.data.completedReservations);
    }
  };

  // Fetch data for usage analytics
  const fetchUsageAnalytics = async () => {
    const res = await getRequest<{
      mostPopularStation: string;
      mostPopularTimeSlotStartTime: string;
    }>("/analytics/usage");
    if (res) {
      setMostPopularStation(res.data.mostPopularStation);
      setMostPopularTimeSlot(res.data.mostPopularTimeSlotStartTime);
    }
  };

  useEffect(() => {
    fetchActiveInactiveStations();
    fetchReservationOverview();
    fetchUsageAnalytics();
  }, []);

  // Chart Data for Active vs Inactive Stations
  const stationsChartData = {
    labels: ["Active Stations", "Inactive Stations"],
    datasets: [
      {
        label: "Station Count",
        data: [activeStations, inactiveStations],
        backgroundColor: ["#34D399", "#F87171"], // Green and Red colors
        borderRadius: 10,
        borderWidth: 1,
      },
    ],
  };

  // Chart Data for Reservations Overview
  const reservationsChartData = {
    labels: ["Pending", "Approved", "Completed", "Charging"],
    datasets: [
      {
        label: "Reservations",
        data: [
          pendingReservations,
          approvedReservations,
          completedReservations,
          chargingReservations,
        ],
        backgroundColor: ["#FBBF24", "#34D399", "#60A5FA", "#F472B6"], // Yellow, Green, Blue, Pink
        borderRadius: 10,
        borderWidth: 1,
      },
    ],
  };

  return (
    <div className="p-6 space-y-6">
      <h2 className="text-2xl font-bold text-gray-800">Admin Dashboard</h2>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* Active vs Inactive Stations */}
        <div className="bg-white shadow-md rounded-lg p-6 hover:shadow-xl transition">
          <h3 className="text-lg font-semibold mb-2">
            Active vs Inactive Stations
          </h3>
          <p className="text-gray-600 mb-4">
            Total active and inactive stations.
          </p>
          <Bar
            data={stationsChartData}
            options={{ responsive: true }}
            height={250}
          />
        </div>

        {/* Reservation Overview */}
        <div className="bg-white shadow-md rounded-lg p-6 hover:shadow-xl transition">
          <h3 className="text-lg font-semibold mb-2">Reservation Overview</h3>
          <p className="text-gray-600 mb-4">
            Overview of pending, approved, and completed reservations.
          </p>
          <Pie
            data={reservationsChartData}
            options={{ responsive: true }}
            height={250}
          />
        </div>

        {/* Combined Usage Analytics and Pending Bookings */}
        <div className="bg-white shadow-md rounded-lg p-6 hover:shadow-xl transition">
          <h3 className="text-lg font-semibold mb-2">
            Usage Analytics & Pending Bookings
          </h3>
          <p className="text-gray-600 mb-4">
            Stats about the most popular station and time slot, and pending
            bookings.
          </p>
          <div>
            <p className="text-sm text-gray-600">Most Popular Station:</p>
            <p className="text-xl font-bold text-indigo-600">
              {mostPopularStation}
            </p>
          </div>
          <div className="mt-4">
            <p className="text-sm text-gray-600">Most Popular Time Slot:</p>
            <p className="text-xl font-bold text-indigo-600">
              {mostPopularTimeSlot}
            </p>
          </div>
          <div className="mt-6">
            <p className="text-sm text-gray-600">Pending Bookings:</p>
            <p className="text-xl font-bold text-red-600">
              {pendingReservations}
            </p>
            <button
              onClick={() => navigate("/admin/bookings/pending")}
              className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 mt-4"
            >
              View Pending Bookings
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;
