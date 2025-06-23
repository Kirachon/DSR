'use client';

// Footer Component
// Application footer with links, copyright, and responsive design

import React from 'react';
import Link from 'next/link';

import { cn } from '@/utils';
import { config } from '@/lib/config';

// Footer props interface
export interface FooterProps {
  className?: string;
  variant?: 'default' | 'minimal' | 'detailed';
  showSocialLinks?: boolean;
  showLegalLinks?: boolean;
}

// Footer link interface
interface FooterLink {
  label: string;
  href: string;
  external?: boolean;
}

// Footer section interface
interface FooterSection {
  title: string;
  links: FooterLink[];
}

// Footer sections configuration
const footerSections: FooterSection[] = [
  {
    title: 'Services',
    links: [
      { label: 'Citizen Registration', href: '/services/registration' },
      { label: 'Social Programs', href: '/services/programs' },
      { label: 'Benefits Verification', href: '/services/verification' },
      { label: 'Support Center', href: '/support' },
    ],
  },
  {
    title: 'Resources',
    links: [
      { label: 'Documentation', href: '/docs' },
      { label: 'API Reference', href: '/api-docs' },
      { label: 'Help Center', href: '/help' },
      { label: 'Contact Us', href: '/contact' },
    ],
  },
  {
    title: 'Legal',
    links: [
      { label: 'Privacy Policy', href: '/privacy' },
      { label: 'Terms of Service', href: '/terms' },
      { label: 'Data Protection', href: '/data-protection' },
      { label: 'Accessibility', href: '/accessibility' },
    ],
  },
];

// Social media links
const socialLinks = [
  {
    label: 'Facebook',
    href: 'https://facebook.com/dsr-philippines',
    icon: (
      <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24">
        <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" />
      </svg>
    ),
  },
  {
    label: 'Twitter',
    href: 'https://twitter.com/dsr_philippines',
    icon: (
      <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24">
        <path d="M23.953 4.57a10 10 0 01-2.825.775 4.958 4.958 0 002.163-2.723c-.951.555-2.005.959-3.127 1.184a4.92 4.92 0 00-8.384 4.482C7.69 8.095 4.067 6.13 1.64 3.162a4.822 4.822 0 00-.666 2.475c0 1.71.87 3.213 2.188 4.096a4.904 4.904 0 01-2.228-.616v.06a4.923 4.923 0 003.946 4.827 4.996 4.996 0 01-2.212.085 4.936 4.936 0 004.604 3.417 9.867 9.867 0 01-6.102 2.105c-.39 0-.779-.023-1.17-.067a13.995 13.995 0 007.557 2.209c9.053 0 13.998-7.496 13.998-13.985 0-.21 0-.42-.015-.63A9.935 9.935 0 0024 4.59z" />
      </svg>
    ),
  },
  {
    label: 'LinkedIn',
    href: 'https://linkedin.com/company/dsr-philippines',
    icon: (
      <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24">
        <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z" />
      </svg>
    ),
  },
];

// Legal links
const legalLinks: FooterLink[] = [
  { label: 'Privacy Policy', href: '/privacy' },
  { label: 'Terms of Service', href: '/terms' },
  { label: 'Cookie Policy', href: '/cookies' },
];

// Footer component
export const Footer: React.FC<FooterProps> = ({
  className,
  variant = 'default',
  showSocialLinks = true,
  showLegalLinks = true,
}) => {
  const currentYear = new Date().getFullYear();

  if (variant === 'minimal') {
    return (
      <footer className={cn('bg-gray-50 border-t border-gray-200', className)}>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex flex-col sm:flex-row justify-between items-center space-y-2 sm:space-y-0">
            <p className="text-sm text-gray-500">
              © {currentYear} {config.appName}. All rights reserved.
            </p>
            {showLegalLinks && (
              <div className="flex space-x-4">
                {legalLinks.map((link) => (
                  <Link
                    key={link.href}
                    href={link.href}
                    className="text-sm text-gray-500 hover:text-gray-900 transition-colors"
                  >
                    {link.label}
                  </Link>
                ))}
              </div>
            )}
          </div>
        </div>
      </footer>
    );
  }

  return (
    <footer className={cn('bg-white border-t border-gray-200', className)}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {variant === 'detailed' && (
          <div className="py-12">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
              {/* Brand section */}
              <div className="col-span-1 lg:col-span-1">
                <div className="flex items-center space-x-2 mb-4">
                  <div className="h-8 w-8 bg-primary-600 rounded-lg flex items-center justify-center">
                    <span className="text-white font-bold text-sm">DSR</span>
                  </div>
                  <span className="text-lg font-semibold text-gray-900">
                    {config.appName}
                  </span>
                </div>
                <p className="text-sm text-gray-600 mb-4">
                  Empowering communities through digital social registry solutions. 
                  Connecting citizens with essential services and benefits.
                </p>
                {showSocialLinks && (
                  <div className="flex space-x-4">
                    {socialLinks.map((social) => (
                      <a
                        key={social.label}
                        href={social.href}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                        aria-label={social.label}
                      >
                        {social.icon}
                      </a>
                    ))}
                  </div>
                )}
              </div>

              {/* Footer sections */}
              {footerSections.map((section) => (
                <div key={section.title} className="col-span-1">
                  <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-4">
                    {section.title}
                  </h3>
                  <ul className="space-y-3">
                    {section.links.map((link) => (
                      <li key={link.href}>
                        {link.external ? (
                          <a
                            href={link.href}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
                          >
                            {link.label}
                          </a>
                        ) : (
                          <Link
                            href={link.href}
                            className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
                          >
                            {link.label}
                          </Link>
                        )}
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Bottom section */}
        <div className="border-t border-gray-200 py-6">
          <div className="flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0">
            <div className="flex flex-col sm:flex-row items-center space-y-2 sm:space-y-0 sm:space-x-4">
              <p className="text-sm text-gray-500">
                © {currentYear} {config.appName}. All rights reserved.
              </p>
              <p className="text-sm text-gray-500">
                Version {config.appVersion}
              </p>
            </div>

            {showLegalLinks && (
              <div className="flex flex-wrap justify-center space-x-4">
                {legalLinks.map((link) => (
                  <Link
                    key={link.href}
                    href={link.href}
                    className="text-sm text-gray-500 hover:text-gray-900 transition-colors"
                  >
                    {link.label}
                  </Link>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
