import React from 'react';

const StatusBadge = ({ status = 'Operational' }) => {
  const isOperational = status.toLowerCase() === 'active' || status.toLowerCase() === 'operational';
  
  return (
    <div className="flex items-center gap-2 text-sm font-medium">
      <span className="text-neutral">Status:</span>
      <div className="flex items-center gap-1.5">
        <div className={`w-2 h-2 rounded-full ${isOperational ? 'bg-green-500' : 'bg-red-500'}`}></div>
        <span className={isOperational ? 'text-primary' : 'text-red-600'}>
          {status}
        </span>
      </div>
    </div>
  );
};

export default StatusBadge;
