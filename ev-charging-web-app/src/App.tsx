import { Routes, Route, useLocation } from "react-router-dom";
import Layout from "./components/Layout";
import AdminDashboard from "./components/Admin/AdminDashboard";
import CSOperatorDashboard from "./components/CSOperator/CSOperatorDashboard";
import NotFound from "./pages/NotFound";
import { useEffect } from "react";
import Unauthorized from "./pages/Unauthorized";

function App() {
  const location = useLocation();

  useEffect(() => {
    switch (location.pathname) {
      case "/admin":
        document.title = "Admin Dashboard | EV Charging App";
        break;
      case "/cs-operator":
        document.title = "CS Operator Dashboard | EV Charging App";
        break;
      default:
        document.title = "EV Charging Management App";
    }
  }, [location]);

  return (
    <Layout>
      <Routes>
        <Route
          path="/"
          element={
            <h2>Welcome to EV Charging App! Select a dashboard above.</h2>
          }
        />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/cs-operator" element={<CSOperatorDashboard />} />
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Layout>
  );
}

export default App;
