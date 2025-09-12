import { useNavigate } from "react-router-dom";
import { LockClosedIcon } from "@heroicons/react/24/outline";

const Unauthorized = () => {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col items-center justify-center flex-1 bg-gradient-to-b from-red-50 to-white px-4 text-center">
      <LockClosedIcon className="w-20 h-20 text-red-500 mb-4 animate-pulse" />
      <p className="text-xl sm:text-2xl md:text-3xl text-gray-700 mb-2">
        Unauthorized Access
      </p>
      <p className="text-gray-500 mb-6">
        You don’t have permission to view this page or perform this action.
      </p>
      <button
        onClick={() => navigate("/login")}
        className="px-6 py-3 bg-red-600 text-white font-semibold rounded-lg shadow-lg hover:bg-red-700 transition"
      >
        Go Back to Sign In
      </button>
    </div>
  );
};

export default Unauthorized;
