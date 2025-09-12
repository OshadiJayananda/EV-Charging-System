import React from 'react';

interface FeatureItemProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  iconBgColor?: string;
  iconColor?: string;
  className?: string;
}

const FeatureItem: React.FC<FeatureItemProps> = ({
  icon,
  title,
  description,
  iconBgColor = 'bg-green-100',
  iconColor = 'text-green-600',
  className = '',
}) => {
  return (
    <div className={`flex items-start space-x-4 p-4 rounded-lg hover:bg-gray-50 transition-colors duration-300 ${className}`}>
      <div className={`${iconBgColor} p-3 rounded-full`}>
        <div className={iconColor}>
          {icon}
        </div>
      </div>
      <div>
        <h3 className="font-semibold text-gray-800">
          {title}
        </h3>
        <p className="text-gray-600 mt-1">
          {description}
        </p>
      </div>
    </div>
  );
};

export default FeatureItem;
