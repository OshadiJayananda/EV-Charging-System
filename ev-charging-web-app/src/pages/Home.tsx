import {
  BuildingOffice2Icon,
  UsersIcon,
  ClockIcon,
  CalendarDaysIcon,
  ChartBarIcon,
  CheckCircleIcon,
} from "@heroicons/react/24/outline";

import { Zap } from "lucide-react";
import { Link } from "react-router-dom";
import {
  Button,
  Section,
  IconCard,
  FeatureItem,
  Footer,
} from "../components/common";

function Home() {
  return (
    <div className="flex flex-col flex-1 min-h-[calc(100vh-4rem)] -mt-16 pt-15">
      {/* Hero Section */}
      <Section background="gradient-green" className="text-white">
        <div className="max-w-4xl mx-auto text-center space-y-6">
          <h1 className="text-4xl md:text-5xl font-bold">
            Welcome to the EV Charging Management Portal
          </h1>
          <p className="text-lg md:text-xl text-white/90 max-w-2xl mx-auto">
            A unified platform to streamline the management of charging
            stations, operator activities, and EV owner bookings for a seamless
            charging experience.
          </p>
          <Link to="/login">
            <Button variant="secondary">Get Started</Button>
          </Link>
        </div>
      </Section>

      {/* Who Uses Section */}
      <Section background="gray">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-3xl font-bold text-center text-gray-800 mb-12">
            Who Uses Our Platform
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <IconCard
              icon={<BuildingOffice2Icon className="h-8 w-8" />}
              title="Backoffice"
              description="Register and manage charging stations, maintain schedules, and oversee network-wide operations."
              borderColor="border-green-500"
              iconBgColor="bg-green-100"
              iconColor="text-green-600"
            />

            <IconCard
              icon={<Zap className="h-8 w-8" />}
              title="Station Operators"
              description="Update slot availability, monitor bookings, and assist EV owners with reservations and cancellations."
              borderColor="border-blue-500"
              iconBgColor="bg-blue-100"
              iconColor="text-blue-600"
            />

            <IconCard
              icon={<UsersIcon className="h-8 w-8" />}
              title="EV Owners"
              description="Use the mobile app to book, modify, and cancel charging slots and track charging history."
              borderColor="border-purple-500"
              iconBgColor="bg-purple-100"
              iconColor="text-purple-600"
            />
          </div>
        </div>
      </Section>

      {/* Key Features */}
      <Section background="white">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-3xl font-bold text-center text-gray-800 mb-12">
            Key Features
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <FeatureItem
              icon={<CheckCircleIcon className="h-6 w-6" />}
              title="Station registration and management"
              description="Easily register and manage all your charging stations in one place."
              iconBgColor="bg-green-100"
              iconColor="text-green-600"
            />

            <FeatureItem
              icon={<ClockIcon className="h-6 w-6" />}
              title="Real-time slot availability updates"
              description="Get instant updates on charging slot availability across the network."
              iconBgColor="bg-blue-100"
              iconColor="text-blue-600"
            />

            <FeatureItem
              icon={<CalendarDaysIcon className="h-6 w-6" />}
              title="Seamless reservation and cancellation process"
              description="Simple and intuitive booking system for users and operators."
              iconBgColor="bg-purple-100"
              iconColor="text-purple-600"
            />

            <FeatureItem
              icon={<ChartBarIcon className="h-6 w-6" />}
              title="Performance monitoring and reporting"
              description="Comprehensive analytics and reporting tools to track station performance."
              iconBgColor="bg-yellow-100"
              iconColor="text-yellow-600"
            />
          </div>
        </div>
      </Section>

      {/* CTA Section */}
      <Section background="gradient-emerald" className="text-white">
        <div className="max-w-3xl mx-auto text-center">
          <h2 className="text-3xl font-bold mb-6">Ready to Get Started?</h2>
          <p className="text-lg mb-8 text-white/90">
            Join thousands of satisfied users who are already managing their EV
            charging operations efficiently.
          </p>
          <div className="flex flex-col sm:flex-row justify-center gap-4">
            <Link to="/login">
              <Button variant="secondary">Sign In</Button>
            </Link>
            <Link to="/contact-sales">
              <Button variant="outline">Contact Sales</Button>
            </Link>
          </div>
        </div>
      </Section>

      {/* Footer */}
      <Footer />
    </div>
  );
}

export default Home;
