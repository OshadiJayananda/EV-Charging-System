import {
  BuildingOffice2Icon,
  UsersIcon,
  ClockIcon,
  CalendarDaysIcon,
  ChartBarIcon,
  CheckCircleIcon,
} from "@heroicons/react/24/outline";

import { Zap } from "lucide-react";

function Home() {
  return (
    <div className="flex flex-col flex-1 min-h-[calc(100vh-4rem)] -mt-16 pt-15">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-green-500 to-emerald-600 text-white py-16 px-6">
        <div className="max-w-4xl mx-auto text-center space-y-6">
          <h1 className="text-4xl md:text-5xl font-bold">
            Welcome to the EV Charging Management Portal
          </h1>
          <p className="text-lg md:text-xl text-white/90 max-w-2xl mx-auto">
            A unified platform to streamline the management of charging
            stations, operator activities, and EV owner bookings for a seamless
            charging experience.
          </p>
          <button className="bg-white text-emerald-700 hover:bg-gray-100 font-semibold py-3 px-8 rounded-lg transition-all duration-300 transform hover:-translate-y-1 shadow-lg">
            Get Started
          </button>
        </div>
      </section>

      {/* Who Uses Section */}
      <section className="py-16 px-6 bg-gray-50">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-3xl font-bold text-center text-gray-800 mb-12">
            Who Uses Our Platform
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-all duration-300 border-t-4 border-green-500">
              <div className="text-center mb-4">
                <div className="bg-green-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto">
                  <BuildingOffice2Icon className="h-8 w-8 text-green-600" />
                </div>
              </div>
              <h3 className="text-xl font-semibold text-center text-gray-800 mb-3">
                Backoffice
              </h3>
              <p className="text-gray-600 text-center">
                Register and manage charging stations, maintain schedules, and
                oversee network-wide operations.
              </p>
            </div>

            <div className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-all duration-300 border-t-4 border-blue-500">
              <div className="text-center mb-4">
                <div className="bg-blue-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto">
                  <Zap className="h-8 w-8 text-blue-600" />
                </div>
              </div>
              <h3 className="text-xl font-semibold text-center text-gray-800 mb-3">
                Station Operators
              </h3>
              <p className="text-gray-600 text-center">
                Update slot availability, monitor bookings, and assist EV owners
                with reservations and cancellations.
              </p>
            </div>

            <div className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-all duration-300 border-t-4 border-purple-500">
              <div className="text-center mb-4">
                <div className="bg-purple-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto">
                  <UsersIcon className="h-8 w-8 text-purple-600" />
                </div>
              </div>
              <h3 className="text-xl font-semibold text-center text-gray-800 mb-3">
                EV Owners
              </h3>
              <p className="text-gray-600 text-center">
                Use the mobile app to book, modify, and cancel charging slots
                and track charging history.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Key Features */}
      <section className="py-16 px-6 bg-white">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-3xl font-bold text-center text-gray-800 mb-12">
            Key Features
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="flex items-start space-x-4 p-4 rounded-lg hover:bg-gray-50 transition-colors duration-300">
              <div className="bg-green-100 p-3 rounded-full">
                <CheckCircleIcon className="h-6 w-6 text-green-600" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-800">
                  Station registration and management
                </h3>
                <p className="text-gray-600 mt-1">
                  Easily register and manage all your charging stations in one
                  place.
                </p>
              </div>
            </div>

            <div className="flex items-start space-x-4 p-4 rounded-lg hover:bg-gray-50 transition-colors duration-300">
              <div className="bg-blue-100 p-3 rounded-full">
                <ClockIcon className="h-6 w-6 text-blue-600" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-800">
                  Real-time slot availability updates
                </h3>
                <p className="text-gray-600 mt-1">
                  Get instant updates on charging slot availability across the
                  network.
                </p>
              </div>
            </div>

            <div className="flex items-start space-x-4 p-4 rounded-lg hover:bg-gray-50 transition-colors duration-300">
              <div className="bg-purple-100 p-3 rounded-full">
                <CalendarDaysIcon className="h-6 w-6 text-purple-600" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-800">
                  Seamless reservation and cancellation process
                </h3>
                <p className="text-gray-600 mt-1">
                  Simple and intuitive booking system for users and operators.
                </p>
              </div>
            </div>

            <div className="flex items-start space-x-4 p-4 rounded-lg hover:bg-gray-50 transition-colors duration-300">
              <div className="bg-yellow-100 p-3 rounded-full">
                <ChartBarIcon className="h-6 w-6 text-yellow-600" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-800">
                  Performance monitoring and reporting
                </h3>
                <p className="text-gray-600 mt-1">
                  Comprehensive analytics and reporting tools to track station
                  performance.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 px-6 bg-gradient-to-r from-green-400 to-emerald-600 text-white">
        <div className="max-w-3xl mx-auto text-center">
          <h2 className="text-3xl font-bold mb-6">Ready to Get Started?</h2>
          <p className="text-lg mb-8 text-white/90">
            Join thousands of satisfied users who are already managing their EV
            charging operations efficiently.
          </p>
          <div className="flex flex-col sm:flex-row justify-center gap-4">
            <button className="bg-white text-emerald-700 hover:bg-gray-100 font-semibold py-3 px-8 rounded-lg transition-colors duration-300">
              Create an Account
            </button>
            <button className="bg-transparent border-2 border-white text-white hover:bg-white/10 font-semibold py-3 px-8 rounded-lg transition-colors duration-300">
              Contact Sales
            </button>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-800 text-white py-3 px-4 text-center text-sm">
        <p>EV Charging Management App © {new Date().getFullYear()}</p>
        <p className="text-gray-400 text-xs mt-1">All rights reserved</p>
      </footer>
    </div>
  );
}

export default Home;
