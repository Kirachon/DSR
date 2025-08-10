'use client';

import Link from 'next/link';
import React, { useState } from 'react';
import { 
  User, 
  FileText, 
  CreditCard, 
  CheckCircle, 
  Clock,
  ArrowLeft,
  Filter,
  Calendar,
  Search
} from 'lucide-react';

export default function ActivityPage() {
  const [filter, setFilter] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');

  const activities = [
    {
      id: 1,
      action: 'Profile Updated',
      description: 'Personal information and contact details updated',
      time: '2 hours ago',
      date: '2024-01-15',
      status: 'completed',
      icon: <User className="h-5 w-5" />,
      category: 'profile'
    },
    {
      id: 2,
      action: 'Benefit Application Submitted',
      description: 'Application for Pantawid Pamilyang Pilipino Program submitted',
      time: '1 day ago',
      date: '2024-01-14',
      status: 'pending',
      icon: <FileText className="h-5 w-5" />,
      category: 'application'
    },
    {
      id: 3,
      action: 'Payment Received',
      description: 'Monthly benefit payment of ₱3,000 received',
      time: '3 days ago',
      date: '2024-01-12',
      status: 'completed',
      icon: <CreditCard className="h-5 w-5" />,
      category: 'payment'
    },
    {
      id: 4,
      action: 'Document Verified',
      description: 'Birth certificate and ID documents verified',
      time: '1 week ago',
      date: '2024-01-08',
      status: 'completed',
      icon: <CheckCircle className="h-5 w-5" />,
      category: 'verification'
    },
    {
      id: 5,
      action: 'Eligibility Assessment',
      description: 'Household eligibility assessment completed',
      time: '2 weeks ago',
      date: '2024-01-01',
      status: 'completed',
      icon: <CheckCircle className="h-5 w-5" />,
      category: 'assessment'
    },
    {
      id: 6,
      action: 'Registration Started',
      description: 'Initial household registration process started',
      time: '3 weeks ago',
      date: '2023-12-25',
      status: 'completed',
      icon: <User className="h-5 w-5" />,
      category: 'registration'
    }
  ];

  const filterOptions = [
    { value: 'all', label: 'All Activities' },
    { value: 'profile', label: 'Profile' },
    { value: 'application', label: 'Applications' },
    { value: 'payment', label: 'Payments' },
    { value: 'verification', label: 'Verification' },
    { value: 'assessment', label: 'Assessment' },
    { value: 'registration', label: 'Registration' }
  ];

  const filteredActivities = activities.filter(activity => {
    const matchesFilter = filter === 'all' || activity.category === filter;
    const matchesSearch = activity.action.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         activity.description.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesFilter && matchesSearch;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'pending':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'failed':
        return 'bg-red-100 text-red-800 border-red-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed':
        return <CheckCircle className="h-4 w-4" />;
      case 'pending':
        return <Clock className="h-4 w-4" />;
      default:
        return <Clock className="h-4 w-4" />;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-professional-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="text-2xl font-bold text-primary-700">DSR</div>
              </div>
              <div className="ml-4">
                <h1 className="text-2xl font-semibold text-gray-900">Activity History</h1>
                <p className="text-sm text-gray-500">Track all your DSR account activities</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <Link
                href="/dashboard"
                className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
              >
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back to Dashboard
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Filters and Search */}
        <div className="bg-white rounded-md shadow-professional-sm border border-gray-200 p-6 mb-8">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-4 sm:space-y-0">
            {/* Search */}
            <div className="relative flex-1 max-w-md">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Search className="h-5 w-5 text-gray-400" />
              </div>
              <input
                type="text"
                placeholder="Search activities..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-sm leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-primary-500 focus:border-primary-500"
              />
            </div>

            {/* Filter */}
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                <Filter className="h-5 w-5 text-gray-400" />
                <select
                  value={filter}
                  onChange={(e) => setFilter(e.target.value)}
                  className="block w-full pl-3 pr-10 py-2 text-base border border-gray-300 focus:outline-none focus:ring-primary-500 focus:border-primary-500 rounded-sm"
                >
                  {filterOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>
        </div>

        {/* Activity List */}
        <div className="bg-white rounded-md shadow-professional-sm border border-gray-200">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">
              Recent Activities ({filteredActivities.length})
            </h3>
          </div>
          
          <div className="divide-y divide-gray-200">
            {filteredActivities.length > 0 ? (
              filteredActivities.map((activity) => (
                <div key={activity.id} className="p-6 hover:bg-gray-50 transition-colors duration-150">
                  <div className="flex items-start space-x-4">
                    <div className="flex-shrink-0 p-2 bg-primary-100 rounded-md">
                      {activity.icon}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-start justify-between">
                        <div>
                          <h4 className="text-lg font-semibold text-gray-900">
                            {activity.action}
                          </h4>
                          <p className="text-gray-600 mt-1">
                            {activity.description}
                          </p>
                          <div className="flex items-center mt-2 space-x-4 text-sm text-gray-500">
                            <div className="flex items-center">
                              <Calendar className="h-4 w-4 mr-1" />
                              {activity.date}
                            </div>
                            <span>{activity.time}</span>
                          </div>
                        </div>
                        <div className={`inline-flex items-center px-3 py-1 rounded-sm text-sm font-medium border ${getStatusColor(activity.status)}`}>
                          {getStatusIcon(activity.status)}
                          <span className="ml-1 capitalize">{activity.status}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="p-12 text-center">
                <div className="text-gray-400 mb-4">
                  <Search className="h-12 w-12 mx-auto" />
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">No activities found</h3>
                <p className="text-gray-500">
                  Try adjusting your search terms or filters to find what you're looking for.
                </p>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
