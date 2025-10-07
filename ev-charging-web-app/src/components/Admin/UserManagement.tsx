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
  stationId?: string;
  stationName?: string;
  stationLocation?: string;
}

interface Station {
  stationId: string;
  name: string;
  location: string;
  type: string;
  capacity: number;
  availableSlots: number;
  isActive: boolean;
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
  stationId?: string;
  stationName?: string;
  stationLocation?: string;
}

interface CreateOperatorForm {
  fullName: string;
  email: string;
  password: string;
  stationId: string;
  stationName: string;
  stationLocation: string;
}

// Pagination interface
interface PaginationInfo {
  page: number;
  pageSize: number;
  totalCount: number;
  totalPages: number;
}

function UserManagement() {
  const navigate = useNavigate();
  const [reactivationRequests, setReactivationRequests] = useState<EVOwner[]>(
    []
  );
  const [operators, setOperators] = useState<Operator[]>([]);
  const [owners, setOwners] = useState<EVOwner[]>([]);
  const [stations, setStations] = useState<Station[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");
  const [activeTab, setActiveTab] = useState<
    "operators" | "owners" | "reactivations"
  >("operators");
  const [selectedUser, setSelectedUser] = useState<UserDetails | null>(null);
  const [showUserDetails, setShowUserDetails] = useState<boolean>(false);
  const [showCreateOperator, setShowCreateOperator] = useState<boolean>(false);
  const [creatingOperator, setCreatingOperator] = useState<boolean>(false);

  // Pagination states
  const [operatorsPagination, setOperatorsPagination] =
    useState<PaginationInfo>({
      page: 1,
      pageSize: 10,
      totalCount: 0,
      totalPages: 0,
    });

  const [ownersPagination, setOwnersPagination] = useState<PaginationInfo>({
    page: 1,
    pageSize: 10,
    totalCount: 0,
    totalPages: 0,
  });

  const [operatorForm, setOperatorForm] = useState<CreateOperatorForm>({
    fullName: "",
    email: "",
    password: "",
    stationId: "",
    stationName: "",
    stationLocation: "",
  });

  // Fetch all data on component mount
  useEffect(() => {
    fetchUserData();
    fetchStations();
  }, []);

  // Fetch data when pagination changes
  useEffect(() => {
    if (activeTab === "operators") {
      fetchOperators();
    } else if (activeTab === "owners") {
      fetchOwners();
    }
  }, [activeTab, operatorsPagination.page, ownersPagination.page]);

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

      // Fetch initial operators and owners
      await fetchOperators();
      await fetchOwners();
    } catch (err) {
      const errorMessage = "Failed to fetch user data";
      setError(errorMessage);
      console.error("Error fetching user data:", err);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const fetchOperators = async (): Promise<void> => {
    try {
      const { page, pageSize } = operatorsPagination;

      console.log(`Fetching operators: page=${page}, pageSize=${pageSize}`);
      const response = await api.get(
        `/users?role=Operator&page=${page}&pageSize=${pageSize}`
      );
      console.log("Operators API Response:", response);

      if (response?.data) {
        let operatorsData = [];
        let totalCount = 0;

        // The response is an array of operators for the current page
        if (Array.isArray(response.data)) {
          operatorsData = response.data;
          // For now, we don't have the total count, so we'll use a fallback
          totalCount = response.data.length;
        }
        // If the backend returns a paginated response object
        else if (response.data.items && Array.isArray(response.data.items)) {
          operatorsData = response.data.items;
          totalCount = response.data.totalCount || response.data.total || 0;
        } else if (response.data.users && Array.isArray(response.data.users)) {
          operatorsData = response.data.users;
          totalCount = response.data.totalCount || response.data.total || 0;
        } else {
          operatorsData = response.data;
          totalCount = operatorsData.length;
        }

        // Fetch station information for each operator
        const operatorsWithStations = await Promise.all(
          operatorsData.map(async (operator: Operator) => {
            if (operator.stationId) {
              try {
                const stationResponse = await api.get(
                  `/station/${operator.stationId}`
                );
                if (stationResponse?.data) {
                  return {
                    ...operator,
                    stationName: stationResponse.data.name,
                    stationLocation: stationResponse.data.location,
                  };
                }
              } catch (error) {
                console.error(
                  `Error fetching station for operator ${
                    operator._id || operator.id
                  }:`,
                  error
                );
              }
            }
            return operator;
          })
        );

        console.log(
          `Processed ${operatorsWithStations.length} operators, total count: ${totalCount}`
        );

        setOperators(operatorsWithStations);

        // If we don't have a proper total count from backend, we need to estimate
        if (totalCount === 0 || totalCount === operatorsWithStations.length) {
          if (operatorsWithStations.length === pageSize) {
            totalCount = page * pageSize + 1;
          } else {
            totalCount = (page - 1) * pageSize + operatorsWithStations.length;
          }
        }

        setOperatorsPagination((prev) => ({
          ...prev,
          totalCount: totalCount,
          totalPages: Math.ceil(totalCount / pageSize),
        }));
      }
    } catch (err: any) {
      console.error("Error fetching operators:", err);
      console.error("Error response:", err.response?.data);
      toast.error(err?.response?.data?.message || "Failed to fetch operators");
    }
  };

  const fetchOwners = async (): Promise<void> => {
    try {
      const { page, pageSize } = ownersPagination;

      console.log(`Fetching owners: page=${page}, pageSize=${pageSize}`);
      const response = await api.get(
        `/users?role=Owner&page=${page}&pageSize=${pageSize}`
      );
      console.log("Owners API Response:", response);

      if (response?.data) {
        let ownersData = [];
        let totalCount = 0;

        // The response is an array of owners for the current page
        if (Array.isArray(response.data)) {
          ownersData = response.data;
          // For now, we don't have the total count, so we'll use a fallback
          totalCount = ownersData.length; // This is only the current page count
        }
        // If the backend returns a paginated response object
        else if (response.data.items && Array.isArray(response.data.items)) {
          ownersData = response.data.items;
          totalCount = response.data.totalCount || response.data.total || 0;
        } else if (
          response.data.Owners &&
          Array.isArray(response.data.Owners)
        ) {
          ownersData = response.data.Owners;
          totalCount = response.data.totalCount || response.data.total || 0;
        } else {
          ownersData = response.data;
          totalCount = ownersData.length;
        }

        console.log(
          `Processed ${ownersData.length} owners, total count: ${totalCount}`
        );

        setOwners(ownersData);

        // If we don't have a proper total count from backend, we need to estimate
        if (totalCount === 0 || totalCount === ownersData.length) {
          // Estimate total count based on current page and data length
          if (ownersData.length === pageSize) {
            // If we got a full page, assume there are more items
            totalCount = page * pageSize + 1; // At least one more item
          } else {
            // This is the last page or only page
            totalCount = (page - 1) * pageSize + ownersData.length;
          }
        }

        setOwnersPagination((prev) => ({
          ...prev,
          totalCount: totalCount,
          totalPages: Math.ceil(totalCount / pageSize),
        }));
      }
    } catch (err: any) {
      console.error("Error fetching owners:", err);
      console.error("Error response:", err.response?.data);
      toast.error(err?.response?.data?.message || "Failed to fetch owners");
    }
  };

  const fetchStations = async (): Promise<void> => {
    try {
      const response = await api.get("/station");
      if (response?.data) {
        setStations(response.data);
      }
    } catch (err) {
      console.error("Error fetching stations:", err);
      toast.error("Failed to fetch stations");
    }
  };

  // Pagination handlers
  const handleOperatorsPageChange = (newPage: number): void => {
    setOperatorsPagination((prev) => ({ ...prev, page: newPage }));
  };

  const handleOwnersPageChange = (newPage: number): void => {
    setOwnersPagination((prev) => ({ ...prev, page: newPage }));
  };

  const handleStationChange = (stationId: string) => {
    const selectedStation = stations.find(
      (station) => station.stationId === stationId
    );
    if (selectedStation) {
      setOperatorForm({
        ...operatorForm,
        stationId: selectedStation.stationId,
        stationName: selectedStation.name,
        stationLocation: selectedStation.location,
      });
    }
  };

  const handleCreateOperator = async (): Promise<void> => {
    if (
      !operatorForm.fullName ||
      !operatorForm.email ||
      !operatorForm.password ||
      !operatorForm.stationId
    ) {
      toast.error("Please fill all required fields");
      return;
    }

    try {
      setCreatingOperator(true);
      const result = await api.post("/operators", operatorForm);

      if (result?.status === 200 || result?.status === 201) {
        toast.success("Operator created successfully");
        setShowCreateOperator(false);
        setOperatorForm({
          fullName: "",
          email: "",
          password: "",
          stationId: "",
          stationName: "",
          stationLocation: "",
        });

        // Refresh operators list and reset to first page
        setOperatorsPagination((prev) => ({ ...prev, page: 1 }));
        await fetchOperators();

        // Also switch to operators tab to show the new operator
        setActiveTab("operators");
      } else {
        toast.error(result?.data?.message || "Failed to create operator");
      }
    } catch (err: any) {
      const errorMessage =
        err?.response?.data?.message || "Error creating operator";
      toast.error(errorMessage);
      console.error("Error creating operator:", err);
    } finally {
      setCreatingOperator(false);
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
        // Refresh current tab data
        if (isOwner) {
          await fetchOwners();
        } else {
          await fetchOperators();
        }
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
        // Refresh current tab data
        if (isOwner) {
          await fetchOwners();
        } else {
          await fetchOperators();
        }
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
          // Use the operator-specific endpoint instead of users endpoint
          const result = await api.get(`/operators/${userId}`);

          if (result?.data) {
            setSelectedUser({ ...result.data, role: "Operator" });
            setShowUserDetails(true);
          }
        }
      }
    } catch (err: any) {
      console.error("Error fetching user details:", err);

      // Fallback: Use the available data without making API call
      if (!isOwner) {
        const operator = user as Operator;
        setSelectedUser({
          id: operator._id || operator.id,
          fullName: getUserDisplayName(operator),
          email: getUserEmail(operator),
          role: "Operator",
          isActive: getUserStatus(operator),
          stationId: operator.stationId,
          stationName: operator.stationName,
          stationLocation: operator.stationLocation,
          createdAt: operator.CreatedAt,
        });
        setShowUserDetails(true);
      } else {
        toast.error(
          err?.response?.data?.message || "Failed to fetch user details"
        );
      }
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

  const getStationInfo = (user: EVOwner | Operator | UserDetails): string => {
    if ("role" in user && user.role === "Operator") {
      // For UserDetails with role Operator
      const operator = user as UserDetails;
      if (operator.stationName && operator.stationLocation) {
        return `${operator.stationName} - ${operator.stationLocation}`;
      }
      if (operator.stationName) {
        return operator.stationName;
      }
      if (operator.stationLocation) {
        return operator.stationLocation;
      }
      return "No station assigned";
    } else if ("stationName" in user || "stationLocation" in user) {
      // For Operator type
      const operator = user as Operator;
      if (operator.stationName && operator.stationLocation) {
        return `${operator.stationName} - ${operator.stationLocation}`;
      }
      if (operator.stationName) {
        return operator.stationName;
      }
      if (operator.stationLocation) {
        return operator.stationLocation;
      }
      return "No station assigned";
    }
    return "N/A";
  };

  // Pagination component
  const Pagination = ({
    pagination,
    onPageChange,
    type,
  }: {
    pagination: PaginationInfo;
    onPageChange: (page: number) => void;
    type: "operators" | "owners";
  }) => {
    const { page, totalPages, totalCount } = pagination;

    if (totalPages <= 1) return null;

    // Generate page numbers to show
    const getPageNumbers = () => {
      const pages = [];
      const maxVisiblePages = 5;

      let startPage = Math.max(1, page - Math.floor(maxVisiblePages / 2));
      let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

      // Adjust if we're near the end
      if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(1, endPage - maxVisiblePages + 1);
      }

      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }

      return pages;
    };

    return (
      <div className="flex flex-col sm:flex-row justify-between items-center space-y-2 sm:space-y-0 mt-6 p-4 bg-gray-50 rounded-lg">
        <div className="text-sm text-gray-600">
          Showing {(pagination.page - 1) * pagination.pageSize + 1} to{" "}
          {Math.min(pagination.page * pagination.pageSize, totalCount)} of{" "}
          {totalCount} {type}
        </div>

        <div className="flex items-center space-x-1">
          <button
            onClick={() => onPageChange(page - 1)}
            disabled={page <= 1}
            className="px-3 py-2 border border-gray-300 rounded-md text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors"
          >
            Previous
          </button>

          {getPageNumbers().map((pageNum) => (
            <button
              key={pageNum}
              onClick={() => onPageChange(pageNum)}
              className={`px-3 py-2 border text-sm font-medium transition-colors ${
                pageNum === page
                  ? "bg-green-600 text-white border-green-600"
                  : "border-gray-300 text-gray-500 hover:bg-gray-50"
              }`}
            >
              {pageNum}
            </button>
          ))}

          <button
            onClick={() => onPageChange(page + 1)}
            disabled={page >= totalPages}
            className="px-3 py-2 border border-gray-300 rounded-md text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors"
          >
            Next
          </button>
        </div>

        <div className="text-sm text-gray-500">
          Page {page} of {totalPages}
        </div>
      </div>
    );
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
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-800">User Management</h2>
        <button
          onClick={() => setShowCreateOperator(true)}
          className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors"
        >
          Create Operator
        </button>
      </div>

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
            onClick={() => {
              setActiveTab("operators");
              setOperatorsPagination((prev) => ({ ...prev, page: 1 }));
            }}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === "operators"
                ? "border-green-500 text-green-600"
                : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
            } transition-colors`}
          >
            Operators ({operatorsPagination.totalCount})
          </button>
          <button
            onClick={() => {
              setActiveTab("owners");
              setOwnersPagination((prev) => ({ ...prev, page: 1 }));
            }}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === "owners"
                ? "border-green-500 text-green-600"
                : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
            } transition-colors`}
          >
            Owners ({ownersPagination.totalCount})
          </button>
        </nav>
      </div>

      {/* Operators Tab */}
      {activeTab === "operators" && (
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <h3 className="text-lg font-semibold">Operators</h3>
            <span className="text-sm text-gray-600">
              Page {operatorsPagination.page} • Showing {operators.length}{" "}
              operators
            </span>
          </div>

          {operators.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-gray-600 text-lg">No operators found.</p>
            </div>
          ) : (
            <>
              <div className="grid gap-4">
                {operators.map((operator) => (
                  <div
                    key={operator._id || operator.id}
                    className="bg-white p-4 rounded-lg shadow border hover:shadow-md transition-shadow"
                  >
                    <div className="flex justify-between items-center">
                      <div className="flex-1">
                        {/* Operator Name - Main heading */}
                        <h4 className="font-semibold text-lg text-gray-800 mb-2">
                          {getUserDisplayName(operator)}
                        </h4>

                        {/* Details in 3 rows */}
                        <div className="space-y-1">
                          {/* Email row */}
                          <p className="text-sm text-gray-600">
                            <span className="font-medium">Email:</span>{" "}
                            {getUserEmail(operator)}
                          </p>

                          {/* Status row */}
                          <p className="text-sm text-gray-600">
                            <span className="font-medium">Status:</span>{" "}
                            <span
                              className={`ml-1 font-medium ${
                                getUserStatus(operator)
                                  ? "text-green-600"
                                  : "text-red-600"
                              }`}
                            >
                              {getUserStatus(operator) ? "Active" : "Inactive"}
                            </span>
                          </p>
                        </div>
                      </div>

                      {/* Action buttons */}
                      <div className="flex flex-col sm:flex-row gap-2 ml-4">
                        <button
                          onClick={() => handleViewUserDetails(operator, false)}
                          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 text-sm transition-colors flex items-center gap-1"
                        >
                          <span>Details</span>
                        </button>
                        {getUserStatus(operator) ? (
                          <button
                            onClick={() =>
                              handleDeactivateUser(
                                operator._id || operator.id || "",
                                false
                              )
                            }
                            className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 text-sm transition-colors flex items-center gap-1"
                          >
                            <span>Deactivate</span>
                          </button>
                        ) : (
                          <button
                            onClick={() =>
                              handleActivateUser(
                                operator._id || operator.id || "",
                                false
                              )
                            }
                            className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 text-sm transition-colors flex items-center gap-1"
                          >
                            <span>Activate</span>
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <Pagination
                pagination={operatorsPagination}
                onPageChange={handleOperatorsPageChange}
                type="operators"
              />
            </>
          )}
        </div>
      )}

      {/* Owners Tab */}
      {activeTab === "owners" && (
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <h3 className="text-lg font-semibold">EV Owners</h3>
            <span className="text-sm text-gray-600">
              Page {ownersPagination.page} • Showing {owners.length} owners
            </span>
          </div>

          {owners.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-gray-600 text-lg">No owners found.</p>
            </div>
          ) : (
            <>
              <div className="grid gap-4">
                {owners.map((owner) => (
                  <div
                    key={owner.nic}
                    className="bg-white p-4 rounded-lg shadow border hover:shadow-md transition-shadow"
                  >
                    <div className="flex justify-between items-center">
                      <div className="flex-1">
                        {/* Owner Name - Main heading */}
                        <h4 className="font-semibold text-lg text-gray-800 mb-2">
                          {owner.fullName}
                        </h4>

                        {/* Details in 3 rows */}
                        <div className="space-y-1">
                          {/* Email row */}
                          <p className="text-sm text-gray-600">
                            <span className="font-medium">Email:</span>{" "}
                            {owner.email}
                          </p>

                          {/* NIC row */}
                          <p className="text-sm text-gray-600">
                            <span className="font-medium">NIC:</span>{" "}
                            {owner.nic}
                          </p>

                          {/* Status row */}
                          <p className="text-sm text-gray-600">
                            <span className="font-medium">Status:</span>{" "}
                            <span
                              className={`ml-1 font-medium ${
                                owner.isActive
                                  ? "text-green-600"
                                  : "text-red-600"
                              }`}
                            >
                              {owner.isActive ? "Active" : "Inactive"}
                            </span>
                          </p>
                        </div>

                        {/* Reactivation Request Badge */}
                        {owner.reactivationRequested && (
                          <div className="mt-2">
                            <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                              Reactivation Requested
                            </span>
                          </div>
                        )}
                      </div>

                      {/* Action buttons */}
                      <div className="flex flex-col sm:flex-row gap-2 ml-4">
                        <button
                          onClick={() => handleViewUserDetails(owner, true)}
                          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 text-sm transition-colors flex items-center gap-1"
                        >
                          <span>Details</span>
                        </button>
                        {!owner.isActive && (
                          <button
                            onClick={() => handleActivateUser(owner.nic, true)}
                            className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 text-sm transition-colors flex items-center gap-1"
                          >
                            <span>Activate</span>
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <Pagination
                pagination={ownersPagination}
                onPageChange={handleOwnersPageChange}
                type="owners"
              />
            </>
          )}
        </div>
      )}
      {/* Create Operator Modal */}
      {showCreateOperator && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold">Create New Operator</h3>
              <button
                onClick={() => setShowCreateOperator(false)}
                className="text-gray-500 hover:text-gray-700 transition-colors"
              >
                ✕
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Full Name *
                </label>
                <input
                  type="text"
                  value={operatorForm.fullName}
                  onChange={(e) =>
                    setOperatorForm({
                      ...operatorForm,
                      fullName: e.target.value,
                    })
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
                  placeholder="Enter full name"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Email *
                </label>
                <input
                  type="email"
                  value={operatorForm.email}
                  onChange={(e) =>
                    setOperatorForm({ ...operatorForm, email: e.target.value })
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
                  placeholder="Enter email address"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Password *
                </label>
                <input
                  type="password"
                  value={operatorForm.password}
                  onChange={(e) =>
                    setOperatorForm({
                      ...operatorForm,
                      password: e.target.value,
                    })
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
                  placeholder="Enter password (min 6 characters)"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Station *
                </label>
                <select
                  value={operatorForm.stationId}
                  onChange={(e) => handleStationChange(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
                >
                  <option value="">Select a station</option>
                  {stations
                    .filter((station) => station.isActive)
                    .map((station) => (
                      <option key={station.stationId} value={station.stationId}>
                        {station.name} - {station.location}
                      </option>
                    ))}
                </select>
              </div>

              {operatorForm.stationId && (
                <div className="bg-gray-50 p-3 rounded-md">
                  <h4 className="font-semibold text-sm mb-2">
                    Selected Station:
                  </h4>
                  <p className="text-sm">
                    <strong>Name:</strong> {operatorForm.stationName}
                  </p>
                  <p className="text-sm">
                    <strong>Location:</strong> {operatorForm.stationLocation}
                  </p>
                </div>
              )}
            </div>

            <div className="mt-6 flex justify-end space-x-3">
              <button
                onClick={() => setShowCreateOperator(false)}
                className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 transition-colors"
                disabled={creatingOperator}
              >
                Cancel
              </button>
              <button
                onClick={handleCreateOperator}
                disabled={
                  creatingOperator ||
                  !operatorForm.fullName ||
                  !operatorForm.email ||
                  !operatorForm.password ||
                  !operatorForm.stationId
                }
                className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
              >
                {creatingOperator ? "Creating..." : "Create Operator"}
              </button>
            </div>
          </div>
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

              {selectedUser.role === "Operator" && (
                <>
                  <div>
                    <label className="font-semibold">Station:</label>
                    <p className="mt-1">{getStationInfo(selectedUser)}</p>
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
