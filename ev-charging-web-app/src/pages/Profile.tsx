import { useEffect, useState } from "react";
import { getRequest, putRequest } from "../components/common/api";
import Loading from "../components/common/Loading";
import { toast } from "react-hot-toast";

// Define TypeScript interfaces
interface User {
  id: string;
  fullName: string;
  email: string;
  role: string;
  userType?: string;
  isActive: boolean;
  createdAt: string;
  stationId?: string;
  stationName?: string;
  stationLocation?: string;
}

interface UserForm {
  fullName: string;
  email: string;
  stationName: string;
  stationLocation: string;
}

interface ApiResponse {
  data?: User;
  status?: number;
}

export default function Profile() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [editMode, setEditMode] = useState(false);
  const [form, setForm] = useState<UserForm>({
    fullName: "",
    email: "",
    stationName: "",
    stationLocation: "",
  });
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = (await getRequest("/auth/me")) as ApiResponse;
        const userData = response?.data;

        if (userData) {
          localStorage.setItem("userId", userData.id || "");
          setUser(userData);
          setForm({
            fullName: userData.fullName || "",
            email: userData.email || "",
            stationName: userData.stationName || "",
            stationLocation: userData.stationLocation || "",
          });
        } else {
          throw new Error("No user data received");
        }
      } catch (error) {
        console.error("Error fetching profile:", error);
        toast.error("Failed to load profile");
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSave = async () => {
    const userId = localStorage.getItem("userId");
    if (!userId) {
      toast.error("User ID not found. Cannot update profile.");
      return;
    }

    // Validate form
    if (!form.fullName.trim() || !form.email.trim()) {
      toast.error("Please fill in all required fields");
      return;
    }

    setIsSaving(true);
    try {
      const response = await putRequest(`/users/${userId}`, {
        fullName: form.fullName,
        email: form.email,
      });

      if (response?.status === 200) {
        toast.success("Profile updated successfully!");
        setUser((prev) =>
          prev
            ? {
                ...prev,
                fullName: form.fullName,
                email: form.email,
              }
            : null
        );
        setEditMode(false);
      } else {
        throw new Error("Failed to update profile");
      }
    } catch (error) {
      console.error("Error updating profile:", error);
      toast.error("Failed to update profile. Please try again.");
    } finally {
      setIsSaving(false);
    }
  };

  const getRoleBadgeColor = (role: string) => {
    switch (role?.toLowerCase()) {
      case "admin":
        return "bg-purple-100 text-purple-800 border-purple-200";
      case "operator":
        return "bg-blue-100 text-blue-800 border-blue-200";
      default:
        return "bg-gray-100 text-gray-800 border-gray-200";
    }
  };

  const getInitials = (name: string) => {
    return name
      ? name
          .split(" ")
          .map((n) => n[0])
          .join("")
          .toUpperCase()
      : "U";
  };

  // Safe value getter with fallbacks
  const getUserValue = <K extends keyof User>(key: K): User[K] => {
    return user?.[key] ?? (key === "isActive" ? false : ("" as any));
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 to-blue-50">
        <Loading text="Loading your profile..." />
      </div>
    );
  }

  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 to-blue-50">
        <div className="bg-white p-8 rounded-2xl shadow-lg w-full max-w-md text-center">
          <div className="text-red-500 text-lg font-semibold mb-4">
            Failed to load profile
          </div>
          <button
            onClick={() => window.location.reload()}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg font-semibold hover:bg-blue-700 transition"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        {/* Header Section */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-800 mb-2">Profile</h1>
          <p className="text-gray-600">
            Manage your account information and preferences
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Column - Profile Card */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-2xl shadow-lg p-6 sticky top-8">
              <div className="text-center mb-6">
                <div className="w-24 h-24 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center mx-auto mb-4 shadow-lg">
                  <span className="text-2xl font-bold text-white">
                    {getInitials(getUserValue("fullName"))}
                  </span>
                </div>
                {editMode ? (
                  <input
                    name="fullName"
                    value={form.fullName}
                    onChange={handleChange}
                    className="text-xl font-bold text-gray-800 mb-2 border-2 border-blue-200 rounded-xl px-3 py-2 w-full text-center focus:border-blue-500 focus:outline-none transition"
                    placeholder="Enter your full name"
                  />
                ) : (
                  <h2 className="text-xl font-bold text-gray-800 mb-2">
                    {getUserValue("fullName")}
                  </h2>
                )}
                <div
                  className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border ${getRoleBadgeColor(
                    getUserValue("role")
                  )}`}
                >
                  {getUserValue("role") || getUserValue("userType") || "User"}
                </div>
              </div>

              <div className="space-y-3">
                <div className="flex items-center text-gray-600">
                  <svg
                    className="w-5 h-5 mr-3 text-gray-400"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                    />
                  </svg>
                  <span className="text-sm">User ID: {getUserValue("id")}</span>
                </div>

                <div className="flex items-center text-gray-600">
                  <svg
                    className="w-5 h-5 mr-3 text-gray-400"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                    />
                  </svg>
                  <span className="text-sm">
                    Joined{" "}
                    {getUserValue("createdAt")
                      ? new Date(getUserValue("createdAt")).toLocaleDateString()
                      : "N/A"}
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Right Column - Details and Actions */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
              <h3 className="text-xl font-semibold text-gray-800 mb-6 pb-2 border-b">
                Account Information
              </h3>

              <div className="space-y-6">
                {/* Email Field */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Email Address
                  </label>
                  {editMode ? (
                    <input
                      name="email"
                      type="email"
                      value={form.email}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:border-blue-500 focus:outline-none transition"
                      placeholder="Enter your email"
                    />
                  ) : (
                    <div className="px-4 py-3 bg-gray-50 rounded-xl text-gray-800">
                      {getUserValue("email")}
                    </div>
                  )}
                </div>

                {/* Station Information (for Operators) */}
                {(user.stationName || user.stationLocation) && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Station Information
                    </label>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <div className="text-xs text-gray-500 mb-1">
                          Station Name
                        </div>
                        <div className="px-4 py-3 bg-gray-50 rounded-xl text-gray-800">
                          {user.stationName || "Not assigned"}
                        </div>
                      </div>
                      <div>
                        <div className="text-xs text-gray-500 mb-1">
                          Location
                        </div>
                        <div className="px-4 py-3 bg-gray-50 rounded-xl text-gray-800">
                          {user.stationLocation || "Not specified"}
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Status */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Account Status
                  </label>
                  <div className="flex items-center">
                    <div
                      className={`w-3 h-3 rounded-full mr-3 ${
                        getUserValue("isActive") ? "bg-green-500" : "bg-red-500"
                      }`}
                    ></div>
                    <span
                      className={`font-semibold ${
                        getUserValue("isActive")
                          ? "text-green-600"
                          : "text-red-600"
                      }`}
                    >
                      {getUserValue("isActive") ? "Active" : "Inactive"}
                    </span>
                    {!getUserValue("isActive") && (
                      <button className="ml-4 text-sm bg-yellow-100 text-yellow-800 px-3 py-1 rounded-lg hover:bg-yellow-200 transition">
                        Contact Admin to Activate
                      </button>
                    )}
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="mt-8 pt-6 border-t border-gray-200 flex flex-wrap gap-3">
                {editMode ? (
                  <>
                    <button
                      onClick={handleSave}
                      disabled={isSaving}
                      className="flex items-center bg-blue-600 text-white px-6 py-3 rounded-xl font-semibold hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {isSaving ? (
                        <>
                          <svg
                            className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
                            fill="none"
                            viewBox="0 0 24 24"
                          >
                            <circle
                              className="opacity-25"
                              cx="12"
                              cy="12"
                              r="10"
                              stroke="currentColor"
                              strokeWidth="4"
                            ></circle>
                            <path
                              className="opacity-75"
                              fill="currentColor"
                              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                            ></path>
                          </svg>
                          Saving...
                        </>
                      ) : (
                        <>
                          <svg
                            className="w-5 h-5 mr-2"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M5 13l4 4L19 7"
                            />
                          </svg>
                          Save Changes
                        </>
                      )}
                    </button>
                    <button
                      onClick={() => {
                        setEditMode(false);
                        setForm({
                          fullName: getUserValue("fullName"),
                          email: getUserValue("email"),
                          stationName: user.stationName || "",
                          stationLocation: user.stationLocation || "",
                        });
                      }}
                      className="bg-gray-200 text-gray-700 px-6 py-3 rounded-xl font-semibold hover:bg-gray-300 transition"
                    >
                      Cancel
                    </button>
                  </>
                ) : (
                  <>
                    <button
                      onClick={() => setEditMode(true)}
                      className="flex items-center bg-blue-600 text-white px-6 py-3 rounded-xl font-semibold hover:bg-blue-700 transition"
                    >
                      <svg
                        className="w-5 h-5 mr-2"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                        />
                      </svg>
                      Edit Profile
                    </button>
                    <button
                      onClick={() => {
                        toast.success("Password change feature coming soon!");
                      }}
                      className="flex items-center bg-green-600 text-white px-6 py-3 rounded-xl font-semibold hover:bg-green-700 transition"
                    >
                      <svg
                        className="w-5 h-5 mr-2"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                        />
                      </svg>
                      Change Password
                    </button>
                  </>
                )}
              </div>
            </div>

            {/* Quick Stats Section */}
            {/* <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-2xl p-6 text-white">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-blue-100 text-sm">Member Since</div>
                    <div className="text-2xl font-bold mt-1">
                      {getUserValue("createdAt")
                        ? new Date(getUserValue("createdAt")).getFullYear()
                        : "N/A"}
                    </div>
                  </div>
                  <svg
                    className="w-8 h-8 text-blue-200"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                    />
                  </svg>
                </div>
              </div>

              <div className="bg-gradient-to-br from-purple-500 to-purple-600 rounded-2xl p-6 text-white">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-purple-100 text-sm">Account Type</div>
                    <div className="text-2xl font-bold mt-1 capitalize">
                      {getUserValue("role")?.toLowerCase() ||
                        getUserValue("userType")?.toLowerCase() ||
                        "user"}
                    </div>
                  </div>
                  <svg
                    className="w-8 h-8 text-purple-200"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                    />
                  </svg>
                </div>
              </div>
            </div> */}
          </div>
        </div>
      </div>
    </div>
  );
}
