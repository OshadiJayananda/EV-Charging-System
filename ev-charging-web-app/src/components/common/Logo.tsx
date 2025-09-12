import React from 'react';
import { Car } from 'lucide-react';

interface LogoProps {
  size?: 'sm' | 'md' | 'lg';
  showText?: boolean;
  text?: string;
  className?: string;
  iconClassName?: string;
  textClassName?: string;
}

const Logo: React.FC<LogoProps> = ({
  size = 'md',
  showText = true,
  text = 'EV Charging Portal',
  className = '',
  iconClassName = '',
  textClassName = '',
}) => {
  const sizeClasses = {
    sm: { container: 'space-x-1', icon: 'p-1', iconSize: 'h-4 w-4', text: 'text-lg' },
    md: { container: 'space-x-2', icon: 'p-2', iconSize: 'h-6 w-6', text: 'text-xl' },
    lg: { container: 'space-x-2', icon: 'p-2', iconSize: 'h-8 w-8', text: 'text-3xl' },
  };

  const currentSize = sizeClasses[size];

  return (
    <div className={`flex items-center ${currentSize.container} ${className}`}>
      <div className={`bg-green-600 ${currentSize.icon} rounded-full ${iconClassName}`}>
        <Car className={`${currentSize.iconSize} text-white`} />
      </div>
      {showText && (
        <h1 className={`${currentSize.text} font-bold text-green-800 ${textClassName}`}>
          {text}
        </h1>
      )}
    </div>
  );
};

export default Logo;
