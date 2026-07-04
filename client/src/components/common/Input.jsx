import React from 'react';

const Input = ({ label, className = '', ...props }) => {
  return (
    <div className={`flex flex-col gap-1.5 ${className}`}>
      {label && <label className="text-sm font-medium text-neutral">{label}</label>}
      <input 
        className="px-3 py-2 border border-gray-200 rounded-md focus:outline-none focus:border-primary transition-colors duration-200 bg-gray-50/50"
        {...props}
      />
    </div>
  );
};

export default Input;
