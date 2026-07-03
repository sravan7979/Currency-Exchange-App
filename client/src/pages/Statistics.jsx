import React, { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import Card from '../components/common/Card';
import { getCacheStatistics } from '../services/api';

const Statistics = () => {
  const [stats, setStats] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const response = await getCacheStatistics();
        setStats(response);
      } catch (error) {
        toast.error('Failed to fetch statistics');
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchStats();
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold text-primary mb-2">Statistics & Analytics</h2>
        <p className="text-neutral">Detailed breakdown of the cache performance and health metrics.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card title="Performance Metrics">
          {isLoading ? (
            <div className="py-4 text-center text-neutral">Loading...</div>
          ) : (
            <div className="space-y-4">
              <StatRow label="Total Cache Hits" value={stats?.cacheHits} />
              <StatRow label="Total Cache Misses" value={stats?.cacheMisses} />
              <StatRow 
                label="Hit/Miss Ratio" 
                value={stats ? `${(stats.cacheHits / (stats.cacheMisses || 1)).toFixed(2)}` : '0'} 
              />
              <StatRow 
                label="Cache Efficiency" 
                value={stats ? `${((stats.cacheHits / (stats.cacheHits + stats.cacheMisses || 1)) * 100).toFixed(1)}%` : '0%'} 
              />
            </div>
          )}
        </Card>

        <Card title="Storage Metrics">
          {isLoading ? (
            <div className="py-4 text-center text-neutral">Loading...</div>
          ) : (
            <div className="space-y-4">
              <StatRow label="Current Cache Size" value={stats?.currentCacheSize} />
              <StatRow label="Maximum Capacity" value={stats?.maximumCacheSize} />
              <StatRow label="Capacity Utilization" value={stats ? `${Math.round((stats.currentCacheSize / stats.maximumCacheSize) * 100)}%` : '0%'} />
              <StatRow label="Derived Cross-Rates" value={stats?.derivedRates} />
            </div>
          )}
        </Card>

        <Card title="Data Health">
          {isLoading ? (
            <div className="py-4 text-center text-neutral">Loading...</div>
          ) : (
            <div className="space-y-4">
              <StatRow label="Fresh Entries" value={stats?.freshEntries} />
              <StatRow label="Stale Entries" value={stats?.staleEntries} />
              <StatRow label="Total Database Records" value={stats?.historyRecords} />
              <StatRow label="Cache Refresh Count" value={stats?.refreshCount} />
            </div>
          )}
        </Card>
      </div>
    </div>
  );
};

const StatRow = ({ label, value }) => (
  <div className="flex justify-between items-center py-3 border-b border-gray-50 last:border-0 last:pb-0">
    <span className="text-tertiary">{label}</span>
    <span className="font-bold text-primary">{value}</span>
  </div>
);

export default Statistics;
