import React from 'react';
import Card from './Card';

interface IconCardProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  borderColor?: string;
  iconBgColor?: string;
  iconColor?: string;
  className?: string;
}

const IconCard: React.FC<IconCardProps> = ({
  icon,
  title,
  description,
  borderColor = 'border-green-500',
  iconBgColor = 'bg-green-100',
  iconColor = 'text-green-600',
  className = '',
}) => {
  return (
    <Card hover className={`border-t-4 ${borderColor} ${className}`}>
      <div className="text-center mb-4">
        <div className={`${iconBgColor} w-16 h-16 rounded-full flex items-center justify-center mx-auto`}>
          <div className={iconColor}>
            {icon}
          </div>
        </div>
      </div>
      <h3 className="text-xl font-semibold text-center text-gray-800 mb-3">
        {title}
      </h3>
      <p className="text-gray-600 text-center">
        {description}
      </p>
    </Card>
  );
};

export default IconCard;
