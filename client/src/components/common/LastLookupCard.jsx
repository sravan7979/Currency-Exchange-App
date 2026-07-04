import React from 'react';
import { BarChart2 } from 'lucide-react';

const formatMethod = (source) => {
  if (!source) return '--';
  switch (source.toUpperCase()) {
    case 'CACHE':
    case 'INVERSE_CACHE':
      return '• Cache Hit';
    case 'HISTORY':
    case 'INVERSE_HISTORY':
    case 'DIRECT':
      return '• Database';
    case 'DERIVED':
      return '• Derived';
    case 'REFRESHED':
    case 'HISTORY/CACHE':
      return '• Refreshed';
    default:
      return `• ${source}`;
  }
};

const LastLookupCard = ({ currencyPair, exchangeRate, source }) => {
  return (
    <div className="mt-6 pt-4 border-t border-gray-100 animate-fade-in-up">
      <h4 className="text-xs font-semibold text-neutral uppercase tracking-wider mb-3">LAST LOOKUP RESULT</h4>
      <div className="flex justify-between items-center bg-gray-50/50 p-4 rounded-md border border-gray-100 transition-all">
        <div className="flex flex-col">
          <span className="text-xs text-neutral mb-1">Currency Pair</span>
          <span className="font-semibold text-primary">
            {currencyPair || '--'}
          </span>
        </div>
        <div className="flex flex-col items-center">
          <span className="text-xs text-neutral mb-1">Live Rate</span>
          <span className="font-semibold text-primary">
            {exchangeRate ? exchangeRate.toFixed(6) : '--'}
          </span>
        </div>
        <div className="flex flex-col items-end">
          <span className="text-xs text-neutral mb-1">Method</span>
          <span className="font-semibold text-primary">
            {formatMethod(source)}
          </span>
        </div>
        <div className="bg-gray-200 p-2 rounded text-tertiary">
          <BarChart2 size={16} />
        </div>
      </div>
    </div>
  );
};

export default LastLookupCard;
