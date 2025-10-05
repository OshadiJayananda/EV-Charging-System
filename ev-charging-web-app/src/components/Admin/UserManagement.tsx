import { useEffect, useState } from "react";
import { api } from "../common";
import toast from "react-hot-toast";

// Matches EVOwnerDto in backend
interface EVOwnerDto {
  nic: string;
  fullName: string;
  email: string;
  phone: string;
  isActive: boolean;
  reactivationRequested: boolean;
  createdAt: string;
}

interface OperatorDto {
  id: string;
  fullName: string;
  email: string;
  isActive: boolean;
}

function UserManagement() {
  const [owners, setOwners] = useState<EVOwnerDto[]>([]);
  const [operators, setOperators] = useState<OperatorDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [reactivationCount, setReactivationCount] = useState(0);
  const [selectedOwner, setSelectedOwner] = useState<EVOwnerDto | null>(null);
  const [selectedOperator, setSelectedOperator] = useState<OperatorDto | null>(
    null
  );

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const [ownersRes, operatorsRes, countRes] = await Promise.all([
          api.get("/users?role=Owner", {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          }),
          api.get("/users?role=Operator", {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          }),
          api.get("/owners/reactivation-count", {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          }),
        ]);
        setOwners(ownersRes.data);
        setOperators(operatorsRes.data);
        setReactivationCount(countRes.data.count);
      } catch (error) {
        console.error("Error fetching users:", error);
        toast.error("Failed to load users.");
      } finally {
        setLoading(false);
      }
    };
    fetchUsers();
  }, []);

  const handleActivate = async (nic: string) => {
    try {
      const res = await api.patch(`/owners/${nic}/activate`, null, {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
        validateStatus: () => true,
      });

      if (res.status === 200 || res.status === 204) {
        toast.success(`Owner ${nic} activated successfully!`);
        setOwners((prev) =>
          prev.map((o) =>
            o.nic === nic
              ? { ...o, isActive: true, reactivationRequested: false }
              : o
          )
        );
        setReactivationCount((prev) => Math.max(0, prev - 1));
      } else if (
        res.status === 400 &&
        res.data?.message?.includes("already active")
      ) {
        toast("Owner is already active.", { icon: "⚠️" });
        setOwners((prev) =>
          prev.map((o) => (o.nic === nic ? { ...o, isActive: true } : o))
        );
      } else if (res.status === 404) {
        toast.error("Owner not found.");
      } else {
        toast.error(
          `Activation failed: ${res.data?.message || "Unknown error"}`
        );
      }
    } catch (err) {
      console.error("Activation failed:", err);
      toast.error("Unexpected error while activating owner.");
    }
  };

  if (loading) return <p className="p-4">Loading users...</p>;

  return (
    <div className="p-6 space-y-8">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-800">User Management</h2>
        <div className="bg-yellow-100 border border-yellow-300 text-yellow-800 px-4 py-2 rounded-md">
          <strong>{reactivationCount}</strong> Pending Reactivation
          {reactivationCount !== 1 && "s"}
        </div>
      </div>

      <div>
        <h3 className="text-xl font-semibold mb-3">EV Owners</h3>
        <table className="min-w-full border border-gray-300 rounded-lg">
          <thead className="bg-gray-100">
            <tr>
              <th className="px-4 py-2 border text-left">Owner Name</th>
              <th className="px-4 py-2 border text-left">Status</th>
              <th className="px-4 py-2 border text-left">Reactivation</th>
              <th className="px-4 py-2 border text-left">Actions</th>
            </tr>
          </thead>
          <tbody>
            {owners.map((owner) => (
              <tr key={owner.nic} className="border-t hover:bg-gray-50">
                <td className="px-4 py-2 border">{owner.fullName}</td>
                <td
                  className={`px-4 py-2 border font-semibold ${
                    owner.isActive ? "text-green-600" : "text-red-500"
                  }`}
                >
                  {owner.isActive ? "Active" : "Deactivated"}
                </td>
                <td className="px-4 py-2 border text-center">
                  {owner.reactivationRequested ? (
                    <span className="text-yellow-600 font-semibold">
                      Requested
                    </span>
                  ) : (
                    "-"
                  )}
                </td>
                <td className="px-4 py-2 border space-x-2">
                  {!owner.isActive && (
                    <button
                      onClick={() => handleActivate(owner.nic)}
                      className="bg-green-600 text-white px-3 py-1 rounded hover:bg-green-700"
                    >
                      Activate
                    </button>
                  )}
                  <button
                    onClick={() => setSelectedOwner(owner)}
                    className="bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700"
                  >
                    View Details
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div>
        <h3 className="text-xl font-semibold mb-3">Station Operators</h3>
        <table className="min-w-full border border-gray-300 rounded-lg">
          <thead className="bg-gray-100">
            <tr>
              <th className="px-4 py-2 border text-left">Operator Name</th>
              <th className="px-4 py-2 border text-left">Status</th>
              <th className="px-4 py-2 border text-left">Action</th>
            </tr>
          </thead>
          <tbody>
            {operators.map((op) => (
              <tr key={op.id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-2 border">{op.fullName}</td>
                <td
                  className={`px-4 py-2 border font-semibold ${
                    op.isActive ? "text-green-600" : "text-red-500"
                  }`}
                >
                  {op.isActive ? "Active" : "Deactivated"}
                </td>
                <td className="px-4 py-2 border">
                  <button
                    onClick={() => setSelectedOperator(op)}
                    className="bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700"
                  >
                    View Details
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selectedOwner && (
        <div className="fixed inset-0 backdrop-blur-sm bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-lg shadow-lg p-6 w-96 relative">
            <h3 className="text-xl font-semibold mb-4 text-center">
              EV Owner Details
            </h3>
            <ul className="space-y-2 text-gray-700">
              <li>
                <strong>NIC:</strong> {selectedOwner.nic}
              </li>
              <li>
                <strong>Name:</strong> {selectedOwner.fullName}
              </li>
              <li>
                <strong>Email:</strong> {selectedOwner.email}
              </li>
              <li>
                <strong>Phone:</strong> {selectedOwner.phone}
              </li>
              <li>
                <strong>Status:</strong>{" "}
                {selectedOwner.isActive ? "Active" : "Deactivated"}
              </li>
              <li>
                <strong>Reactivation Requested:</strong>{" "}
                {selectedOwner.reactivationRequested ? "Yes" : "No"}
              </li>
              <li>
                <strong>Created At:</strong>{" "}
                {new Date(selectedOwner.createdAt).toLocaleDateString()}
              </li>
            </ul>
            <div className="mt-6 flex justify-end">
              <button
                onClick={() => setSelectedOwner(null)}
                className="bg-gray-300 px-3 py-1 rounded hover:bg-gray-400"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {selectedOperator && (
        <div className="fixed inset-0 backdrop-blur-sm bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-lg shadow-lg p-6 w-96 relative">
            <h3 className="text-xl font-semibold mb-4 text-center">
              Operator Details
            </h3>
            <ul className="space-y-2 text-gray-700">
              <li>
                <strong>ID:</strong> {selectedOperator.id}
              </li>
              <li>
                <strong>Name:</strong> {selectedOperator.fullName}
              </li>
              <li>
                <strong>Email:</strong> {selectedOperator.email}
              </li>
              <li>
                <strong>Status:</strong>{" "}
                {selectedOperator.isActive ? "Active" : "Deactivated"}
              </li>
            </ul>
            <div className="mt-6 flex justify-end">
              <button
                onClick={() => setSelectedOperator(null)}
                className="bg-gray-300 px-3 py-1 rounded hover:bg-gray-400"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default UserManagement;
