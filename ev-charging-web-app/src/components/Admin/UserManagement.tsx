import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../common";
import toast from "react-hot-toast";

// Type definitions - Updated to match API response
interface EVOwner {
  nic: string;
  fullName: string;
  email: string;
  phone: string | null;
  isActive: boolean;
  reactivationRequested?: boolean;
  createdAt?: string;
}

interface Operator {
  _id: string;
  id?: string;
  name?: string;
  Name?: string;
  email: string;
  Email?: string;
  IsActive: boolean;
  isActive?: boolean;
  CreatedAt?: string;
}

interface UserDetails {
  nic?: string;
  NIC?: string;
  fullName?: string;
  FullName?: string;
  email?: string;
  Email?: string;
  phone?: string | null;
  Phone?: string;
  isActive?: boolean;
  IsActive?: boolean;
  reactivationRequested?: boolean;
  ReactivationRequested?: boolean;
  createdAt?: string;
  CreatedAt?: string;
  name?: string;
  Name?: string;
  _id?: string;
  id?: string;
  role: "Owner" | "Operator";
}

function UserManagement() {
  const navigate = useNavigate();
  const [reactivationRequests, setReactivationRequests] = useState<EVOwner[]>(
    []
  );
  const [operators, setOperators] = useState<Operator[]>([]);
  const [owners, setOwners] = useState<EVOwner[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");
  const [activeTab, setActiveTab] = useState<
    "operators" | "owners" | "reactivations"
  >("operators");
  const [selectedUser, setSelectedUser] = useState<UserDetails | null>(null);
  const [showUserDetails, setShowUserDetails] = useState<boolean>(false);

  // Fetch all data on component mount
  useEffect(() => {
    fetchUserData();
  }, []);

  const fetchUserData = async (): Promise<void> => {
    try {
      setLoading(true);
      setError("");

      // Fetch reactivation requests
      const reactivationResponse = await api.get(
        "/owners/reactivation-requests"
      );
      console.log("Reactivation requests:", reactivationResponse?.data);
      if (reactivationResponse?.data) {
        setReactivationRequests(reactivationResponse.data);
      }

      // Fetch operators
      const operatorsResponse = await api.get("/users?role=Operator");
      console.log("Operators:", operatorsResponse?.data);
      if (operatorsResponse?.data) {
        setOperators(operatorsResponse.data);
      }

      // Fetch owners
      const ownersResponse = await api.get("/users?role=Owner");
      console.log("Owners:", ownersResponse?.data);
      if (ownersResponse?.data) {
        setOwners(ownersResponse.data);
      }
    } catch (err) {
      const errorMessage = "Failed to fetch user data";
      setError(errorMessage);
      console.error("Error fetching user data:", err);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleActivateUser = async (
    id: string,
    isOwner: boolean = false
  ): Promise<void> => {
    try {
      const endpoint = isOwner
        ? `/owners/${id}/activate`
        : `/users/${id}/activate`;
      const result = await api.patch(endpoint);

      if (result?.status === 200) {
        toast.success("User activated successfully");
        await fetchUserData(); // Refresh data
      } else {
        toast.error(result?.data?.message || "Failed to activate user");
      }
    } catch (err: any) {
      toast.error(err?.response?.data?.message || "Error activating user");
      console.error("Error activating user:", err);
    }
  };

  const handleDeactivateUser = async (
    id: string,
    isOwner: boolean = false
  ): Promise<void> => {
    try {
      const endpoint = isOwner
        ? `/owners/${id}/deactivate`
        : `/users/${id}/deactivate`;
      const result = await api.patch(endpoint);

      if (result?.status === 200) {
        toast.success("User deactivated successfully");
        await fetchUserData(); // Refresh data
      } else {
        toast.error(result?.data?.message || "Failed to deactivate user");
      }
    } catch (err: any) {
      toast.error(err?.response?.data?.message || "Error deactivating user");
      console.error("Error deactivating user:", err);
    }
  };

  const handleClearReactivationRequest = async (nic: string): Promise<void> => {
    try {
      const result = await api.patch(`/owners/${nic}/clear-reactivation`);

      if (result?.status === 200) {
        toast.success("Reactivation request cleared successfully");
        await fetchUserData(); // Refresh data
      } else {
        toast.error(
          result?.data?.message || "Failed to clear reactivation request"
        );
      }
    } catch (err: any) {
      toast.error(
        err?.response?.data?.message || "Error clearing reactivation request"
      );
      console.error("Error clearing reactivation request:", err);
    }
  };

  const handleViewUserDetails = async (
    user: EVOwner | Operator,
    isOwner: boolean = false
  ): Promise<void> => {
    try {
      if (isOwner) {
        const owner = user as EVOwner;
        const result = await api.get(`/owners/${owner.nic}`);

        if (result?.data) {
          setSelectedUser({ ...result.data, role: "Owner" });
          setShowUserDetails(true);
        }
      } else {
        const operator = user as Operator;
        const userId = operator._id || operator.id;
        if (userId) {
          const result = await api.get(`/users/${userId}`);

          if (result?.data) {
            setSelectedUser({ ...result.data, role: "Operator" });
            setShowUserDetails(true);
          }
        }
      }
    } catch (err: any) {
      console.error("Error fetching user details:", err);
      toast.error(
        err?.response?.data?.message || "Failed to fetch user details"
      );
    }
  };

  // Helper functions that handle both uppercase and lowercase properties
  const getUserDisplayName = (
    user: EVOwner | Operator | UserDetails
  ): string => {
    if ("role" in user) {
      // UserDetails - handle both cases
      return (
        user.FullName ||
        user.fullName ||
        user.Name ||
        user.name ||
        user.email ||
        "Unknown User"
      );
    } else if ("nic" in user) {
      // EVOwner - use lowercase properties
      return user.fullName || "Unknown User";
    } else {
      // Operator
      return user.Name || user.name || user.email || "Unknown User";
    }
  };

  const getUserEmail = (user: EVOwner | Operator | UserDetails): string => {
    if ("role" in user) {
      return user.Email || user.email || "";
    } else if ("nic" in user) {
      return user.email;
    } else {
      return user.email;
    }
  };

  const getUserStatus = (user: EVOwner | Operator | UserDetails): boolean => {
    if ("role" in user) {
      return user.IsActive || user.isActive || false;
    } else if ("nic" in user) {
      return user.isActive;
    } else {
      return user.IsActive || user.isActive || false;
    }
  };

  const getUserPhone = (user: EVOwner | Operator | UserDetails): string => {
    if ("role" in user) {
      return user.Phone || user.phone || "N/A";
    } else if ("nic" in user) {
      return user.phone || "N/A";
    } else {
      return "N/A";
    }
  };

  const getUserNIC = (user: EVOwner | Operator | UserDetails): string => {
    if ("role" in user) {
      return user.NIC || user.nic || "N/A";
    } else if ("nic" in user) {
      return user.nic;
    } else {
      return "N/A";
    }
  };

  const getReactivationRequested = (
    user: EVOwner | Operator | UserDetails
  ): boolean => {
    if ("role" in user) {
      return user.ReactivationRequested || user.reactivationRequested || false;
    } else if ("nic" in user) {
      return user.reactivationRequested || false;
    } else {
      return false;
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Loading user data...</div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <h2 className="text-2xl font-bold text-gray-800">User Management</h2>

      {/* Reactivation Requests Section */}
      {reactivationRequests.length > 0 && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-semibold text-yellow-800">
              Reactivation Requests ({reactivationRequests.length})
            </h3>
            <button
              onClick={() => setActiveTab("reactivations")}
              className="bg-yellow-600 text-white px-4 py-2 rounded hover:bg-yellow-700 transition-colors"
            >
              View All
            </button>
          </div>

          {activeTab === "reactivations" ? (
            <div className="space-y-4">
              {reactivationRequests.map((request) => (
                <div
                  key={request.nic}
                  className="bg-white p-4 rounded border shadow-sm"
                >
                  <div className="flex justify-between items-center">
                    <div>
                      <h4 className="font-semibold">{request.fullName}</h4>
                      <p className="text-sm text-gray-600">
                        NIC: {request.nic}
                      </p>
                      <p className="text-sm text-gray-600">
                        Email: {request.email}
                      </p>
                      <p className="text-sm text-gray-600">
                        Phone: {request.phone || "N/A"}
                      </p>
                    </div>
                    <div className="flex flex-col sm:flex-row gap-2">
                      <button
                        onClick={() => handleActivateUser(request.nic, true)}
                        className="bg-green-600 text-white px-3 py-2 rounded hover:bg-green-700 text-sm transition-colors"
                      >
                        Activate
                      </button>
                      <button
                        onClick={() =>
                          handleClearReactivationRequest(request.nic)
                        }
                        className="bg-gray-600 text-white px-3 py-2 rounded hover:bg-gray-700 text-sm transition-colors"
                      >
                        Clear Request
                      </button>
                      <button
                        onClick={() => handleViewUserDetails(request, true)}
                        className="bg-blue-600 text-white px-3 py-2 rounded hover:bg-blue-700 text-sm transition-colors"
                      >
                        View Details
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-yellow-700">
              {reactivationRequests.length} user(s) have requested account
              reactivation.
            </p>
          )}
        </div>
      )}

      {/* Tab Navigation */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab("operators")}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === "operators"
                ? "border-green-500 text-green-600"
                : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
            } transition-colors`}
          >
            Operators ({operators.length})
          </button>
          <button
            onClick={() => setActiveTab("owners")}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === "owners"
                ? "border-green-500 text-green-600"
                : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
            } transition-colors`}
          >
            Owners ({owners.length})
          </button>
        </nav>
      </div>

      {/* Operators Tab */}
      {activeTab === "operators" && (
        <div className="space-y-4">
          <h3 className="text-lg font-semibold">Operators</h3>
          {operators.length === 0 ? (
            <p className="text-gray-600">No operators found.</p>
          ) : (
            <div className="grid gap-4">
              {operators.map((operator) => (
                <div
                  key={operator._id || operator.id}
                  className="bg-white p-4 rounded-lg shadow border"
                >
                  <div className="flex justify-between items-center">
                    <div>
                      <h4 className="font-semibold">
                        {getUserDisplayName(operator)}
                      </h4>
                      <p className="text-sm text-gray-600">
                        Email: {getUserEmail(operator)}
                      </p>
                      <p className="text-sm text-gray-600">
                        Status:{" "}
                        <span
                          className={`ml-1 ${
                            getUserStatus(operator)
                              ? "text-green-600"
                              : "text-red-600"
                          }`}
                        >
                          {getUserStatus(operator) ? "Active" : "Inactive"}
                        </span>
                      </p>
                    </div>
                    <div className="flex flex-col sm:flex-row gap-2">
                      <button
                        onClick={() => handleViewUserDetails(operator, false)}
                        className="bg-blue-600 text-white px-3 py-2 rounded hover:bg-blue-700 text-sm transition-colors"
                      >
                        View Details
                      </button>
                      {getUserStatus(operator) ? (
                        <button
                          onClick={() =>
                            handleDeactivateUser(
                              operator._id || operator.id || "",
                              false
                            )
                          }
                          className="bg-red-600 text-white px-3 py-2 rounded hover:bg-red-700 text-sm transition-colors"
                        >
                          Deactivate
                        </button>
                      ) : (
                        <button
                          onClick={() =>
                            handleActivateUser(
                              operator._id || operator.id || "",
                              false
                            )
                          }
                          className="bg-green-600 text-white px-3 py-2 rounded hover:bg-green-700 text-sm transition-colors"
                        >
                          Activate
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Owners Tab */}
      {activeTab === "owners" && (
        <div className="space-y-4">
          <h3 className="text-lg font-semibold">EV Owners</h3>
          {owners.length === 0 ? (
            <p className="text-gray-600">No owners found.</p>
          ) : (
            <div className="grid gap-4">
              {owners.map((owner) => (
                <div
                  key={owner.nic}
                  className="bg-white p-4 rounded-lg shadow border"
                >
                  <div className="flex justify-between items-center">
                    <div>
                      <h4 className="font-semibold">{owner.fullName}</h4>
                      <p className="text-sm text-gray-600">NIC: {owner.nic}</p>
                      <p className="text-sm text-gray-600">
                        Status:{" "}
                        <span
                          className={`ml-1 ${
                            owner.isActive ? "text-green-600" : "text-red-600"
                          }`}
                        >
                          {owner.isActive ? "Active" : "Inactive"}
                        </span>
                      </p>
                      {owner.reactivationRequested && (
                        <p className="text-sm text-yellow-600 font-semibold">
                          Reactivation Requested
                        </p>
                      )}
                    </div>
                    <div className="flex flex-col sm:flex-row gap-2">
                      <button
                        onClick={() => handleViewUserDetails(owner, true)}
                        className="bg-blue-600 text-white px-3 py-2 rounded hover:bg-blue-700 text-sm transition-colors"
                      >
                        View Details
                      </button>
                      {!owner.isActive && (
                        <button
                          onClick={() => handleActivateUser(owner.nic, true)}
                          className="bg-green-600 text-white px-3 py-2 rounded hover:bg-green-700 text-sm transition-colors"
                        >
                          Activate
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* User Details Modal */}
      {showUserDetails && selectedUser && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold">User Details</h3>
              <button
                onClick={() => setShowUserDetails(false)}
                className="text-gray-500 hover:text-gray-700 transition-colors"
              >
                ✕
              </button>
            </div>

            <div className="space-y-3">
              <div>
                <label className="font-semibold">Name:</label>
                <p className="mt-1">{getUserDisplayName(selectedUser)}</p>
              </div>

              <div>
                <label className="font-semibold">Email:</label>
                <p className="mt-1">{getUserEmail(selectedUser)}</p>
              </div>

              {selectedUser.role === "Owner" && (
                <>
                  <div>
                    <label className="font-semibold">NIC:</label>
                    <p className="mt-1">{getUserNIC(selectedUser)}</p>
                  </div>
                  <div>
                    <label className="font-semibold">Phone:</label>
                    <p className="mt-1">{getUserPhone(selectedUser)}</p>
                  </div>
                </>
              )}

              <div>
                <label className="font-semibold">Role:</label>
                <p className="mt-1">{selectedUser.role}</p>
              </div>

              <div>
                <label className="font-semibold">Status:</label>
                <p
                  className={`mt-1 ${
                    getUserStatus(selectedUser)
                      ? "text-green-600"
                      : "text-red-600"
                  }`}
                >
                  {getUserStatus(selectedUser) ? "Active" : "Inactive"}
                </p>
              </div>

              {selectedUser.role === "Owner" &&
                getReactivationRequested(selectedUser) && (
                  <div>
                    <label className="font-semibold text-yellow-600">
                      Reactivation Requested:
                    </label>
                    <p className="mt-1">Yes</p>
                  </div>
                )}

              <div>
                <label className="font-semibold">Created At:</label>
                <p className="mt-1">
                  {selectedUser.CreatedAt || selectedUser.createdAt
                    ? new Date(
                        selectedUser.CreatedAt || selectedUser.createdAt || ""
                      ).toLocaleDateString()
                    : "N/A"}
                </p>
              </div>
            </div>

            <div className="mt-6 flex justify-end">
              <button
                onClick={() => setShowUserDetails(false)}
                className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-red-800">{error}</p>
        </div>
      )}
    </div>
  );
}

export default UserManagement;
