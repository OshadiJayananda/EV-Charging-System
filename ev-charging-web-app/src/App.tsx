import { Routes, Route, useLocation } from "react-router-dom";
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

function App() {
  const location = useLocation();

  // Set document title based on current route
  const getPageTitle = () => {
    switch (location.pathname) {
      case "/admin":
        return "Admin Dashboard | EV Charging App";
      case "/cs-operator":
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
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/contact-sales" element={<ContactSales />} />
      <Route path="/contact" element={<Contact />} />

      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/admin/dashboard" element={<AdminDashboard />} />
        <Route path="/operator/dashboard" element={<CSOperatorDashboard />} />
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}

export default App;
