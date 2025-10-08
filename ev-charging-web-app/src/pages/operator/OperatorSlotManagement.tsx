import { useEffect, useState } from "react";
import { getRequest, patchRequest } from "../../components/common/api";
import type { Slot, Station } from "../../types";
import { useParams, useNavigate } from "react-router-dom";
import Loading from "../../components/common/Loading";
import toast from "react-hot-toast";

const OperatorSlotManagement = () => {
  const [slots, setSlots] = useState<Slot[]>([]);
  const [station, setStation] = useState<Station | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const { stationId } = useParams();
  const navigate = useNavigate();

  const fetchStationAndSlots = async () => {
    if (!stationId) return;

    setLoading(true);
    const stationRes = await getRequest<Station>(`/station/${stationId}`);
    if (stationRes) {
      setStation(stationRes.data);
      setSlots(stationRes.data.slots || []);
    }
    setLoading(false);
  };

  const handleUpdateSlotStatus = async (slotId: string, newStatus: string) => {
    setLoading(true);
    const res = await patchRequest(`/slots/${slotId}/status`, { status: newStatus });
    if (res) {
      toast.success(`Slot status updated to ${newStatus}`);
      await fetchStationAndSlots();
    }
    setLoading(false);
  };

  const handleToggleSlot = async (slotId: string) => {
    setLoading(true);
    const res = await patchRequest(`/slots/${slotId}/toggle`);
    if (res) {
      toast.success("Slot status toggled");
      await fetchStationAndSlots();
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchStationAndSlots();
  }, [stationId]);

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case "available":
        return "bg-green-100 text-green-800";
      case "booked":
      case "charging":
        return "bg-blue-100 text-blue-800";
      case "under maintenance":
        return "bg-yellow-100 text-yellow-800";
      case "out of order":
      case "faulty":
        return "bg-red-100 text-red-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <button
            onClick={() => navigate("/operator/stations")}
            className="text-sm text-gray-600 hover:text-gray-800 mb-2 flex items-center gap-1"
          >
            ← Back to Stations
          </button>
          <h2 className="text-2xl font-semibold text-gray-800">
            Manage Slots - {station?.name || "Loading..."}
          </h2>
          <p className="text-sm text-gray-600 mt-1">
            {station?.location}
          </p>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center items-center h-64">
          <Loading size="lg" color="green" text="Loading slots..." />
        </div>
      ) : (
        <div className="overflow-x-auto bg-white shadow rounded-lg">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-100">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                  Slot #
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                  Connector Type
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                  Status
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-600">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {slots.map((slot) => (
                <tr key={slot.slotId} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm text-gray-800">
                    {slot.number}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {slot.connectorType}
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <span
                      className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(
                        slot.status
                      )}`}
                    >
                      {slot.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <div className="flex gap-2">
                      <select
                        value={slot.status}
                        onChange={(e) =>
                          handleUpdateSlotStatus(slot.slotId, e.target.value)
                        }
                        className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
                        disabled={slot.status === "Charging"}
                      >
                        <option value="Available">Available</option>
                        <option value="Under Maintenance">Under Maintenance</option>
                        <option value="Out Of Order">Out Of Order</option>
                      </select>

                      {(slot.status === "Available" || slot.status === "Booked") && (
                        <button
                          onClick={() => handleToggleSlot(slot.slotId)}
                          className="px-3 py-1 bg-purple-600 text-white rounded-md hover:bg-purple-700 transition text-sm"
                        >
                          Toggle
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {slots.length === 0 && (
                <tr>
                  <td
                    colSpan={4}
                    className="px-4 py-6 text-center text-gray-500 text-sm"
                  >
                    No slots found for this station.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 className="text-sm font-semibold text-blue-900 mb-2">Status Guide:</h3>
        <ul className="text-sm text-blue-800 space-y-1">
          <li><strong>Available:</strong> Slot is ready for booking</li>
          <li><strong>Booked/Charging:</strong> Slot is currently in use</li>
          <li><strong>Under Maintenance:</strong> Slot is temporarily unavailable for maintenance</li>
          <li><strong>Out Of Order:</strong> Slot is not functioning and requires repair</li>
        </ul>
      </div>
    </div>
  );
};

export default OperatorSlotManagement;
