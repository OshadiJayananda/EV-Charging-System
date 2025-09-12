import React from "react";

function AdminDashboard() {
  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-gray-800">Admin Dashboard</h2>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Manage Users Card */}
        <div className="bg-white shadow-md rounded-lg p-6 hover:shadow-xl transition">
          <h3 className="text-lg font-semibold mb-2">Manage Users</h3>
          <p className="text-gray-600 mb-4">
            Create, edit, or deactivate EV Owners and operators.
          </p>
          <button className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">
            Go
          </button>
        </div>
        {/* Manage Stations Card */}
        <div className="bg-white shadow-md rounded-lg p-6 hover:shadow-xl transition">
          <h3 className="text-lg font-semibold mb-2">Manage Stations</h3>
          <p className="text-gray-600 mb-4">
            Add new charging stations or update existing ones.
          </p>
          <button className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">
            Go
          </button>
        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;
