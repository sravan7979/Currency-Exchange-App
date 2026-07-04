import React, { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import { PlusCircle, Search, BarChart2 } from 'lucide-react';
import Card from '../components/common/Card';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import ProgressBar from '../components/common/ProgressBar';
import LastLookupCard from '../components/common/LastLookupCard';
import { addExchangeRate, resolveExchangeRate, getCacheStatistics } from '../services/api';

const Dashboard = () => {
  const [stats, setStats] = useState(null);
  
  // Add Rate Form State
  const [targetCurrency, setTargetCurrency] = useState('');
  const [rate, setRate] = useState('');
  const [isAdding, setIsAdding] = useState(false);

  // Get Rate Form State
  const [sourceCurrency, setSourceCurrency] = useState('');
  const [lookupTargetCurrency, setLookupTargetCurrency] = useState('');
  const [isLookingUp, setIsLookingUp] = useState(false);
  const [lookupResult, setLookupResult] = useState(null);
  const [lookupId, setLookupId] = useState(0);

  const formatCurrency = (val) => val.trim().replace(/[^A-Za-z]/g, '').toUpperCase().slice(0, 3);

  const fetchStats = async () => {
    try {
      const response = await getCacheStatistics();
      setStats(response);
    } catch (error) {
      console.error("Failed to fetch stats", error);
    }
  };

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, 5000);
    return () => clearInterval(interval);
  }, []);

  const handleAddRate = async (e) => {
    e.preventDefault();
    if (!targetCurrency || !rate) return;
    
    setIsAdding(true);
    try {
      await addExchangeRate(targetCurrency.toUpperCase(), parseFloat(rate));
      toast.success(`Successfully added rate for INR to ${targetCurrency.toUpperCase()}`);
      setTargetCurrency('');
      setRate('');
      fetchStats();
    } catch (error) {
      toast.error(error.message || 'Failed to add exchange rate');
    } finally {
      setIsAdding(false);
    }
  };

  const handleLookupRate = async (e) => {
    e.preventDefault();
    if (!sourceCurrency || !lookupTargetCurrency) return;
    if (sourceCurrency.length !== 3 || lookupTargetCurrency.length !== 3) {
      toast.error('Currency codes must be exactly 3 letters');
      return;
    }
    
    setIsLookingUp(true);
    try {
      const response = await resolveExchangeRate(sourceCurrency.toUpperCase(), lookupTargetCurrency.toUpperCase());
      setLookupResult(response);
      setLookupId(prev => prev + 1);
      toast.success('Rate resolved successfully');
      fetchStats();
    } catch (error) {
      toast.error(error.message || 'Failed to resolve exchange rate');
    } finally {
      setIsLookingUp(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold text-primary mb-2">Overview</h2>
        <p className="text-neutral">System real-time in-memory cache status and exchange management.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card title={<div className="flex items-center gap-2"><PlusCircle size={20} /> Add Exchange Rate</div>}>
            <form onSubmit={handleAddRate} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <Input label="Base Currency" value="INR" disabled className="bg-gray-100 opacity-75" />
                <Input 
                  label="Target Currency" 
                  placeholder="e.g. USD" 
                  value={targetCurrency}
                  onChange={(e) => setTargetCurrency(formatCurrency(e.target.value))}
                  maxLength={3}
                />
              </div>
              <Input 
                label="Exchange Rate (1 INR = ?)" 
                placeholder="e.g. 0.0123" 
                type="number"
                step="0.000001"
                value={rate}
                onChange={(e) => setRate(e.target.value)}
              />
              <Button type="submit" className="w-full" disabled={isAdding}>
                {isAdding ? 'ADDING...' : 'ADD RATE'}
              </Button>
            </form>
          </Card>

          <Card title={<div className="flex items-center gap-2"><Search size={20} /> Get Exchange Rate</div>}>
            <form onSubmit={handleLookupRate} className="space-y-4">
              <div className="grid grid-cols-2 gap-4 items-end relative">
                <Input 
                  label="Source Currency" 
                  placeholder="e.g. USD" 
                  value={sourceCurrency}
                  onChange={(e) => setSourceCurrency(formatCurrency(e.target.value))}
                  maxLength={3}
                />
                <div className="absolute left-1/2 top-1/2 transform -translate-x-1/2 translate-y-1 text-tertiary">
                   ⇄ 
                </div>
                <Input 
                  label="Target Currency" 
                  placeholder="e.g. EUR" 
                  value={lookupTargetCurrency}
                  onChange={(e) => setLookupTargetCurrency(formatCurrency(e.target.value))}
                  maxLength={3}
                />
              </div>
              <Button type="submit" variant="secondary" className="w-full bg-gray-100 hover:bg-gray-200 text-primary border border-gray-200" disabled={isLookingUp}>
                {isLookingUp ? 'LOOKING UP...' : 'GET RATE'}
              </Button>
            </form>
            
            {lookupResult && (
              <LastLookupCard 
                key={lookupId}
                currencyPair={`${lookupResult.baseCurrency} → ${lookupResult.targetCurrency}`}
                exchangeRate={lookupResult.exchangeRate}
                source={lookupResult.source}
              />
            )}
          </Card>
        </div>

        <div className="space-y-6">
          <Card title="Statistics">
            <div className="space-y-4">
              <div className="flex justify-between items-center py-2 border-b border-gray-50">
                <span className="text-tertiary">Cache Hits</span>
                <span className="font-bold text-primary">{stats?.cacheHits || 0}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-gray-50">
                <span className="text-tertiary">Cache Misses</span>
                <span className="font-bold text-primary">{stats?.cacheMisses || 0}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-gray-50">
                <span className="text-tertiary">Hit Rate</span>
                <span className="font-bold text-primary">
                  {stats ? 
                    ((stats.cacheHits / (stats.cacheHits + stats.cacheMisses || 1)) * 100).toFixed(1) + '%' 
                    : '0.0%'}
                </span>
              </div>
              <div className="flex justify-between items-center py-2">
                <span className="text-tertiary">Cache Size</span>
                <span className="font-bold text-primary">{stats?.currentCacheSize || 0}</span>
              </div>
            </div>
          </Card>

          <Card title="Cache Health">
            <div className="space-y-6">
              <ProgressBar 
                label="Fresh Entries" 
                subLabel={`${stats?.freshEntries || 0} entries`} 
                value={stats?.freshEntries || 0} 
                max={stats?.maximumCacheSize || 50} 
                color="bg-primary"
              />
              <ProgressBar 
                label="Stale Entries" 
                subLabel={`${stats?.staleEntries || 0} entries`} 
                value={stats?.staleEntries || 0} 
                max={stats?.maximumCacheSize || 50} 
                color="bg-tertiary"
              />
              
              <div className="mt-4 p-3 bg-gray-50 border border-gray-100 rounded-md flex items-center gap-2 text-sm text-tertiary">
                <svg className="w-4 h-4 text-neutral" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Next cleanup task scheduled in <span className="font-semibold text-primary">14m 22s</span>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
