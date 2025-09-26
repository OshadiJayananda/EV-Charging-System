import { useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { toast } from "react-hot-toast";
import { postRequest } from "../components/common/api";

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [password, setPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const token = searchParams.get("token");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      const response = await postRequest("/auth/reset-password", {
        resetToken: token,
        newPassword: password,
      });

      if (response?.status === 200) {
        toast.success("Password reset successful!");
        navigate("/login");
      } else {
        toast.error("Failed to reset password.");
      }
    } catch {
      toast.error("Error resetting password.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!token) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="bg-white p-8 rounded-xl shadow-lg">
          <h2 className="text-xl font-bold text-red-600 mb-4">Invalid Link</h2>
          <p className="text-gray-600">
            Password reset link is invalid or expired.
          </p>

          <div className="mt-4">
            <button
              onClick={() => navigate("/login")}
              className="text-sm text-green-600 hover:text-green-500"
            >
              Back to Login
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-50 to-emerald-100">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-8 rounded-xl shadow-lg w-full max-w-md"
      >
        <h2 className="text-2xl font-bold mb-4 text-green-700">
          Reset Password
        </h2>
        <input
          type="password"
          className="w-full border rounded px-3 py-2 mb-4"
          placeholder="New password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full bg-green-600 text-white py-2 rounded font-semibold hover:bg-green-700 transition"
        >
          {isSubmitting ? "Resetting..." : "Reset Password"}
        </button>
      </form>
    </div>
  );
}
