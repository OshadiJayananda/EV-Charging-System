import { useEffect, useState } from "react";
import { getRequest, deleteRequest } from "../../components/common/api";
import type { Station } from "../../types";
import { useNavigate } from "react-router-dom";

const StationList = () => {
  const [stations, setStations] = useState<Station[]>([]);
  const navigate = useNavigate();

  const fetchStations = async () => {
    const res = await getRequest<Station[]>("/station");
    console.log(res);
    if (res) setStations(res.data);
  };

  const handleDeactivate = async (id: string) => {
    await deleteRequest(`/station/${id}/deactivate`);
    fetchStations();
  };

  useEffect(() => {
    fetchStations();
  }, []);

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-semibold text-gray-800">Stations</h2>
        <button
          onClick={() => navigate("/admin/stations/new")}
          className="px-4 py-2 bg-green-600 text-white rounded-md shadow hover:bg-green-700 transition"
        >
          + Add Station
        </button>
      </div>

      <div className="overflow-x-auto bg-white shadow rounded-lg">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-100">
            <tr>
              <th className="px-4 py-2 text-left text-sm font-semibold text-gray-600">Name</th>
              <th className="px-4 py-2 text-left text-sm font-semibold text-gray-600">Location</th>
              <th className="px-4 py-2 text-left text-sm font-semibold text-gray-600">Type</th>
              <th className="px-4 py-2 text-left text-sm font-semibold text-gray-600">Capacity</th>
              <th className="px-4 py-2 text-left text-sm font-semibold text-gray-600">Available Slots</th>
              <th className="px-4 py-2 text-left text-sm font-semibold text-gray-600">Status</th>
              <th className="px-4 py-2 text-left text-sm font-semibold text-gray-600">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {stations.map((s) => (
              <tr key={s.stationId} className="hover:bg-gray-50">
                <td className="px-4 py-2 text-sm text-gray-800">{s.name}</td>
                <td className="px-4 py-2 text-sm text-gray-600">{s.location}</td>
                <td className="px-4 py-2 text-sm text-gray-600">{s.type}</td>
                <td className="px-4 py-2 text-sm text-gray-600">{s.capacity}</td>
                <td className="px-4 py-2 text-sm text-gray-600">{s.availableSlots}</td>
                <td className="px-4 py-2 text-sm">
                  <span
                    className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                      s.isActive
                        ? "bg-green-100 text-green-800"
                        : "bg-red-100 text-red-800"
                    }`}
                  >
                    {s.isActive ? "Active" : "Inactive"}
                  </span>
                </td>
                <td className="px-4 py-2 text-sm flex gap-2">
                  <button
                    onClick={() => navigate(`/admin/stations/edit/${s.stationId}`)}
                    className="px-3 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => handleDeactivate(s.stationId)}
                    className="px-3 py-1 bg-yellow-500 text-white rounded-md hover:bg-yellow-600 transition"
                  >
                    Deactivate
                  </button>
                  <button
                    onClick={() => navigate(`/admin/stations/${s.stationId}/slots`)}
                    className="px-3 py-1 bg-purple-600 text-white rounded-md hover:bg-purple-700 transition"
                  >
                    Manage Slots
                  </button>
                </td>
              </tr>
            ))}
            {stations.length === 0 && (
              <tr>
                <td
                  colSpan={7}
                  className="px-4 py-6 text-center text-gray-500 text-sm"
                >
                  No stations found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default StationList;
