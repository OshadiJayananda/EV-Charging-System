import { useState } from "react";
import { toast } from "react-hot-toast";
import { postRequest } from "../components/common/api";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
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
      >
        <h2 className="text-2xl font-bold mb-4 text-green-700">
          Forgot Password
        </h2>
        <p className="mb-6 text-gray-600">
          Enter your email address and we'll send you a password reset link.
        </p>
        <input
          type="email"
          className="w-full border rounded px-3 py-2 mb-4"
          placeholder="you@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full bg-green-600 text-white py-2 rounded font-semibold hover:bg-green-700 transition"
        >
          {isSubmitting ? "Sending..." : "Send Reset Link"}
        </button>
      </form>
    </div>
  );
}
