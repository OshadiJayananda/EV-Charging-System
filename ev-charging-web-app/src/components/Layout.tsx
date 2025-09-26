import { Link, Outlet } from "react-router-dom";
import { useState } from "react";
import { Menu, X } from "lucide-react";
import { useAuth } from "../context/AuthContext";

const Layout: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const { isAuthenticated, userRole, logout } = useAuth();

  return (
    <div className="flex flex-col min-h-screen">
      {/* Navbar */}
      <nav className="fixed top-0 left-0 right-0 bg-gradient-to-r from-green-500 to-emerald-600 text-white p-4 flex items-center justify-between z-50 shadow-md">
        <Link to="/" className="font-bold text-xl">
          EV Charging App
        </Link>

        {/* Desktop Links */}
        <div className="hidden md:flex space-x-4 items-center">
          {isAuthenticated && userRole === "admin" && (
            <Link to="/admin/dashboard" className="hover:underline">
              Admin
            </Link>
          )}
          {isAuthenticated && userRole === "operator" && (
            <Link to="/operator/dashboard" className="hover:underline">
              CS Operator
            </Link>
          )}
          {isAuthenticated && (
            <button
              onClick={logout}
              className="ml-4 px-3 py-1 rounded bg-white text-green-700 font-semibold hover:bg-green-100 transition-colors"
            >
              Logout
            </button>
          )}
          {!isAuthenticated && (
            <Link to="/login" className="hover:underline">
              Login
            </Link>
          )}
        </div>

        {/* Mobile Menu Button */}
        <button
          className="md:hidden"
          onClick={() => setIsOpen(!isOpen)}
          aria-label="Toggle Menu"
        >
          {isOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </nav>
      {/* Mobile Dropdown Menu */}
      {isOpen && (
        <div className="fixed top-16 left-0 right-0 bg-green-700 text-white p-4 space-y-2 md:hidden shadow-md z-40">
          {isAuthenticated && userRole === "admin" && (
            <Link
              to="/admin/dashboard"
              className="block hover:underline"
              onClick={() => setIsOpen(false)}
            >
              Admin
            </Link>
          )}
          {isAuthenticated && userRole === "operator" && (
            <Link
              to="/operator/dashboard"
              className="block hover:underline"
              onClick={() => setIsOpen(false)}
            >
              CS Operator
            </Link>
          )}
          {isAuthenticated && (
            <button
              onClick={() => {
                logout();
                setIsOpen(false);
              }}
              className="block w-full text-left px-3 py-1 rounded bg-white text-green-700 font-semibold hover:bg-green-100 transition-colors mt-2"
            >
              Logout
            </button>
          )}
          {!isAuthenticated && (
            <Link
              to="/login"
              className="block hover:underline"
              onClick={() => setIsOpen(false)}
            >
              Login
            </Link>
          )}
        </div>
      )}
      {/* Page Content */}
      <main className="flex-1 flex flex-col pt-16">
        <Outlet />
      </main>
    </div>
  );
};

export default Layout;
