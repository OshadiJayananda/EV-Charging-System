import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { useState } from "react";
import { Menu, X } from "lucide-react";

interface LayoutProps {
  children: ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className="flex flex-col min-h-screen">
      {/* Navbar */}
      <nav className="fixed top-0 left-0 right-0 bg-green-600 text-white p-4 flex items-center justify-between z-50 shadow-md">
        <h1 className="font-bold text-xl">EV Charging App</h1>

        {/* Desktop Links */}
        <div className="hidden md:flex space-x-4">
          <Link to="/admin" className="hover:underline">
            Admin
          </Link>
          <Link to="/cs-operator" className="hover:underline">
            CS Operator
          </Link>
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
          <Link
            to="/admin"
            className="block hover:underline"
            onClick={() => setIsOpen(false)}
          >
            Admin
          </Link>
          <Link
            to="/cs-operator"
            className="block hover:underline"
            onClick={() => setIsOpen(false)}
          >
            CS Operator
          </Link>
        </div>
      )}

      {/* Page Content */}
      <main className="flex-1 flex flex-col pt-16">{children}</main>
    </div>
  );
};

export default Layout;
