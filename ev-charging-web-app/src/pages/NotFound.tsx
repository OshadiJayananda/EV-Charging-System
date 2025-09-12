import { useNavigate } from "react-router-dom";
import { BoltIcon } from "@heroicons/react/24/outline";

const NotFound = () => {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col items-center justify-center flex-1 bg-gradient-to-b from-blue-50 to-white px-4 text-center">
      <BoltIcon className="w-20 h-20 text-yellow-400 mb-4 animate-bounce" />
      <h1 className="text-6xl sm:text-8xl font-bold text-blue-600 mb-4">404</h1>
      <p className="text-xl sm:text-2xl md:text-3xl text-gray-700 mb-2">
        Oops! Page not found.
      </p>
      <p className="text-gray-500 mb-6">
        Looks like the charging station you’re trying to reach doesn’t exist.
      </p>
      <button
        onClick={() => navigate("/")}
        className="px-6 py-3 bg-blue-600 text-white font-semibold rounded-lg shadow-lg hover:bg-blue-700 transition"
      >
        Go Back to Dashboard
      </button>
    </div>
  );
};

export default NotFound;
