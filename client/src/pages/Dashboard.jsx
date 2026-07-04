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
  const [addMode, setAddMode] = useState('direct');

  // Get Rate Form State
  const [sourceCurrency, setSourceCurrency] = useState('');
  const [lookupTargetCurrency, setLookupTargetCurrency] = useState('');
  const [isLookingUp, setIsLookingUp] = useState(false);
  const [lookupResult, setLookupResult] = useState(null);
  const [lookupId, setLookupId] = useState(0);
  const [lookupSwap, setLookupSwap] = useState(false);

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
    if (targetCurrency.length !== 3) {
      toast.error('Currency code must be exactly 3 letters');
      return;
    }
    
    setIsAdding(true);
    try {
      const base = addMode === 'direct' ? 'INR' : targetCurrency;
      const target = addMode === 'direct' ? targetCurrency : 'INR';
      await addExchangeRate(base.toUpperCase(), target.toUpperCase(), parseFloat(rate));
      toast.success(`Successfully added rate`);
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
                
                <button 
                  type="button" 
                  onClick={() => {
                    setLookupSwap(p => !p);
                    const temp = sourceCurrency;
                    setSourceCurrency(lookupTargetCurrency);
                    setLookupTargetCurrency(temp);
                  }}
                  className="absolute left-1/2 top-1/2 transform -translate-x-1/2 translate-y-1 bg-white border border-gray-200 rounded-full p-1.5 hover:bg-gray-50 transition-all duration-300 z-10 cursor-pointer shadow-sm"
                >
                  <div className={`transition-transform duration-300 ${lookupSwap ? 'rotate-180' : ''}`}>
                    ⇄
                  </div>
                </button>

                <Input 
                  label="Target Currency" 
                  placeholder="e.g. EUR" 
                  value={lookupTargetCurrency}
                  onChange={(e) => setLookupTargetCurrency(formatCurrency(e.target.value))}
                  maxLength={3}
                />
              </div>
              <p className="text-xs text-tertiary">
                Enter any two supported currencies. The application automatically checks Cache, History, Inverse Rate, and finally derives a Cross Rate if required.
              </p>
              <Button type="submit" className="m-auto w-96 text-primary border border-gray-200" disabled={isLookingUp}>
                {isLookingUp ? 'LOADING...' : 'GET RATE'}
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

          <Card title={<div className="flex items-center gap-2"><PlusCircle size={20} /> Add Exchange Rate</div>}>
            <form onSubmit={handleAddRate} className="space-y-4">
              <div className="grid grid-cols-2 gap-4 items-end relative">
                <Input 
                  label="Base Currency" 
                  value={addMode === 'direct' ? 'INR' : targetCurrency} 
                  disabled={addMode === 'direct'} 
                  className={addMode === 'direct' ? 'bg-gray-100 opacity-75' : ''}
                  onChange={addMode === 'inverse' ? (e) => setTargetCurrency(formatCurrency(e.target.value)) : undefined}
                  maxLength={3}
                  placeholder={addMode === 'inverse' ? 'e.g. USD' : ''}
                />
                
                <button 
                  type="button" 
                  onClick={() => { setAddMode(p => p === 'direct' ? 'inverse' : 'direct'); setTargetCurrency(''); setRate(''); }}
                  className="absolute left-1/2 top-1/2 transform -translate-x-1/2 translate-y-1 bg-white border border-gray-200 rounded-full p-1.5 hover:bg-gray-50 transition-all duration-300 z-10 cursor-pointer shadow-sm"
                >
                  <div className={`transition-transform duration-300 ${addMode === 'inverse' ? 'rotate-180' : ''}`}>
                    ⇄
                  </div>
                </button>

                <Input 
                  label="Target Currency" 
                  value={addMode === 'direct' ? targetCurrency : 'INR'} 
                  disabled={addMode === 'inverse'}
                  className={addMode === 'inverse' ? 'bg-gray-100 opacity-75' : ''}
                  onChange={addMode === 'direct' ? (e) => setTargetCurrency(formatCurrency(e.target.value)) : undefined}
                  maxLength={3}
                  placeholder={addMode === 'direct' ? 'e.g. USD' : ''}
                />
              </div>
              <p className="text-xs text-tertiary">
                Exchange Rate Format: {addMode === 'direct' ? '1 INR = ? Target Currency' : '1 Base Currency = ? INR'}
              </p>
              <Input 
                label={`Exchange Rate (1 ${addMode === 'direct' ? 'INR' : targetCurrency || 'Base'} = ? ${addMode === 'direct' ? targetCurrency || 'Target' : 'INR'})`}
                placeholder="e.g. 0.0123" 
                type="number"
                step="0.000001"
                value={rate}
                onChange={(e) => setRate(e.target.value)}
              />
              <Button type="submit" className=" m-auto w-96 " disabled={isAdding}>
                {isAdding ? 'ADDING...' : 'ADD RATE'}
              </Button>
            </form>
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
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
