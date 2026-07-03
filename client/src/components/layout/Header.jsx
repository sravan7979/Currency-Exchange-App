import React from 'react';
import { Printer, RefreshCw, Power } from 'lucide-react';
import StatusBadge from '../common/StatusBadge';

const Header = () => {
  return (
    <header className="h-16 border-b border-gray-200 bg-white flex items-center justify-between px-8 sticky top-0 z-10">
      <div className="flex items-center text-sm">
        <span className="text-neutral mr-2">Current Region:</span>
        <span className="font-semibold text-primary">India-South-1</span>
      </div>
      
      <div className="flex items-center gap-6">
        <div className="flex items-center gap-4 text-tertiary">
          <button className="hover:text-primary transition-colors">
            <Printer size={18} />
          </button>
          <button className="hover:text-primary transition-colors">
            <RefreshCw size={18} />
          </button>
          <button className="hover:text-primary transition-colors">
            <Power size={18} />
          </button>
        </div>
        
        <div className="w-px h-6 bg-gray-200"></div>
        
        <StatusBadge status="Active" />
      </div>
    </header>
  );
};

export default Header;
