import { useState } from "react";
import { toast } from "react-hot-toast";
import { postRequest } from "../components/common/api";
import { Navigate, useNavigate } from "react-router-dom";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const validateEmail = (value: string): boolean => {
    if (!value) {
      setError("Email is required");
      return false;
    } else if (!/\S+@\S+\.\S+/.test(value)) {
      setError("Email is invalid");
      return false;
    }
    setError(null);
    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateEmail(email)) return;

    setIsSubmitting(true);
    try {
      const response = await postRequest("/auth/forgot-password", { email });
      if (response?.status === 200) {
        toast.success("If an account exists, a reset link has been sent.");
      } else {
        toast.error("Failed to send reset link.");
      }
    } catch {
      toast.error("Error sending reset link.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-50 to-emerald-100">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-8 rounded-xl shadow-lg w-full max-w-md"
        noValidate
      >
        <h2 className="text-2xl font-bold mb-4 text-green-700">
          Forgot Password
        </h2>
        <p className="mb-6 text-gray-600">
          Enter your email address and we'll send you a password reset link.
        </p>
        <input
          type="email"
          className={`w-full border rounded px-3 py-2 mb-1 ${
            error ? "border-red-500" : ""
          }`}
          placeholder="you@example.com"
          value={email}
          onChange={(e) => {
            setEmail(e.target.value);
            if (error) validateEmail(e.target.value);
          }}
        />
        {error && <p className="text-red-500 text-sm mb-3">{error}</p>}
        <button
          type="submit"
          disabled={isSubmitting}
          className="mt-4 w-full bg-green-600 text-white py-2 rounded font-semibold hover:bg-green-700 transition"
        >
          {isSubmitting ? "Sending..." : "Send Reset Link"}
        </button>

        <div className="mt-2 text-center">
          <button
            type="button"
            onClick={() => navigate("/login")}
            className="w-full py-2 rounded border text-sm text-green-600 hover:text-green-500"
          >
            Back to Login
          </button>
        </div>
      </form>
    </div>
  );
}
