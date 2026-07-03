import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Database, History, BarChart3 } from 'lucide-react';

const Sidebar = () => {
  const navItems = [
    { path: '/', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/cache', label: 'Cache Entries', icon: Database },
    { path: '/history', label: 'History', icon: History },
    { path: '/statistics', label: 'Statistics', icon: BarChart3 },
  ];

  return (
    <aside className="w-64 bg-[#f9fafb] border-r border-gray-200 h-screen sticky top-0 flex flex-col hidden md:flex">
      <div className="p-6">
        <h1 className="text-xl font-bold text-primary tracking-tight">Currency Cache</h1>
        <p className="text-xs text-neutral mt-1">In-Memory Exchange</p>
      </div>

      <nav className="flex-1 px-4 mt-6">
        <ul className="space-y-1">
          {navItems.map((item) => (
            <li key={item.path}>
              <NavLink
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-md transition-colors text-sm font-medium ${
                    isActive 
                      ? 'bg-gray-100 text-primary' 
                      : 'text-tertiary hover:text-primary hover:bg-gray-50'
                  }`
                }
              >
                <item.icon size={18} className="opacity-80" />
                {item.label}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>

      <div className="p-6 mt-auto">
        <div className="flex items-center gap-2 text-xs text-neutral">
          <div className="w-2 h-2 rounded-full bg-green-500"></div>
          Node Status: Operational
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
