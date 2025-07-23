'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import React, { useEffect, useState } from 'react';

export default function Home() {
  const router = useRouter();
  const [isLoaded, setIsLoaded] = useState(false);

  useEffect(() => {
    setIsLoaded(true);
  }, []);

  return (
    <div className="min-h-screen gradient-bg overflow-hidden">
      {/* Floating Background Elements */}
      <div className="fixed inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-64 h-64 bg-blue-400/10 rounded-full blur-3xl animate-float"></div>
        <div className="absolute top-3/4 right-1/4 w-96 h-96 bg-purple-400/10 rounded-full blur-3xl animate-float" style={{animationDelay: '2s'}}></div>
        <div className="absolute top-1/2 left-3/4 w-48 h-48 bg-pink-400/10 rounded-full blur-3xl animate-float" style={{animationDelay: '4s'}}></div>
      </div>

      {/* Header */}
      <header className="glass-strong sticky top-0 z-50 border-b border-white/20">
        <div className="container">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center space-x-4">
              <div className="w-14 h-14 rounded-2xl gradient-hero flex items-center justify-center shadow-lg animate-float">
                <span className="font-bold text-2xl text-white">DSR</span>
              </div>
              <div>
                <span className="text-2xl font-bold text-gradient">Digital Social Registry</span>
                <p className="text-sm text-gray-600 font-medium">Republic of the Philippines</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <Link href="/login" className="btn btn-secondary">
                <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
                </svg>
                Sign In
              </Link>
              <Link href="/register" className="btn btn-primary btn-lg">
                <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
                </svg>
                Get Started
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className="relative z-10">
        <div className="container py-20">
          <div className={`max-w-6xl mx-auto text-center ${isLoaded ? 'animate-fade-in' : 'opacity-0'}`}>
            {/* Philippine Flag Accent */}
            <div className="flex justify-center mb-8">
              <div className="flex space-x-2">
                <div className="w-4 h-4 rounded-full bg-blue-600 animate-pulse"></div>
                <div className="w-4 h-4 rounded-full bg-red-600 animate-pulse" style={{animationDelay: '0.5s'}}></div>
                <div className="w-4 h-4 rounded-full bg-yellow-500 animate-pulse" style={{animationDelay: '1s'}}></div>
              </div>
            </div>

            <h1 className="text-6xl md:text-8xl font-bold mb-8 text-gradient leading-tight">
              Digital Social
              <span className="block text-gradient-static">Registry</span>
            </h1>
            
            <p className="text-2xl md:text-3xl text-gray-700 mb-12 leading-relaxed max-w-4xl mx-auto font-light">
              Empowering <span className="font-semibold text-gradient-static">Filipino families</span> through 
              innovative digital solutions that ensure equitable access to 
              <span className="font-semibold text-gradient-static"> social protection programs</span> nationwide.
            </p>
            
            <div className="flex flex-col sm:flex-row gap-6 justify-center mb-16">
              <Link href="/register" className="btn btn-primary btn-lg">
                <svg className="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
                </svg>
                Register as Citizen
              </Link>
              <Link href="/login" className="btn btn-secondary btn-lg">
                <svg className="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2-2v2m8 0V6a2 2 0 012 2v6a2 2 0 01-2 2H8a2 2 0 01-2-2V8a2 2 0 012-2V6" />
                </svg>
                Staff Portal
              </Link>
            </div>

            {/* Stats Section */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-20">
              <div className="card-elevated p-8 text-center">
                <div className="text-5xl font-bold text-gradient mb-3">1M+</div>
                <div className="text-gray-600 text-lg font-medium">Registered Citizens</div>
                <div className="text-sm text-green-600 font-semibold mt-2">↗ +15% this month</div>
              </div>
              <div className="card-elevated p-8 text-center">
                <div className="text-5xl font-bold text-gradient mb-3">₱50B+</div>
                <div className="text-gray-600 text-lg font-medium">Benefits Distributed</div>
                <div className="text-sm text-green-600 font-semibold mt-2">↗ +8% this quarter</div>
              </div>
              <div className="card-elevated p-8 text-center">
                <div className="text-5xl font-bold text-gradient mb-3">17</div>
                <div className="text-gray-600 text-lg font-medium">Regions Covered</div>
                <div className="text-sm text-blue-600 font-semibold mt-2">100% Coverage</div>
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Features Section */}
      <section className="py-24 relative">
        <div className="container">
          <div className="text-center mb-20">
            <h2 className="text-5xl font-bold mb-6 text-gradient-static">
              Comprehensive Social Protection Services
            </h2>
            <p className="text-xl text-gray-600 max-w-4xl mx-auto leading-relaxed">
              Our integrated platform streamlines access to government benefits and ensures
              efficient delivery of social protection programs across the Philippines.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="card-elevated p-10 text-center group hover:scale-105 transition-all duration-500">
              <div className="w-20 h-20 gradient-primary rounded-2xl flex items-center justify-center mx-auto mb-8 group-hover:scale-110 transition-transform duration-300">
                <svg className="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <h3 className="text-2xl font-bold mb-4 text-gray-900">Citizen Registration</h3>
              <p className="text-gray-600 leading-relaxed text-lg">
                Streamlined digital registration process that connects Filipino families
                to essential social protection programs and government benefits.
              </p>
            </div>

            <div className="card-elevated p-10 text-center group hover:scale-105 transition-all duration-500">
              <div className="w-20 h-20 gradient-secondary rounded-2xl flex items-center justify-center mx-auto mb-8 group-hover:scale-110 transition-transform duration-300">
                <svg className="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-2xl font-bold mb-4 text-gray-900">Eligibility Assessment</h3>
              <p className="text-gray-600 leading-relaxed text-lg">
                AI-powered assessment system that automatically determines citizen
                eligibility for various social protection programs and benefits.
              </p>
            </div>

            <div className="card-elevated p-10 text-center group hover:scale-105 transition-all duration-500">
              <div className="w-20 h-20 gradient-accent rounded-2xl flex items-center justify-center mx-auto mb-8 group-hover:scale-110 transition-transform duration-300">
                <svg className="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                </svg>
              </div>
              <h3 className="text-2xl font-bold mb-4 text-gray-900">Secure Payments</h3>
              <p className="text-gray-600 leading-relaxed text-lg">
                Bank-grade security for processing social benefit payments and
                program disbursements directly to Filipino families nationwide.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-24 gradient-hero relative overflow-hidden">
        <div className="absolute inset-0 bg-black/20"></div>
        <div className="container relative z-10">
          <div className="max-w-4xl mx-auto text-center text-white">
            <h3 className="text-5xl font-bold mb-8">
              Ready to Transform Lives?
            </h3>
            <p className="text-xl mb-12 leading-relaxed opacity-90">
              Join over 1 million Filipino families already benefiting from our
              comprehensive social protection programs. Start your journey today.
            </p>

            <div className="flex flex-col sm:flex-row gap-6 justify-center mb-12">
              <Link href="/register" className="btn btn-accent btn-lg">
                <svg className="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
                Start Registration
              </Link>
              <Link href="/dashboard" className="btn btn-secondary btn-lg">
                <svg className="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
                View Dashboard
              </Link>
            </div>

            {/* Trust Indicators */}
            <div className="flex flex-wrap justify-center items-center gap-8 text-white/80">
              <div className="flex items-center gap-3">
                <svg className="w-6 h-6 text-green-400" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
                <span className="font-medium">Government Verified</span>
              </div>
              <div className="flex items-center gap-3">
                <svg className="w-6 h-6 text-green-400" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clipRule="evenodd" />
                </svg>
                <span className="font-medium">Bank-Grade Security</span>
              </div>
              <div className="flex items-center gap-3">
                <svg className="w-6 h-6 text-green-400" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span className="font-medium">24/7 Support</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-20 bg-gray-900 text-white relative">
        <div className="container">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-12">
            <div className="md:col-span-2">
              <div className="flex items-center mb-8">
                <div className="w-16 h-16 rounded-2xl gradient-hero flex items-center justify-center mr-6">
                  <span className="font-bold text-2xl text-white">DSR</span>
                </div>
                <div>
                  <h4 className="text-2xl font-bold">Digital Social Registry</h4>
                  <p className="text-blue-300 text-sm font-medium">Republic of the Philippines</p>
                </div>
              </div>
              <p className="text-gray-300 mb-8 max-w-md text-lg leading-relaxed">
                Empowering Filipino families through innovative digital solutions
                that ensure equitable access to social protection programs nationwide.
              </p>
            </div>

            <div>
              <h5 className="text-xl font-semibold mb-6">Quick Links</h5>
              <ul className="space-y-4">
                <li><Link href="/about" className="text-gray-300 hover:text-white transition-colors text-lg">About DSR</Link></li>
                <li><Link href="/programs" className="text-gray-300 hover:text-white transition-colors text-lg">Programs</Link></li>
                <li><Link href="/help" className="text-gray-300 hover:text-white transition-colors text-lg">Help Center</Link></li>
              </ul>
            </div>

            <div>
              <h5 className="text-xl font-semibold mb-6">Contact Info</h5>
              <div className="space-y-4 text-gray-300">
                <p className="text-lg font-medium">DSWD Central Office</p>
                <p className="text-lg">Batasan Pambansa Complex</p>
                <p className="text-lg">support@dsr.gov.ph</p>
                <p className="text-lg">+63 (2) 8931-8101</p>
              </div>
            </div>
          </div>

          <div className="border-t border-gray-700 mt-16 pt-8 text-center">
            <p className="text-gray-400 text-lg">
              © 2024 Digital Social Registry. All rights reserved. Government of the Philippines.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
}
