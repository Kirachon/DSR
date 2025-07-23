'use client';

import React from 'react';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { User, LogOut, FileText, CreditCard, CheckCircle } from 'lucide-react';

export default function Dashboard() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Wide centered container - optimized for 1920x1200 */}
      <div className="max-w-[1600px] mx-auto px-8 sm:px-12 lg:px-16 py-8">
        
        {/* Header */}
        <header className="bg-blue-600 text-white rounded-lg p-8 mb-8 text-center">
          <h1 className="text-3xl font-bold mb-2">Digital Social Registry</h1>
          <p className="text-blue-100 text-lg">Dashboard</p>
          <div className="mt-6 flex justify-center gap-4">
            <Button variant="outline" size="md" className="text-white border-white/30 hover:bg-white/10">
              <User className="w-4 h-4 mr-2" />
              Profile
            </Button>
            <Button variant="outline" size="md" className="text-white border-white/30 hover:bg-white/10">
              <LogOut className="w-4 h-4 mr-2" />
              Logout
            </Button>
          </div>
        </header>

        {/* Welcome */}
        <section className="bg-white rounded-lg p-8 mb-8 text-center shadow-sm">
          <h2 className="text-3xl font-bold text-gray-900 mb-4">Welcome to Your Dashboard</h2>
          <p className="text-lg text-gray-600 max-w-3xl mx-auto">
            Manage your social protection programs, track benefits, and access government services.
          </p>
        </section>

        {/* Stats */}
        <section className="mb-10">
          <h3 className="text-2xl font-bold text-gray-900 mb-8 text-center">Your Account Overview</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-12 max-w-[1400px] mx-auto">
            <Card className="p-8 text-center shadow-lg hover:shadow-xl transition-shadow">
              <div className="inline-flex p-5 rounded-full mb-6 bg-green-100 text-green-600">
                <User className="w-8 h-8" />
              </div>
              <p className="text-base text-gray-500 mb-3">Registration Status</p>
              <p className="text-3xl font-bold text-gray-900 mb-2">Active</p>
              <p className="text-sm text-gray-600">+2.5%</p>
            </Card>
            
            <Card className="p-8 text-center shadow-lg hover:shadow-xl transition-shadow">
              <div className="inline-flex p-5 rounded-full mb-6 bg-blue-100 text-blue-600">
                <CreditCard className="w-8 h-8" />
              </div>
              <p className="text-base text-gray-500 mb-3">Benefits Received</p>
              <p className="text-3xl font-bold text-gray-900 mb-2">₱15,000</p>
              <p className="text-sm text-gray-600">+12%</p>
            </Card>

            <Card className="p-8 text-center shadow-lg hover:shadow-xl transition-shadow">
              <div className="inline-flex p-5 rounded-full mb-6 bg-yellow-100 text-yellow-600">
                <FileText className="w-8 h-8" />
              </div>
              <p className="text-base text-gray-500 mb-3">Pending Applications</p>
              <p className="text-3xl font-bold text-gray-900 mb-2">2</p>
              <p className="text-sm text-gray-600">-1</p>
            </Card>

            <Card className="p-8 text-center shadow-lg hover:shadow-xl transition-shadow">
              <div className="inline-flex p-5 rounded-full mb-6 bg-purple-100 text-purple-600">
                <CheckCircle className="w-8 h-8" />
              </div>
              <p className="text-base text-gray-500 mb-3">Case Status</p>
              <p className="text-3xl font-bold text-gray-900 mb-2">Resolved</p>
              <p className="text-sm text-gray-600">Updated</p>
            </Card>
          </div>
        </section>

        {/* Quick Actions */}
        <section className="bg-white rounded-lg p-8 mb-8 shadow-sm">
          <h3 className="text-2xl font-bold text-gray-900 mb-8 text-center">Quick Actions</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-12 max-w-[1200px] mx-auto">
            <Card className="p-8 text-center hover:shadow-lg transition-shadow">
              <div className="inline-flex p-5 rounded-full mb-6 bg-blue-100 text-blue-600">
                <User className="w-8 h-8" />
              </div>
              <h4 className="font-semibold text-gray-900 mb-3 text-xl">Update Profile</h4>
              <p className="text-base text-gray-600 mb-6">Keep your personal information current</p>
              <Button size="lg" className="w-full">Access Service</Button>
            </Card>

            <Card className="p-8 text-center hover:shadow-lg transition-shadow">
              <div className="inline-flex p-5 rounded-full mb-6 bg-gray-100 text-gray-600">
                <FileText className="w-8 h-8" />
              </div>
              <h4 className="font-semibold text-gray-900 mb-3 text-xl">Apply for Benefits</h4>
              <p className="text-base text-gray-600 mb-6">Submit new applications for social programs</p>
              <Button size="lg" className="w-full">Access Service</Button>
            </Card>

            <Card className="p-8 text-center hover:shadow-lg transition-shadow">
              <div className="inline-flex p-5 rounded-full mb-6 bg-purple-100 text-purple-600">
                <CreditCard className="w-8 h-8" />
              </div>
              <h4 className="font-semibold text-gray-900 mb-3 text-xl">Payment History</h4>
              <p className="text-base text-gray-600 mb-6">View your benefit payment records</p>
              <Button size="lg" className="w-full">Access Service</Button>
            </Card>
          </div>
        </section>

        {/* Footer */}
        <footer className="bg-gray-800 text-white rounded-lg p-6 text-center">
          <h4 className="font-bold mb-2">Digital Social Registry</h4>
          <p className="text-gray-300 text-sm mb-4">Empowering citizens through digital transformation</p>
          <p className="text-xs text-gray-400">
            © 2024 Department of Social Welfare and Development. All rights reserved.
          </p>
        </footer>

      </div>
    </div>
  );
}
