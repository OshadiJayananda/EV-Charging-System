import { Routes, Route, useLocation, Navigate } from "react-router-dom";
import Layout from "./components/Layout";
import AdminDashboard from "./components/Admin/AdminDashboard";
import CSOperatorDashboard from "./components/CSOperator/CSOperatorDashboard";
import NotFound from "./pages/NotFound";
import Unauthorized from "./pages/Unauthorized";
import Home from "./pages/Home";
import Login from "./pages/Login";
import ContactSales from "./pages/ContactSales";
import { useDocumentTitle } from "./hooks/useDocumentTitle";
import Contact from "./pages/Contact";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/common/ProtectedRoute";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";

function App() {
  const location = useLocation();

  // Set document title based on current route
  const getPageTitle = () => {
    switch (location.pathname) {
      case "/admin/dashboard":
        return "Admin Dashboard | EV Charging App";
      case "/operator/dashboard":
        return "CS Operator Dashboard | EV Charging App";
      case "/login":
        return "Login | EV Charging App";
      case "/contact-sales":
        return "Contact Sales | EV Charging App";
      default:
        return "EV Charging Management App";
    }
  };

  useDocumentTitle(getPageTitle());

  return (
    <AuthProvider>
      <Routes>
        {/* Routes without Layout */}
        <Route path="/login" element={<Login />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/contact-sales" element={<ContactSales />} />
        <Route path="/contact" element={<Contact />} />

        {/* All routes inside Layout */}
        <Route element={<Layout />}>
          <Route path="/" element={<Home />} />
          <Route path="/unauthorized" element={<Unauthorized />} />

          {/* Protected Routes */}
          <Route element={<ProtectedRoute requiredRole="admin" />}>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
          </Route>

          <Route element={<ProtectedRoute requiredRole="operator" />}>
            <Route
              path="/operator/dashboard"
              element={<CSOperatorDashboard />}
            />
          </Route>

          {/* Catch-all */}
          <Route path="/not-found" element={<NotFound />} />
          <Route path="*" element={<Navigate to="/not-found" replace />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}

export default App;
