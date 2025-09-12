import React from "react";
import { LockClosedIcon } from "@heroicons/react/24/outline";
import { ErrorPage } from "../components/common";

const Unauthorized: React.FC = () => {
  return (
    <ErrorPage
      icon={<LockClosedIcon className="w-20 h-20 text-red-500 animate-pulse" />}
      title="401"
      subtitle="Unauthorized Access"
      description="You don’t have permission to view this page or perform this action."
      buttonText="Go Back to Sign In"
      buttonVariant="danger"
      gradient="from-red-50 to-white"
      onButtonClick={() => (window.location.href = "/login")}
    />
  );
};

export default Unauthorized;
