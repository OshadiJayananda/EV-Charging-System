import { useEffect, useState } from "react";
import { getRequest, patchRequest } from "../../components/common/api";
import type { Slot, Station } from "../../types";
import { useParams, useNavigate } from "react-router-dom";
import Loading from "../../components/common/Loading";
import toast from "react-hot-toast";
import {
  Battery,
  Zap,
  Clock,
  AlertCircle,
  CheckCircle,
  XCircle,
  ArrowLeft,
  RefreshCw,
} from "lucide-react";

const OperatorSlotManagement = () => {
  const [slots, setSlots] = useState<Slot[]>([]);
  const [station, setStation] = useState<Station | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [updatingSlot, setUpdatingSlot] = useState<string | null>(null);
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
    setUpdatingSlot(slotId);
    const res = await patchRequest(`/slots/${slotId}/status`, {
      status: newStatus,
    });
    if (res) {
      toast.success(`Slot status updated to ${newStatus}`);
      await fetchStationAndSlots();
    }
    setUpdatingSlot(null);
  };

  useEffect(() => {
    fetchStationAndSlots();
  }, [stationId]);

  const getStatusInfo = (status: string) => {
    switch (status?.toLowerCase()) {
      case "available":
        return {
          icon: <CheckCircle className="w-5 h-5" />,
          color: "text-green-600 bg-green-50 border-green-300",
          badgeColor: "bg-green-100 text-green-700",
        };
      case "charging":
        return {
          icon: <Zap className="w-5 h-5" />,
          color: "text-purple-600 bg-purple-50 border-purple-300",
          badgeColor: "bg-purple-100 text-purple-700",
        };
      case "booked":
        return {
          icon: <Clock className="w-5 h-5" />,
          color: "text-blue-600 bg-blue-50 border-blue-300",
          badgeColor: "bg-blue-100 text-blue-700",
        };
      case "under maintenance":
        return {
          icon: <AlertCircle className="w-5 h-5" />,
          color: "text-yellow-600 bg-yellow-50 border-yellow-300",
          badgeColor: "bg-yellow-100 text-yellow-700",
        };
      case "out of order":
      case "inactive":
        return {
          icon: <XCircle className="w-5 h-5" />,
          color: "text-red-600 bg-red-50 border-red-300",
          badgeColor: "bg-red-100 text-red-700",
        };
      default:
        return {
          icon: <AlertCircle className="w-5 h-5" />,
          color: "text-gray-600 bg-gray-50 border-gray-300",
          badgeColor: "bg-gray-100 text-gray-700",
        };
    }
  };

  const availableSlots = slots.filter((s) => s.status === "Available").length;
  const outOfOrderSlots = slots.filter(
    (s) => s.status === "Out Of Order"
  ).length;
  const maintenanceSlots = slots.filter(
    (s) => s.status === "Under Maintenance"
  ).length;

  if (loading && !station) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-50">
        <Loading size="lg" color="blue" text="Loading slots..." />
      </div>
    );
  }

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
                Slot Management
              </h1>
              <p className="text-gray-600 mt-1">
                Manage your charging station operations
              </p>
            </div>
            <button
              onClick={() => fetchStationAndSlots()}
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

        <div className="mb-8 bg-gradient-to-r from-blue-600 to-blue-700 rounded-2xl p-6 text-white shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <div className="flex items-center gap-3 mb-2">
                <div className="p-2 bg-white bg-opacity-20 rounded-lg">
                  <Battery className="w-6 h-6" />
                </div>
                <h2 className="text-2xl font-bold">
                  {station?.name || "Loading..."}
                </h2>
              </div>
              <p className="text-blue-100">{station?.location}</p>
            </div>
            <div className="text-right">
              <div className="text-4xl font-bold">{slots.length}</div>
              <div className="text-blue-100 text-sm">Total Slots</div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
          <div className="bg-white rounded-xl shadow-md p-4 border-l-4 border-green-500">
            <div className="flex items-center justify-between">
              <div>
                <div className="text-2xl font-bold text-gray-900">
                  {availableSlots}
                </div>
                <div className="text-sm text-gray-600">Available</div>
              </div>
              <div className="p-3 bg-green-100 rounded-lg">
                <CheckCircle className="w-6 h-6 text-green-600" />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-md p-4 border-l-4 border-red-500">
            <div className="flex items-center justify-between">
              <div>
                <div className="text-2xl font-bold text-gray-900">
                  {outOfOrderSlots}
                </div>
                <div className="text-sm text-gray-600">Out of Order</div>
              </div>
              <div className="p-3 bg-red-100 rounded-lg">
                <XCircle className="w-6 h-6 text-red-600" />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-md p-4 border-l-4 border-yellow-500">
            <div className="flex items-center justify-between">
              <div>
                <div className="text-2xl font-bold text-gray-900">
                  {maintenanceSlots}
                </div>
                <div className="text-sm text-gray-600">Maintenance</div>
              </div>
              <div className="p-3 bg-yellow-100 rounded-lg">
                <AlertCircle className="w-6 h-6 text-yellow-600" />
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-md overflow-hidden">
          <div className="p-6 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
              <Battery className="w-5 h-5 text-blue-600" />
              Slot Management
            </h3>
          </div>

          {loading ? (
            <div className="flex justify-center items-center py-12">
              <Loading size="lg" color="blue" text="Loading slots..." />
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 p-6">
              {slots.map((slot) => {
                const statusInfo = getStatusInfo(slot.status);
                const isUpdating = updatingSlot === slot.slotId;
                const canUpdate =
                  slot.status !== "Charging" && slot.status !== "Booked";

                return (
                  <div
                    key={slot.slotId}
                    className={`border-2 rounded-xl p-5 transition-all hover:shadow-lg ${statusInfo.color}`}
                  >
                    <div className="flex items-center justify-between mb-4">
                      <div className="flex items-center gap-3">
                        <div className="flex items-center justify-center w-12 h-12 bg-blue-600 text-white rounded-lg font-bold text-lg">
                          {slot.number}
                        </div>
                        <div>
                          <div className="font-semibold text-gray-900">
                            Slot {slot.number}
                          </div>
                          <div className="text-sm text-gray-600">
                            {slot.connectorType}
                          </div>
                        </div>
                      </div>
                      <div
                        className={`flex items-center gap-2 px-3 py-1 rounded-full ${statusInfo.badgeColor} text-sm font-medium`}
                      >
                        {statusInfo.icon}
                      </div>
                    </div>

                    <div className="space-y-3">
                      <div className="text-sm">
                        <label className="block text-gray-700 font-medium mb-2">
                          Update Status
                        </label>
                        <select
                          value={slot.status}
                          onChange={(e) =>
                            handleUpdateSlotStatus(slot.slotId, e.target.value)
                          }
                          disabled={!canUpdate || isUpdating}
                          className={`w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all ${
                            !canUpdate || isUpdating
                              ? "bg-gray-100 cursor-not-allowed"
                              : "bg-white"
                          }`}
                        >
                          <option value="Available">Available</option>
                          <option value="Under Maintenance">
                            Under Maintenance
                          </option>
                          <option value="Out Of Order">Out Of Order</option>
                        </select>
                      </div>

                      {!canUpdate && (
                        <div className="text-xs text-gray-600 flex items-center gap-1 bg-gray-50 p-2 rounded">
                          <AlertCircle className="w-3 h-3" />
                          Cannot update while {slot.status.toLowerCase()}
                        </div>
                      )}

                      {isUpdating && (
                        <div className="flex items-center gap-2 text-xs text-blue-600">
                          <RefreshCw className="w-3 h-3 animate-spin" />
                          Updating...
                        </div>
                      )}

                      <div className="text-xs text-gray-500 pt-2 border-t border-gray-200">
                        Last updated: {new Date().toLocaleTimeString()}
                      </div>
                    </div>
                  </div>
                );
              })}

              {slots.length === 0 && (
                <div className="col-span-full text-center py-12">
                  <Battery className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                  <p className="text-gray-500">
                    No slots found for this station.
                  </p>
                </div>
              )}
            </div>
          )}
        </div>

        <div className="mt-6 bg-blue-50 border border-blue-200 rounded-xl p-6">
          <h3 className="text-sm font-semibold text-blue-900 mb-3 flex items-center gap-2">
            <AlertCircle className="w-4 h-4" />
            Status Guide
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 text-sm text-blue-800">
            <div className="flex items-start gap-2">
              <CheckCircle className="w-4 h-4 text-green-600 mt-0.5 flex-shrink-0" />
              <div>
                <strong className="block text-green-700">Available</strong>
                <span className="text-gray-600">Ready for booking</span>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <Zap className="w-4 h-4 text-purple-600 mt-0.5 flex-shrink-0" />
              <div>
                <strong className="block text-purple-700">Charging</strong>
                <span className="text-gray-600">Currently in use</span>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <AlertCircle className="w-4 h-4 text-yellow-600 mt-0.5 flex-shrink-0" />
              <div>
                <strong className="block text-yellow-700">
                  Under Maintenance
                </strong>
                <span className="text-gray-600">Temporarily unavailable</span>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <XCircle className="w-4 h-4 text-red-600 mt-0.5 flex-shrink-0" />
              <div>
                <strong className="block text-red-700">Out Of Order</strong>
                <span className="text-gray-600">Requires repair</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OperatorSlotManagement;
