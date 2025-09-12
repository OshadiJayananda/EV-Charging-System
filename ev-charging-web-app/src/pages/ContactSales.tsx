import { useState } from "react";
import {
  PhoneIcon,
  EnvelopeIcon,
  ChatBubbleLeftRightIcon,
  UserIcon,
  BuildingOfficeIcon,
} from "@heroicons/react/24/outline";
import { Car, Send, CheckCircle } from "lucide-react";
import { Input } from "../components/common";
import ContactInfo from "../components/Contact/ContactInfo";

interface FormData {
  name: string;
  email: string;
  company: string;
  phone: string;
  message: string;
  interest: string;
  employees: string;
}

export default function ContactSales() {
  const [formData, setFormData] = useState<FormData>({
    name: "",
    email: "",
    company: "",
    phone: "",
    message: "",
    interest: "enterprise",
    employees: "1-10",
  });
  const [errors, setErrors] = useState<Partial<FormData>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);

  const handleChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // Clear error when typing
    if (errors[name as keyof FormData]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Partial<FormData> = {};
    if (!formData.name) newErrors.name = "Name is required";
    if (!formData.email) {
      newErrors.email = "Email is required";
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = "Email is invalid";
    }
    if (!formData.company) newErrors.company = "Company name is required";
    if (!formData.message) newErrors.message = "Message is required";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    try {
      await new Promise((resolve) => setTimeout(resolve, 1500));
      console.log("Contact form data:", formData);
      setIsSubmitted(true);
    } catch (error) {
      console.error("Form submission error:", error);
      alert("Submission failed. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isSubmitted) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-green-50 to-emerald-100 flex items-center justify-center">
        <div className="max-w-md w-full text-center">
          <div className="flex items-center justify-center space-x-2 mb-6">
            <div className="bg-green-600 p-2 rounded-full">
              <Car className="h-8 w-8 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-green-800">
              EV Charging Portal
            </h1>
          </div>
          <div className="bg-white shadow-xl rounded-2xl p-8">
            <CheckCircle className="h-16 w-16 text-green-500 mx-auto mb-6" />
            <h2 className="text-2xl font-bold text-gray-800 mb-2">
              Thank You!
            </h2>
            <p className="text-gray-600 mb-6">
              Your message has been received. Our sales team will contact you
              within 24 hours.
            </p>
            <button
              onClick={() => setIsSubmitted(false)}
              className="w-full py-3 px-4 rounded-md text-white bg-green-600 hover:bg-green-700 transition"
            >
              Submit Another Inquiry
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-50 to-emerald-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto">
        {/* Header */}
        <div className="text-center mb-12">
          <div className="flex items-center justify-center space-x-2 mb-4">
            <div className="bg-green-600 p-2 rounded-full">
              <Car className="h-8 w-8 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-green-800">
              EV Charging Portal
            </h1>
          </div>
          <h2 className="text-2xl font-semibold text-gray-800">
            Contact Our Sales Team
          </h2>
          <p className="mt-2 text-gray-600 max-w-2xl mx-auto">
            Get in touch with our experts to learn how our EV charging solutions
            can benefit your business.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Contact Info */}
          <ContactInfo />
          {/* Contact Form */}
          <div className="lg:col-span-2 bg-white shadow-xl rounded-2xl p-8">
            <div className="flex items-center mb-6">
              <ChatBubbleLeftRightIcon className="h-6 w-6 text-green-600 mr-2" />
              <h3 className="text-xl font-semibold text-gray-800">
                Send us a message
              </h3>
            </div>
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input
                  label="Full Name"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="John Doe"
                  required
                  error={errors.name}
                  icon={<UserIcon className="h-5 w-5" />}
                />
                <Input
                  label="Email Address"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="you@company.com"
                  required
                  error={errors.email}
                  icon={<EnvelopeIcon className="h-5 w-5" />}
                />
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input
                  label="Company Name"
                  name="company"
                  value={formData.company}
                  onChange={handleChange}
                  placeholder="Your Company"
                  required
                  error={errors.company}
                  icon={<BuildingOfficeIcon className="h-5 w-5" />}
                />
                <Input
                  label="Phone Number"
                  name="phone"
                  type="tel"
                  value={formData.phone}
                  onChange={handleChange}
                  placeholder="(555) 123-4567"
                  icon={<PhoneIcon className="h-5 w-5" />}
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    I'm interested in
                  </label>
                  <select
                    name="interest"
                    value={formData.interest}
                    onChange={handleChange}
                    className="block w-full py-2 px-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500"
                  >
                    <option value="enterprise">Enterprise Solutions</option>
                    <option value="small-business">Small Business</option>
                    <option value="municipal">Municipal/Government</option>
                    <option value="partnership">Partnership</option>
                    <option value="other">Other</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Company Size
                  </label>
                  <select
                    name="employees"
                    value={formData.employees}
                    onChange={handleChange}
                    className="block w-full py-2 px-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500"
                  >
                    <option value="1-10">1-10 employees</option>
                    <option value="11-50">11-50 employees</option>
                    <option value="51-200">51-200 employees</option>
                    <option value="201-500">201-500 employees</option>
                    <option value="501+">501+ employees</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Message *
                </label>
                <textarea
                  name="message"
                  rows={4}
                  value={formData.message}
                  onChange={handleChange}
                  className={`block w-full py-2 px-3 border rounded-md focus:ring-2 focus:ring-green-500 ${
                    errors.message ? "border-red-500" : "border-gray-300"
                  }`}
                  placeholder="Tell us about your EV charging needs..."
                />
                {errors.message && (
                  <p className="mt-1 text-sm text-red-600">{errors.message}</p>
                )}
              </div>

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full flex justify-center items-center py-3 px-4 rounded-md text-white bg-green-600 hover:bg-green-700 transition disabled:opacity-75"
              >
                {isSubmitting ? (
                  <>
                    <svg
                      className="animate-spin -ml-1 mr-2 h-4 w-4"
                      viewBox="0 0 24 24"
                    >
                      <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                      />
                      <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                      />
                    </svg>
                    Sending...
                  </>
                ) : (
                  <>
                    <Send className="h-4 w-4 mr-2" /> Send Message
                  </>
                )}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
