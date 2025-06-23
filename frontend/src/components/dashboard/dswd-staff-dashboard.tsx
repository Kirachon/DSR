'use client';

// DSWD Staff Dashboard Component
// Dashboard interface for Department of Social Welfare and Development staff
// Provides program oversight, policy implementation tracking, and administrative tools

import React from 'react';
import Link from 'next/link';

import { Card, Button, Alert } from '@/components/ui';
import type { User } from '@/types';

// Dashboard props interface
interface DSWDStaffDashboardProps {
  user: User;
}

// Metric card interface
interface MetricCard {
  title: string;
  value: string | number;
  change: string;
  trend: 'up' | 'down' | 'neutral';
  icon: React.ReactNode;
}

// Program status interface
interface ProgramStatus {
  id: string;
  name: string;
  status: 'active' | 'suspended' | 'under-review';
  beneficiaries: number;
  coverage: string;
  lastUpdated: string;
}

// Regional data interface
interface RegionalData {
  region: string;
  registrations: number;
  activePrograms: number;
  coverage: string;
  trend: 'up' | 'down' | 'neutral';
}

// Icons
const ProgramIcon = () => (
  <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
  </svg>
);

const BeneficiaryIcon = () => (
  <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
  </svg>
);

const RegionIcon = () => (
  <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
  </svg>
);

const AnalyticsIcon = () => (
  <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
  </svg>
);

const PolicyIcon = () => (
  <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
);

const ReportIcon = () => (
  <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
  </svg>
);

const TrendUpIcon = () => (
  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
  </svg>
);

const TrendDownIcon = () => (
  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6" />
  </svg>
);

// DSWD-specific metrics
const metrics: MetricCard[] = [
  {
    title: 'Active Programs',
    value: '47',
    change: '+3 this quarter',
    trend: 'up',
    icon: <ProgramIcon />,
  },
  {
    title: 'Total Beneficiaries',
    value: '2.4M',
    change: '+125K this month',
    trend: 'up',
    icon: <BeneficiaryIcon />,
  },
  {
    title: 'Regional Coverage',
    value: '17/17',
    change: '100% coverage',
    trend: 'neutral',
    icon: <RegionIcon />,
  },
  {
    title: 'Program Compliance',
    value: '94.2%',
    change: '+2.1% from last month',
    trend: 'up',
    icon: <PolicyIcon />,
  },
];

// Sample program status data
const programStatuses: ProgramStatus[] = [
  {
    id: '1',
    name: 'Pantawid Pamilyang Pilipino Program (4Ps)',
    status: 'active',
    beneficiaries: 4200000,
    coverage: '85.3%',
    lastUpdated: '2 hours ago',
  },
  {
    id: '2',
    name: 'Social Pension for Indigent Senior Citizens',
    status: 'active',
    beneficiaries: 3100000,
    coverage: '78.9%',
    lastUpdated: '4 hours ago',
  },
  {
    id: '3',
    name: 'Sustainable Livelihood Program',
    status: 'under-review',
    beneficiaries: 850000,
    coverage: '62.1%',
    lastUpdated: '1 day ago',
  },
];

// Sample regional data
const regionalData: RegionalData[] = [
  { region: 'NCR', registrations: 245000, activePrograms: 12, coverage: '92.1%', trend: 'up' },
  { region: 'Region IV-A', registrations: 198000, activePrograms: 11, coverage: '88.7%', trend: 'up' },
  { region: 'Region III', registrations: 167000, activePrograms: 10, coverage: '85.3%', trend: 'neutral' },
  { region: 'Region VII', registrations: 143000, activePrograms: 9, coverage: '82.9%', trend: 'down' },
];

// DSWD Staff Dashboard component
export const DSWDStaffDashboard: React.FC<DSWDStaffDashboardProps> = ({ user }) => {
  return (
    <div className="space-y-6">
      {/* Program Overview Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {metrics.map((metric, index) => (
          <Card key={index} className="p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0 p-3 bg-primary-100 text-primary-600 rounded-lg">
                {metric.icon}
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">{metric.title}</p>
                <p className="text-2xl font-bold text-gray-900">{metric.value}</p>
                <p className={`text-sm flex items-center ${
                  metric.trend === 'up' ? 'text-success-600' :
                  metric.trend === 'down' ? 'text-error-600' :
                  'text-gray-600'
                }`}>
                  {metric.trend === 'up' && <TrendUpIcon />}
                  {metric.trend === 'down' && <TrendDownIcon />}
                  <span className="ml-1">{metric.change}</span>
                </p>
              </div>
            </div>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Quick Actions */}
        <Card className="p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h2>
          <div className="space-y-3">
            <Link href="/programs/management">
              <div className="flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer">
                <div className="flex-shrink-0 p-2 bg-primary-100 text-primary-600 rounded-lg">
                  <ProgramIcon />
                </div>
                <div className="ml-3">
                  <p className="font-medium text-gray-900">Program Management</p>
                  <p className="text-sm text-gray-600">Oversee and configure social programs</p>
                </div>
              </div>
            </Link>

            <Link href="/beneficiaries/oversight">
              <div className="flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer">
                <div className="flex-shrink-0 p-2 bg-success-100 text-success-600 rounded-lg">
                  <BeneficiaryIcon />
                </div>
                <div className="ml-3">
                  <p className="font-medium text-gray-900">Beneficiary Oversight</p>
                  <p className="text-sm text-gray-600">Monitor beneficiary status and compliance</p>
                </div>
              </div>
            </Link>

            <Link href="/analytics/reports">
              <div className="flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer">
                <div className="flex-shrink-0 p-2 bg-accent-100 text-accent-600 rounded-lg">
                  <ReportIcon />
                </div>
                <div className="ml-3">
                  <p className="font-medium text-gray-900">Generate Reports</p>
                  <p className="text-sm text-gray-600">Create program performance reports</p>
                </div>
              </div>
            </Link>

            <Link href="/policy/implementation">
              <div className="flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer">
                <div className="flex-shrink-0 p-2 bg-warning-100 text-warning-600 rounded-lg">
                  <PolicyIcon />
                </div>
                <div className="ml-3">
                  <p className="font-medium text-gray-900">Policy Implementation</p>
                  <p className="text-sm text-gray-600">Track policy compliance and updates</p>
                </div>
              </div>
            </Link>
          </div>
        </Card>

        {/* Program Status Overview */}
        <Card className="p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Program Status</h2>
          <div className="space-y-4">
            {programStatuses.map((program) => (
              <div key={program.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <div className="flex-1">
                  <div className="flex items-center space-x-2">
                    <p className="font-medium text-gray-900 text-sm">{program.name}</p>
                    <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                      program.status === 'active' ? 'bg-success-100 text-success-800' :
                      program.status === 'suspended' ? 'bg-error-100 text-error-800' :
                      'bg-warning-100 text-warning-800'
                    }`}>
                      {program.status.replace('-', ' ')}
                    </span>
                  </div>
                  <div className="flex items-center space-x-4 mt-1">
                    <p className="text-xs text-gray-500">
                      {program.beneficiaries.toLocaleString()} beneficiaries
                    </p>
                    <p className="text-xs text-gray-500">
                      {program.coverage} coverage
                    </p>
                    <p className="text-xs text-gray-500">
                      Updated {program.lastUpdated}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>

      {/* Regional Performance Overview */}
      <Card className="p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Regional Performance</h2>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Region
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Registrations
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Active Programs
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Coverage
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Trend
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {regionalData.map((region, index) => (
                <tr key={index} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {region.region}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {region.registrations.toLocaleString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {region.activePrograms}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {region.coverage}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <div className={`flex items-center ${
                      region.trend === 'up' ? 'text-success-600' :
                      region.trend === 'down' ? 'text-error-600' :
                      'text-gray-600'
                    }`}>
                      {region.trend === 'up' && <TrendUpIcon />}
                      {region.trend === 'down' && <TrendDownIcon />}
                      <span className="ml-1 capitalize">{region.trend}</span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      {/* Recent Policy Updates and System Activity */}
      <Card className="p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Recent Activity</h2>
        <div className="space-y-4">
          <div className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
            <div className="flex-shrink-0 w-2 h-2 bg-success-500 rounded-full"></div>
            <div className="flex-1">
              <p className="text-sm font-medium text-gray-900">Updated 4Ps eligibility criteria</p>
              <p className="text-xs text-gray-500">Policy implementation completed - 2 hours ago</p>
            </div>
          </div>
          <div className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
            <div className="flex-shrink-0 w-2 h-2 bg-primary-500 rounded-full"></div>
            <div className="flex-1">
              <p className="text-sm font-medium text-gray-900">Generated quarterly beneficiary report</p>
              <p className="text-xs text-gray-500">Report distributed to regional offices - 4 hours ago</p>
            </div>
          </div>
          <div className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
            <div className="flex-shrink-0 w-2 h-2 bg-warning-500 rounded-full"></div>
            <div className="flex-1">
              <p className="text-sm font-medium text-gray-900">Reviewed program compliance audit</p>
              <p className="text-xs text-gray-500">3 regions require follow-up actions - 1 day ago</p>
            </div>
          </div>
          <div className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
            <div className="flex-shrink-0 w-2 h-2 bg-accent-500 rounded-full"></div>
            <div className="flex-1">
              <p className="text-sm font-medium text-gray-900">Approved new program budget allocation</p>
              <p className="text-xs text-gray-500">₱2.4B allocated for disaster response programs - 2 days ago</p>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default DSWDStaffDashboard;
