import { Link, Outlet } from "react-router-dom";
import { useState } from "react";
import { LogOut, Menu, X } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { postRequest } from "./common/api";
import toast from "react-hot-toast";

const Layout: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const { isAuthenticated, userRole, logout } = useAuth();

  type LogoutResponse = {
    success?: boolean;
    [key: string]: any;
  };

  const handleLogout = async () => {
    setShowLogoutModal(true);
  };

  const confirmLogout = async () => {
    setShowLogoutModal(false);
    const response = (await postRequest("/auth/logout")) as LogoutResponse;
    if (response?.status === 200) {
      toast.success("Logout successful!");
    }
    logout();
  };

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
              onClick={handleLogout}
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
                handleLogout();
                setIsOpen(false);
              }}
              className="block w-full text-left px-3 py-1 rounded bg-green-700 text-white font-semibold hover:bg-green-100 transition-colors mt-2"
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

      {/* Logout Confirmation Modal */}
      {showLogoutModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center backdrop-blur-sm">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-sm text-center">
            <LogOut className="mx-auto mb-2 text-red-500" size={40} />
            <h2 className="text-lg font-bold mb-2">Confirm Logout</h2>
            <p className="mb-4">
              🚪 Are you sure you want to log out?
              <br />
              You will be redirected to the login page.
            </p>
            <div className="flex justify-center gap-4">
              <button
                onClick={confirmLogout}
                className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 font-semibold"
              >
                Yes, Logout
              </button>
              <button
                onClick={() => setShowLogoutModal(false)}
                className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 font-semibold"
              >
                Cancel
              </button>
            </div>
          </div>
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
