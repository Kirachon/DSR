import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';
import './globals.css';

import { AuthProvider, ThemeProvider } from '@/contexts';

const geistSans = Geist({
  variable: '--font-geist-sans',
  subsets: ['latin'],
});

const geistMono = Geist_Mono({
  variable: '--font-geist-mono',
  subsets: ['latin'],
});

export const metadata: Metadata = {
  title: {
    default: 'Digital Social Registry - DSR Portal',
    template: '%s | DSR Portal',
  },
  description: 'Digital Social Registry (DSR) - A comprehensive platform for managing social protection programs, citizen registration, eligibility assessment, and benefit distribution across the Philippines.',
  keywords: [
    'Digital Social Registry',
    'DSR',
    'Social Protection',
    'Philippines',
    'DSWD',
    'Citizen Registration',
    'Benefits',
    'Government Services',
  ],
  authors: [
    {
      name: 'Department of Social Welfare and Development',
      url: 'https://www.dswd.gov.ph',
    },
  ],
  creator: 'Department of Social Welfare and Development',
  publisher: 'Government of the Philippines',
  formatDetection: {
    email: false,
    address: false,
    telephone: false,
  },
  metadataBase: new URL('https://dsr.gov.ph'),
  alternates: {
    canonical: '/',
  },
  openGraph: {
    type: 'website',
    locale: 'en_PH',
    url: 'https://dsr.gov.ph',
    title: 'Digital Social Registry - DSR Portal',
    description: 'A comprehensive platform for managing social protection programs, citizen registration, eligibility assessment, and benefit distribution across the Philippines.',
    siteName: 'Digital Social Registry',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Digital Social Registry - DSR Portal',
    description: 'A comprehensive platform for managing social protection programs, citizen registration, eligibility assessment, and benefit distribution across the Philippines.',
  },
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      'max-video-preview': -1,
      'max-image-preview': 'large',
      'max-snippet': -1,
    },
  },
  verification: {
    google: 'google-site-verification-code',
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang='en'>
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <AuthProvider>
          <ThemeProvider>
            {children}
          </ThemeProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
