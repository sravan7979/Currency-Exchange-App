import React from 'react';

const ProgressBar = ({ label, subLabel, value, max = 100, color = 'bg-primary' }) => {
  const percentage = Math.min(100, Math.max(0, (value / max) * 100));

  return (
    <div className="flex flex-col gap-2 w-full">
      <div className="flex justify-between items-end">
        <span className="text-sm font-medium text-primary">{label}</span>
        {subLabel && <span className="text-xs text-neutral">{subLabel}</span>}
      </div>
      <div className="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
        <div 
          className={`h-full rounded-full transition-all duration-500 ease-out ${color}`} 
          style={{ width: `${percentage}%` }}
        ></div>
      </div>
    </div>
  );
};

export default ProgressBar;
