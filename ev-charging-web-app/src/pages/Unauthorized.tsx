import React from "react";
import { LockClosedIcon } from "@heroicons/react/24/outline";
import { ErrorPage } from "../components/common";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { roleNavigate } from "../components/common/RoleBasedAccess";

const Unauthorized: React.FC = () => {
  const { userRole } = useAuth();
  const navigate = useNavigate();

  let buttonText = "Go Back to Sign In";
  if (userRole === "admin") {
    buttonText = "Go to Admin Dashboard";
  } else if (userRole === "operator") {
    buttonText = "Go to Operator Dashboard";
  }

  return (
    <ErrorPage
      icon={<LockClosedIcon className="w-20 h-20 text-red-500 animate-pulse" />}
      title="401"
      subtitle="Unauthorized Access"
      description="You don’t have permission to view this page or perform this action."
      buttonText={buttonText}
      buttonVariant="danger"
      gradient="from-red-50 to-white"
      onButtonClick={() => roleNavigate(userRole, navigate)}
    />
  );
};

export default Unauthorized;
