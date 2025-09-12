import React from "react";
import { useNavigate } from "react-router-dom";
import { BoltIcon } from "@heroicons/react/24/outline";
import ErrorPage from "../components/common/ErrorPage";

const NotFound: React.FC = () => {
  const navigate = useNavigate();

  return (
    <ErrorPage
      icon={<BoltIcon className="w-20 h-20 text-yellow-400 animate-bounce" />}
      title="404"
      subtitle="Oops! Page not found."
      description="Looks like the charging station you’re trying to reach doesn’t exist."
      buttonText="Go Back to Dashboard"
      buttonVariant="primary"
      gradient="from-blue-50 to-white"
      onButtonClick={() => navigate("/")}
    />
  );
};

export default NotFound;
