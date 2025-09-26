import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

export default function ProtectedRoute({
  requiredRole,
}: Readonly<{
  requiredRole?: string;
}>) {
  const { isAuthenticated, userRole } = useAuth();

  // If not authenticated, redirect to login
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // If authenticated but no requiredRole is specified, render child routes
  if (!requiredRole) {
    return <Outlet />;
  }

  // If a requiredRole is specified, check if the user has the required role
  if (userRole !== requiredRole) {
    return <Navigate to="/unauthorized" replace />;
  }

  // If authenticated and has the required role, render child routes
  return <Outlet />;
}
