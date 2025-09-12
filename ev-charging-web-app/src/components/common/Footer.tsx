import React from 'react';

interface FooterProps {
  className?: string;
  appName?: string;
}

const Footer: React.FC<FooterProps> = ({
  className = '',
  appName = 'EV Charging Management App',
}) => {
  return (
    <footer className={`bg-gray-800 text-white py-3 px-4 text-center text-sm ${className}`}>
      <p>{appName} © {new Date().getFullYear()}</p>
      <p className="text-gray-400 text-xs mt-1">All rights reserved</p>
    </footer>
  );
};

export default Footer;
