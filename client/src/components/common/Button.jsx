import React from 'react';

const Button = ({ children, variant = 'primary', className = '', ...props }) => {
  const baseStyle = "px-4 py-2 font-medium rounded-md transition-all duration-200 flex items-center justify-center gap-2 active:scale-[0.98] disabled:opacity-70 disabled:cursor-not-allowed disabled:active:scale-100";
  
  const variants = {
    primary: "bg-primary text-white hover:bg-black/90",
    secondary: "bg-gray-100 text-primary hover:bg-gray-200",
    inverted: "bg-tertiary text-white hover:bg-neutral",
    outlined: "border border-gray-300 text-primary hover:bg-gray-50",
  };

  return (
    <button className={`${baseStyle} ${variants[variant]} ${className}`} {...props}>
      {children}
    </button>
  );
};

export default Button;
