import React, { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import { Trash2 } from 'lucide-react';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import { getCacheEntries, removeStaleCache, clearCache } from '../services/api';

const CacheEntries = () => {
  const [entries, setEntries] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const fetchEntries = async () => {
    try {
      const response = await getCacheEntries();
      setEntries(response);
    } catch (error) {
      toast.error('Failed to fetch cache entries');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchEntries();
  }, []);

  const handleRemoveStale = async () => {
    try {
      const response = await removeStaleCache();
      toast.success(`Removed ${response.removed} stale entries`);
      fetchEntries();
    } catch (error) {
      toast.error('Failed to remove stale entries');
    }
  };

  const handleClearCache = async () => {
    try {
      await clearCache();
      toast.success('Cache cleared successfully');
      fetchEntries();
    } catch (error) {
      toast.error('Failed to clear cache');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-end">
        <div>
          <h2 className="text-3xl font-bold text-primary mb-2">Cache Entries</h2>
          <p className="text-neutral">View and manage the real-time contents of the in-memory cache.</p>
        </div>
        <div className="flex gap-4">
          <Button variant="outlined" onClick={handleRemoveStale}>
            <Trash2 size={16} /> Remove Stale
          </Button>
          <Button variant="inverted" onClick={handleClearCache}>
            <Trash2 size={16} /> Clear All
          </Button>
        </div>
      </div>

      <Card>
        {isLoading ? (
          <div className="py-8 text-center text-neutral">Loading cache entries...</div>
        ) : entries.length === 0 ? (
          <div className="py-8 text-center text-neutral">The cache is currently empty.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="text-xs text-tertiary uppercase bg-gray-50/50">
                <tr>
                  <th className="px-6 py-4 font-medium rounded-tl-md">Key (Pair)</th>
                  <th className="px-6 py-4 font-medium">Rate</th>
                  <th className="px-6 py-4 font-medium">Age (s)</th>
                  <th className="px-6 py-4 font-medium">Type</th>
                  <th className="px-6 py-4 font-medium rounded-tr-md">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {entries.map((entry) => (
                  <tr key={entry.key} className="hover:bg-gray-50/50 transition-colors">
                    <td className="px-6 py-4 font-medium text-primary">{entry.key}</td>
                    <td className="px-6 py-4 text-tertiary">{entry.rate.toFixed(6)}</td>
                    <td className="px-6 py-4 text-tertiary">{entry.ageSeconds}s</td>
                    <td className="px-6 py-4 text-tertiary">
                      {entry.isDerived ? (
                        <span className="px-2 py-1 bg-gray-100 rounded text-xs">Derived</span>
                      ) : (
                        <span className="px-2 py-1 bg-gray-100 rounded text-xs">Direct</span>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      {entry.isStale ? (
                        <span className="text-red-500 font-medium">Stale</span>
                      ) : (
                        <span className="text-green-500 font-medium">Fresh</span>
                      )}
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

export default CacheEntries;
