import { useNavigate } from "react-router-dom";

function CSOperatorDashboard() {
  const navigate = useNavigate();

  return (
    <div className="p-6 space-y-6">
      <h2 className="text-2xl font-bold text-gray-800">
        Charging Station Operator Dashboard
      </h2>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white shadow-md rounded-lg p-6 hover:shadow-xl transition">
          <h3 className="text-lg font-semibold mb-2">
            Manage Stations
          </h3>
          <p className="text-gray-600 mb-4">
            View all charging stations and manage their slots and availability.
          </p>
          <button
            onClick={() => navigate("/operator/stations")}
            className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
          >
            Go to Stations
          </button>
        </div>

        <div className="bg-white shadow-md rounded-lg p-6 hover:shadow-xl transition">
          <h3 className="text-lg font-semibold mb-2">Profile Settings</h3>
          <p className="text-gray-600 mb-4">
            View and update your operator profile information.
          </p>
          <button
            onClick={() => navigate("/profile")}
            className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
          >
            Go to Profile
          </button>
        </div>
      </div>
    </div>
  );
}

export default CSOperatorDashboard;
