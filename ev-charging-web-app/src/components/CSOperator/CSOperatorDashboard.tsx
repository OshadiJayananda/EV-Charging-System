import React from "react";

function CSOperatorDashboard() {
  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-gray-800">
        Charging Station Operator Dashboard
      </h2>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Update Slot Availability Card */}
        <div className="bg-white shadow-md rounded-lg p-6 hover:shadow-xl transition">
          <h3 className="text-lg font-semibold mb-2">
            Update Slot Availability
          </h3>
          <p className="text-gray-600 mb-4">
            Mark slots as available or occupied for each charging station.
          </p>
          <button className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">
            Go
          </button>
        </div>

        {/* View Station Status Card */}
        <div className="bg-white shadow-md rounded-lg p-6 hover:shadow-xl transition">
          <h3 className="text-lg font-semibold mb-2">View Station Status</h3>
          <p className="text-gray-600 mb-4">
            See the current status of all charging stations you manage.
          </p>
          <button className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">
            Go
          </button>
        </div>
      </div>
    </div>
  );
}

export default CSOperatorDashboard;
