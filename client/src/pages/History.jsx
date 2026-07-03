import React, { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import Card from '../components/common/Card';
import { getHistory } from '../services/api';

const History = () => {
  const [history, setHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const response = await getHistory();
        setHistory(response);
      } catch (error) {
        toast.error('Failed to fetch history');
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchHistory();
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold text-primary mb-2">History</h2>
        <p className="text-neutral">Persistent log of all direct exchange rate updates.</p>
      </div>

      <Card>
        {isLoading ? (
          <div className="py-8 text-center text-neutral">Loading history...</div>
        ) : history.length === 0 ? (
          <div className="py-8 text-center text-neutral">No history records found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="text-xs text-tertiary uppercase bg-gray-50/50">
                <tr>
                  <th className="px-6 py-4 font-medium rounded-tl-md">ID</th>
                  <th className="px-6 py-4 font-medium">Base</th>
                  <th className="px-6 py-4 font-medium">Target</th>
                  <th className="px-6 py-4 font-medium">Rate</th>
                  <th className="px-6 py-4 font-medium rounded-tr-md">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {history.map((record) => (
                  <tr key={record.id} className="hover:bg-gray-50/50 transition-colors">
                    <td className="px-6 py-4 text-tertiary">#{record.id}</td>
                    <td className="px-6 py-4 font-medium text-primary">{record.baseCurrency}</td>
                    <td className="px-6 py-4 font-medium text-primary">{record.targetCurrency}</td>
                    <td className="px-6 py-4 text-tertiary">{record.exchangeRate.toFixed(6)}</td>
                    <td className="px-6 py-4 text-tertiary">
                      {new Date(record.createdAt).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
};

export default History;
