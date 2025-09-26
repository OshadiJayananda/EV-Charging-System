import { Link, Outlet, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import { LogOut, Menu, User, X, Bell } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { postRequest, getRequest } from "./common/api";
import toast from "react-hot-toast";
import { HubConnectionBuilder } from "@microsoft/signalr";
import type { Notification } from "../types";
import { roleNavigate } from "./common/RoleBasedAccess";

const Layout: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [showNotifications, setShowNotifications] = useState(false);
  const { isAuthenticated, userRole, logout } = useAuth();
  const navigate = useNavigate();

  // Fetch notifications and setup SignalR
  useEffect(() => {
    let connection: any;
    const userId = localStorage.getItem("userId");
    if (isAuthenticated && userId) {
      getRequest<Notification[]>(`/notifications/user/${userId}`).then(
        (res) => {
          setNotifications(res?.data ?? []);
        }
      );

      connection = new HubConnectionBuilder()
        .withUrl("/notificationHub", {
          accessTokenFactory: () => localStorage.getItem("token") || "",
        })
        .withAutomaticReconnect()
        .build();

      connection.on("ReceiveNotification", (notification: any) => {
        setNotifications((prev) => [notification, ...prev]);
        toast.success(notification.message);
      });

      connection.start().catch(console.error);
    }
    return () => {
      if (connection) connection.stop();
    };
  }, [isAuthenticated]);

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

  const handleRoleNavigate = () => {
    roleNavigate(userRole, navigate);
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
          {isAuthenticated && (
            <Link
              to="/profile"
              className="hover:underline flex items-center gap-1"
            >
              <User className="w-5 h-5" />
              Profile
            </Link>
          )}
          {isAuthenticated && (
            <button
              onClick={() => setShowNotifications(!showNotifications)}
              className="relative"
              aria-label="Show notifications"
            >
              <Bell className="w-5 h-5" />
              {notifications.some((n) => !n.isRead) && (
                <span className="absolute -top-1 -right-1 bg-red-500 text-white rounded-full text-xs px-1">
                  {notifications.filter((n) => !n.isRead).length}
                </span>
              )}
            </button>
          )}
          {isAuthenticated && (
            <button onClick={handleRoleNavigate} className="hover:underline">
              Dashboard
            </button>
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
          {isAuthenticated && (
            <Link
              to="/profile"
              className="block hover:underline flex items-center gap-1"
              onClick={() => setIsOpen(false)}
            >
              Profile
            </Link>
          )}
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

      {/* Notifications Dropdown */}
      {showNotifications && (
        <div className="absolute right-4 top-16 bg-white shadow-lg rounded-lg w-80 z-50 border">
          <div className="p-4 border-b font-bold text-green-700 flex justify-between items-center">
            Notifications
            <button
              className="text-gray-400 hover:text-gray-600"
              onClick={() => setShowNotifications(false)}
            >
              <X className="w-5 h-5" />
            </button>
          </div>
          <div className="max-h-80 overflow-y-auto">
            {notifications.length === 0 ? (
              <div className="p-4 text-gray-500 text-center">
                No notifications
              </div>
            ) : (
              notifications.map((n) => (
                <div
                  key={n.id}
                  className={`p-4 border-b last:border-b-0 ${
                    n.isRead ? "bg-gray-50" : "bg-green-50"
                  }`}
                >
                  <div className="font-medium">{n.message}</div>
                  <div className="text-xs text-gray-400">
                    {new Date(n.createdAt).toLocaleString()}
                  </div>
                  {!n.isRead && (
                    <button
                      className="mt-2 text-xs text-green-600 hover:underline"
                      onClick={async () => {
                        await postRequest(`/notifications/${n.id}/read`);
                        setNotifications((prev) =>
                          prev.map((x) =>
                            x.id === n.id ? { ...x, isRead: true } : x
                          )
                        );
                      }}
                    >
                      Mark as read
                    </button>
                  )}
                </div>
              ))
            )}
          </div>
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
