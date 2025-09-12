import { Routes, Route, Link } from "react-router-dom";
import "./App.css";
import AdminDashboard from "./components/Admin/AdminDashboard";
import CSOperatorDashboard from "./components/CSOperator/CSOperatorDashboard";

function App() {
  return (
    <div className="App">
      {/* Navbar */}
      <nav className="bg-green-600 text-white p-4 flex justify-between">
        <h1 className="font-bold text-xl">EV Charging App</h1>
        <div className="space-x-4">
          <Link to="/admin" className="hover:underline">
            Admin
          </Link>
          <Link to="/cs-operator" className="hover:underline">
            CS Operator
          </Link>
        </div>
      </nav>

      {/* Routes */}
      <main className="p-4">
        <Routes>
          <Route path="/admin" element={<AdminDashboard />} />
          <Route path="/cs-operator" element={<CSOperatorDashboard />} />
          <Route
            path="*"
            element={
              <h2>Welcome to EV Charging App! Select a dashboard above.</h2>
            }
          />
        </Routes>
      </main>
    </div>
  );
}

export default App;
