'use client';

import Link from 'next/link';
import React from 'react';
import { 
  HelpCircle, 
  Phone, 
  Mail, 
  MessageCircle, 
  FileText, 
  Clock,
  MapPin,
  ArrowLeft
} from 'lucide-react';

export default function SupportPage() {
  const supportOptions = [
    {
      title: 'Frequently Asked Questions',
      description: 'Find answers to common questions about DSR services',
      icon: <HelpCircle className="h-6 w-6" />,
      href: '/support/faq',
      color: 'blue'
    },
    {
      title: 'Contact Support',
      description: 'Get direct assistance from our support team',
      icon: <MessageCircle className="h-6 w-6" />,
      href: '/support/contact',
      color: 'green'
    },
    {
      title: 'User Guide',
      description: 'Step-by-step guides for using DSR services',
      icon: <FileText className="h-6 w-6" />,
      href: '/support/guide',
      color: 'purple'
    },
    {
      title: 'Service Status',
      description: 'Check current status of DSR services',
      icon: <Clock className="h-6 w-6" />,
      href: '/support/status',
      color: 'orange'
    }
  ];

  const contactInfo = [
    {
      type: 'Phone',
      value: '+63 (2) 8931-8101',
      icon: <Phone className="h-5 w-5" />,
      description: 'Monday to Friday, 8:00 AM - 5:00 PM'
    },
    {
      type: 'Email',
      value: 'support@dsr.gov.ph',
      icon: <Mail className="h-5 w-5" />,
      description: 'Response within 24 hours'
    },
    {
      type: 'Office',
      value: 'DSWD Central Office',
      icon: <MapPin className="h-5 w-5" />,
      description: 'Batasan Pambansa Complex, Quezon City'
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
                <h1 className="text-2xl font-semibold text-gray-900">Help & Support</h1>
                <p className="text-sm text-gray-500">Get assistance with your DSR account</p>
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
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Hero Section */}
        <div className="text-center mb-12">
          <h2 className="text-3xl font-bold text-gray-900 mb-4">
            How can we help you today?
          </h2>
          <p className="text-lg text-gray-600 max-w-2xl mx-auto">
            Find answers to your questions, get support, or learn how to use DSR services effectively.
          </p>
        </div>

        {/* Support Options Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
          {supportOptions.map((option, index) => (
            <Link key={index} href={option.href} className="group">
              <div className="bg-white rounded-md shadow-professional-sm border border-gray-200 p-6 hover:shadow-professional-md transition-all duration-200 group-hover:border-primary-300">
                <div className={`inline-flex p-3 rounded-md mb-4 ${
                  option.color === 'blue' ? 'bg-blue-100 text-blue-600' :
                  option.color === 'green' ? 'bg-green-100 text-green-600' :
                  option.color === 'purple' ? 'bg-purple-100 text-purple-600' :
                  'bg-orange-100 text-orange-600'
                }`}>
                  {option.icon}
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2 group-hover:text-primary-600">
                  {option.title}
                </h3>
                <p className="text-gray-600 text-sm">
                  {option.description}
                </p>
              </div>
            </Link>
          ))}
        </div>

        {/* Contact Information */}
        <div className="bg-white rounded-md shadow-professional-sm border border-gray-200 p-8">
          <h3 className="text-2xl font-bold text-gray-900 mb-6">Contact Information</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {contactInfo.map((contact, index) => (
              <div key={index} className="flex items-start space-x-4">
                <div className="flex-shrink-0 p-2 bg-primary-100 rounded-md">
                  {contact.icon}
                </div>
                <div>
                  <h4 className="text-lg font-semibold text-gray-900">{contact.type}</h4>
                  <p className="text-primary-600 font-medium">{contact.value}</p>
                  <p className="text-sm text-gray-500 mt-1">{contact.description}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Emergency Notice */}
        <div className="mt-8 bg-blue-50 border border-blue-200 rounded-md p-6">
          <div className="flex">
            <div className="flex-shrink-0">
              <HelpCircle className="h-5 w-5 text-blue-400" />
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-blue-800">
                Need Immediate Assistance?
              </h3>
              <div className="mt-2 text-sm text-blue-700">
                <p>
                  For urgent matters related to benefit payments or emergency assistance,
                  please call our hotline at <strong>+63 (2) 8931-8101</strong> during business hours.
                </p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
