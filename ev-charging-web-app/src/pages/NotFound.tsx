import React from "react";
import { useNavigate } from "react-router-dom";
import { BoltIcon } from "@heroicons/react/24/outline";
import ErrorPage from "../components/common/ErrorPage";
import { useAuth } from "../context/AuthContext";
import { roleNavigate } from "../components/common/RoleBasedAccess";

const NotFound: React.FC = () => {
  const navigate = useNavigate();
  const { userRole } = useAuth();

  let buttonText = "Go Back to Sign In";
  if (userRole === "admin") {
    buttonText = "Go to Admin Dashboard";
  } else if (userRole === "operator") {
    buttonText = "Go to Operator Dashboard";
  }

  return (
    <ErrorPage
      icon={<BoltIcon className="w-20 h-20 text-yellow-400 animate-bounce" />}
      title="404"
      subtitle="Oops! Page not found."
      description="Looks like the charging station you’re trying to reach doesn’t exist."
      buttonText={buttonText}
      buttonVariant="primary"
      gradient="from-blue-50 to-white"
      onButtonClick={() => roleNavigate(userRole, navigate)}
    />
  );
};

export default NotFound;
