import React from 'react';

interface SectionProps {
  children: React.ReactNode;
  className?: string;
  background?: 'white' | 'gray' | 'gradient-green' | 'gradient-emerald';
  padding?: 'sm' | 'md' | 'lg' | 'xl';
}

const Section: React.FC<SectionProps> = ({
  children,
  className = '',
  background = 'white',
  padding = 'lg',
}) => {
  const backgroundClasses = {
    white: 'bg-white',
    gray: 'bg-gray-50',
    'gradient-green': 'bg-gradient-to-r from-green-500 to-emerald-600',
    'gradient-emerald': 'bg-gradient-to-r from-green-400 to-emerald-600',
  };

  const paddingClasses = {
    sm: 'py-8 px-6',
    md: 'py-12 px-6',
    lg: 'py-16 px-6',
    xl: 'py-20 px-6',
  };

  const classes = `${backgroundClasses[background]} ${paddingClasses[padding]} ${className}`;

  return (
    <section className={classes}>
      {children}
    </section>
  );
};

export default Section;
