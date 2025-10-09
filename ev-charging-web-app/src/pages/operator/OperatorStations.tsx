import { useEffect, useState } from "react";
import { getRequest } from "../../components/common/api";
import type { Station } from "../../types";
import { useNavigate } from "react-router-dom";
import Loading from "../../components/common/Loading";

const OperatorStations = () => {
  const [stations, setStations] = useState<Station[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const navigate = useNavigate();

  const fetchStations = async () => {
    setLoading(true);
    const res = await getRequest<Station[]>("/station");
    if (res) {
      setStations(res.data.filter(s => s.isActive));
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchStations();
  }, []);

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-semibold text-gray-800">My Stations</h2>
      </div>

      {loading ? (
        <div className="flex justify-center items-center h-64">
          <Loading size="lg" color="green" text="Loading stations..." />
        </div>
      ) : (
        <div className="overflow-x-auto bg-white shadow rounded-lg">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-100">
              <tr>
                <th className="px-4 py-2 text-left text-sm font-semibold text-gray-600">
                  Name
                </th>
                <th className="px-4 py-2 text-left text-sm font-semibold text-gray-600">
                  Location
                </th>
                <th className="px-4 py-2 text-left text-sm font-semibold text-gray-600">
                  Type
                </th>
                <th className="px-4 py-2 text-left text-sm font-semibold text-gray-600">
                  Capacity
                </th>
                <th className="px-4 py-2 text-left text-sm font-semibold text-gray-600">
                  Available Slots
                </th>
                <th className="px-4 py-2 text-left text-sm font-semibold text-gray-600">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {stations.map((s) => (
                <tr key={s.stationId} className="hover:bg-gray-50">
                  <td className="px-4 py-2 text-sm text-gray-800">{s.name}</td>
                  <td className="px-4 py-2 text-sm text-gray-600">
                    {s.location}
                  </td>
                  <td className="px-4 py-2 text-sm text-gray-600">{s.type}</td>
                  <td className="px-4 py-2 text-sm text-gray-600">
                    {s.capacity}
                  </td>
                  <td className="px-4 py-2 text-sm text-gray-600">
                    {s.availableSlots}
                  </td>
                  <td className="px-4 py-2 text-sm flex gap-2">
                    <button
                      onClick={() =>
                        navigate(`/operator/stations/${s.stationId}/slots`)
                      }
                      className="px-3 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition"
                    >
                      Manage Slots
                    </button>
                    <button
                      onClick={() =>
                        navigate(`/operator/stations/${s.stationId}/bookings`)
                      }
                      className="px-3 py-1 bg-green-600 text-white rounded-md hover:bg-green-700 transition"
                    >
                      View Bookings
                    </button>
                  </td>
                </tr>
              ))}
              {stations.length === 0 && (
                <tr>
                  <td
                    colSpan={6}
                    className="px-4 py-6 text-center text-gray-500 text-sm"
                  >
                    No stations found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default OperatorStations;
