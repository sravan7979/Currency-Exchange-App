import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';

import Sidebar from './components/layout/Sidebar';
import Header from './components/layout/Header';
import Dashboard from './pages/Dashboard';
import CacheEntries from './pages/CacheEntries';
import History from './pages/History';
import Statistics from './pages/Statistics';

function App() {
  return (
    <BrowserRouter>
      <div className="flex min-h-screen bg-white">
        <Sidebar />
        <div className="flex-1 flex flex-col min-w-0">
          <Header />
          <main className="flex-1 overflow-auto p-8">
            <div className="max-w-6xl mx-auto">
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/cache" element={<CacheEntries />} />
                <Route path="/history" element={<History />} />
                <Route path="/statistics" element={<Statistics />} />
              </Routes>
            </div>
          </main>
        </div>
      </div>
      <Toaster position="bottom-right" />
    </BrowserRouter>
  );
}

export default App;
