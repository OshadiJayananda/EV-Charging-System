import { useEffect, useState } from "react";
import { getRequest, putRequest } from "../components/common/api";
import Loading from "../components/common/Loading";
import { toast } from "react-hot-toast";

export default function Profile() {
  const [user, setUser] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [editMode, setEditMode] = useState(false);
  const [form, setForm] = useState({ fullName: "", email: "" });
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    const fetchProfile = async () => {
      const response = await getRequest("/auth/me");
      setUser(response?.data || null);
      type UserResponse = {
        data?: {
          id?: string;
          fullName?: string;
          email?: string;
          role?: string;
          userType?: string;
          isActive?: boolean;
          createdAt?: string;
        };
        status?: number;
      };
      const typedResponse = response as UserResponse;
      localStorage.setItem("userId", typedResponse?.data?.id || "");
      setUser(response?.data || null);
      setForm({
        fullName: typedResponse?.data?.fullName || "",
        email: typedResponse?.data?.email || "",
      });
      setLoading(false);
    };
    fetchProfile();
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSave = async () => {
    console.log("Saving profile...", form);
    const userId = localStorage.getItem("userId");
    if (!userId) {
      toast.error("User ID not found. Cannot update profile.");
      return;
    }
    setIsSaving(true);
    const response = await putRequest(`/users/${userId}`, {
      fullName: form.fullName,
      email: form.email,
    });
    if (response?.status === 200) {
      toast.success("Profile updated!");
      setUser({ ...user, fullName: form.fullName, email: form.email });
      setEditMode(false);
    } else {
      toast.error("Failed to update profile.");
    }
    setIsSaving(false);
  };

  let content;

  if (loading) {
    content = <Loading text="Loading profile..." />;
  } else if (user) {
    content = (
      <>
        <div className="mb-4 flex flex-col items-center">
          <div className="w-20 h-20 rounded-full bg-green-100 flex items-center justify-center mb-2">
            <span className="text-4xl font-bold text-green-700">
              {user.fullName ? user.fullName[0] : "?"}
            </span>
          </div>
          {editMode ? (
            <>
              <input
                name="fullName"
                value={form.fullName}
                onChange={handleChange}
                className="text-lg font-semibold text-green-800 mb-2 border rounded px-2 py-1 w-full text-center"
              />
              <input
                name="email"
                value={form.email}
                onChange={handleChange}
                className="text-sm text-gray-500 mb-2 border rounded px-2 py-1 w-full text-center"
              />
            </>
          ) : (
            <>
              <div className="text-lg font-semibold text-green-800">
                {user.fullName}
              </div>
              <div className="text-sm text-gray-500">{user.email}</div>
            </>
          )}
        </div>
        <div className="mb-2">
          <span className="font-semibold">Role:</span>{" "}
          {user.role || user.userType}
        </div>
        <div className="mb-2">
          <span className="font-semibold">User ID:</span> {user.id}
        </div>
        <div className="mb-2">
          <span className="font-semibold">Active:</span>{" "}
          {user.isActive ? (
            <span className="text-green-600 font-bold">Active</span>
          ) : (
            <span className="text-red-600 font-bold">Inactive</span>
          )}
        </div>
        <div className="mb-2">
          <span className="font-semibold">Created At:</span>{" "}
          {user.createdAt ? new Date(user.createdAt).toLocaleString() : "N/A"}
        </div>
        <div className="mt-4 flex gap-2">
          {editMode ? (
            <>
              <button
                onClick={handleSave}
                disabled={isSaving}
                className="bg-green-600 text-white px-4 py-2 rounded font-semibold hover:bg-green-700 transition"
              >
                {isSaving ? "Saving..." : "Save"}
              </button>
              <button
                onClick={() => {
                  setEditMode(false);
                  setForm({
                    fullName: user.fullName,
                    email: user.email,
                  });
                }}
                className="bg-gray-200 text-gray-700 px-4 py-2 rounded font-semibold hover:bg-gray-300 transition"
              >
                Cancel
              </button>
            </>
          ) : (
            <button
              onClick={() => setEditMode(true)}
              className="bg-green-600 text-white px-4 py-2 rounded font-semibold hover:bg-green-700 transition"
            >
              Edit Profile
            </button>
          )}
        </div>
      </>
    );
  } else {
    content = <div className="text-red-500">Failed to load profile.</div>;
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-50 to-emerald-100">
      <div className="bg-white p-8 rounded-xl shadow-lg w-full max-w-md">
        <h2 className="text-2xl font-bold mb-4 text-green-700">Profile</h2>
        {content}
      </div>
    </div>
  );
}
