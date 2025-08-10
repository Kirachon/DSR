'use client';

import Link from 'next/link';
import React, { useState } from 'react';
import {
  BarChart3,
  PieChart,
  TrendingUp,
  Download,
  Calendar,
  ArrowLeft,
  FileText,
  Users,
  Banknote,
  Activity
} from 'lucide-react';

export default function ReportsPage() {
  const [selectedPeriod, setSelectedPeriod] = useState('monthly');

  const reportTypes = [
    {
      title: 'Benefit Summary',
      description: 'Overview of received benefits and payments',
      icon: <Banknote className="h-6 w-6" />,
      color: 'green',
      data: '₱18,000 total received',
      href: '/reports/benefits'
    },
    {
      title: 'Application Status',
      description: 'Status of all submitted applications',
      icon: <FileText className="h-6 w-6" />,
      color: 'blue',
      data: '3 active applications',
      href: '/reports/applications'
    },
    {
      title: 'Household Overview',
      description: 'Household member information and status',
      icon: <Users className="h-6 w-6" />,
      color: 'purple',
      data: '5 household members',
      href: '/reports/household'
    },
    {
      title: 'Activity Report',
      description: 'Detailed activity and transaction history',
      icon: <Activity className="h-6 w-6" />,
      color: 'orange',
      data: '24 activities this month',
      href: '/reports/activity'
    }
  ];

  const quickStats = [
    {
      label: 'Total Benefits Received',
      value: '₱18,000',
      change: '+12%',
      trend: 'up'
    },
    {
      label: 'Active Applications',
      value: '3',
      change: '+1',
      trend: 'up'
    },
    {
      label: 'Completed Verifications',
      value: '8',
      change: '+2',
      trend: 'up'
    },
    {
      label: 'Program Enrollments',
      value: '2',
      change: '0',
      trend: 'neutral'
    }
  ];

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
                <h1 className="text-2xl font-semibold text-gray-900">Reports & Analytics</h1>
                <p className="text-sm text-gray-500">View detailed reports and analytics</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <select
                value={selectedPeriod}
                onChange={(e) => setSelectedPeriod(e.target.value)}
                className="block w-full pl-3 pr-10 py-2 text-base border border-gray-300 focus:outline-none focus:ring-primary-500 focus:border-primary-500 rounded-sm"
              >
                <option value="weekly">This Week</option>
                <option value="monthly">This Month</option>
                <option value="quarterly">This Quarter</option>
                <option value="yearly">This Year</option>
              </select>
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
        {/* Quick Stats */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {quickStats.map((stat, index) => (
            <div key={index} className="bg-white rounded-md shadow-professional-sm border border-gray-200 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-500">{stat.label}</p>
                  <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
                </div>
                <div className={`flex items-center text-sm font-medium ${
                  stat.trend === 'up' ? 'text-green-600' :
                  stat.trend === 'down' ? 'text-red-600' :
                  'text-gray-500'
                }`}>
                  {stat.trend === 'up' && <TrendingUp className="h-4 w-4 mr-1" />}
                  {stat.change}
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Report Types */}
        <div className="mb-8">
          <h2 className="text-xl font-bold text-gray-900 mb-6">Available Reports</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {reportTypes.map((report, index) => (
              <Link key={index} href={report.href} className="group">
                <div className="bg-white rounded-md shadow-professional-sm border border-gray-200 p-6 hover:shadow-professional-md transition-all duration-200 group-hover:border-primary-300">
                  <div className={`inline-flex p-3 rounded-md mb-4 ${
                    report.color === 'green' ? 'bg-green-100 text-green-600' :
                    report.color === 'blue' ? 'bg-blue-100 text-blue-600' :
                    report.color === 'purple' ? 'bg-purple-100 text-purple-600' :
                    'bg-orange-100 text-orange-600'
                  }`}>
                    {report.icon}
                  </div>
                  <h3 className="text-lg font-semibold text-gray-900 mb-2 group-hover:text-primary-600">
                    {report.title}
                  </h3>
                  <p className="text-gray-600 text-sm mb-3">
                    {report.description}
                  </p>
                  <p className="text-sm font-medium text-primary-600">
                    {report.data}
                  </p>
                </div>
              </Link>
            ))}
          </div>
        </div>

        {/* Charts Section */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Benefits Chart */}
          <div className="bg-white rounded-md shadow-professional-sm border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">Monthly Benefits</h3>
              <BarChart3 className="h-5 w-5 text-gray-400" />
            </div>
            <div className="h-64 flex items-center justify-center bg-gray-50 rounded-md">
              <div className="text-center">
                <BarChart3 className="h-12 w-12 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500">Chart visualization would appear here</p>
                <p className="text-sm text-gray-400">Integration with charting library needed</p>
              </div>
            </div>
          </div>

          {/* Application Status Chart */}
          <div className="bg-white rounded-md shadow-professional-sm border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">Application Status</h3>
              <PieChart className="h-5 w-5 text-gray-400" />
            </div>
            <div className="h-64 flex items-center justify-center bg-gray-50 rounded-md">
              <div className="text-center">
                <PieChart className="h-12 w-12 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500">Chart visualization would appear here</p>
                <p className="text-sm text-gray-400">Integration with charting library needed</p>
              </div>
            </div>
          </div>
        </div>

        {/* Recent Reports */}
        <div className="bg-white rounded-md shadow-professional-sm border border-gray-200">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">Recent Reports</h3>
          </div>
          <div className="p-12 text-center">
            <div className="text-gray-400 mb-4">
              <FileText className="h-12 w-12 mx-auto" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">No reports generated yet</h3>
            <p className="text-gray-500">
              Generate your first report using the available report types above.
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}
