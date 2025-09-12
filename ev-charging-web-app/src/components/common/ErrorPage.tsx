import React from 'react';
import { useNavigate } from 'react-router-dom';
import Button from './Button';

interface ErrorPageProps {
  icon: React.ReactNode;
  title: string;
  subtitle: string;
  description: string;
  buttonText?: string;
  buttonVariant?: 'primary' | 'secondary' | 'outline' | 'danger';
  onButtonClick?: () => void;
  gradient?: string;
  className?: string;
}

const ErrorPage: React.FC<ErrorPageProps> = ({
  icon,
  title,
  subtitle,
  description,
  buttonText = 'Go Back to Dashboard',
  buttonVariant = 'primary',
  onButtonClick,
  gradient = 'from-blue-50 to-white',
  className = '',
}) => {
  const navigate = useNavigate();

  const handleButtonClick = () => {
    if (onButtonClick) {
      onButtonClick();
    } else {
      navigate('/');
    }
  };

  return (
    <div className={`flex flex-col items-center justify-center flex-1 bg-gradient-to-b ${gradient} px-4 text-center ${className}`}>
      <div className="mb-4">
        {icon}
      </div>
      <h1 className="text-6xl sm:text-8xl font-bold mb-4">
        {title}
      </h1>
      <p className="text-xl sm:text-2xl md:text-3xl text-gray-700 mb-2">
        {subtitle}
      </p>
      <p className="text-gray-500 mb-6">
        {description}
      </p>
      <Button
        variant={buttonVariant}
        onClick={handleButtonClick}
      >
        {buttonText}
      </Button>
    </div>
  );
};

export default ErrorPage;
