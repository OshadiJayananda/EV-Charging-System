import {
  PhoneIcon,
  EnvelopeIcon,
  MapPinIcon,
  ClockIcon,
} from "@heroicons/react/24/outline";
import { companyInfo } from "../../config/companyInfo";
import { ArrowLeft } from "lucide-react";

export default function ContactInfo() {
  return (
    <div className="bg-white rounded-2xl shadow-md p-6">
      <h3 className="text-lg font-semibold text-gray-800 mb-6">Get in Touch</h3>
      <div className="space-y-6">
        {/* Phone */}
        <div className="flex items-start">
          <PhoneIcon className="h-5 w-5 text-green-600 mr-3" />
          <div>
            <h4 className="font-medium text-gray-700">Phone</h4>
            <p className="text-gray-600">{companyInfo.phone.number}</p>
            <p className="text-sm text-gray-500">{companyInfo.phone.note}</p>
          </div>
        </div>

        {/* Email */}
        <div className="flex items-start">
          <EnvelopeIcon className="h-5 w-5 text-green-600 mr-3" />
          <div>
            <h4 className="font-medium text-gray-700">Email</h4>
            <p className="text-gray-600">{companyInfo.email.address}</p>
            <p className="text-sm text-gray-500">{companyInfo.email.note}</p>
          </div>
        </div>

        {/* Address */}
        <div className="flex items-start">
          <MapPinIcon className="h-5 w-5 text-green-600 mr-3" />
          <div>
            <h4 className="font-medium text-gray-700">Office</h4>
            <p className="text-gray-600">{companyInfo.address.line1}</p>
            <p className="text-gray-600">{companyInfo.address.line2}</p>
          </div>
        </div>

        {/* Hours */}
        <div className="flex items-start">
          <ClockIcon className="h-5 w-5 text-green-600 mr-3" />
          <div>
            <h4 className="font-medium text-gray-700">Business Hours</h4>
            <p className="text-gray-600">{companyInfo.hours.weekdays}</p>
            <p className="text-gray-600">{companyInfo.hours.saturday}</p>
          </div>
        </div>
      </div>

      <a
        href="/"
        className="mt-6 w-full flex justify-center items-center py-3 px-4 rounded-md text-white bg-green-600 hover:bg-green-700 transition"
      >
        <ArrowLeft className="h-4 w-4 mr-2" /> Go Back
      </a>
    </div>
  );
}
