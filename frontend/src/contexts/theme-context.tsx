'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import { useAuth } from './auth-context';

type Theme = 'citizen' | 'dswd-staff' | 'lgu-staff';

interface ThemeContextType {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  isLoading: boolean;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

interface ThemeProviderProps {
  children: React.ReactNode;
}

export const ThemeProvider: React.FC<ThemeProviderProps> = ({ children }) => {
  const [theme, setThemeState] = useState<Theme>('citizen');
  const [isLoading, setIsLoading] = useState(true);
  const { user, isAuthenticated } = useAuth();

  // Determine theme based on user role
  useEffect(() => {
    if (isAuthenticated && user?.role) {
      const roleThemeMap: Record<string, Theme> = {
        'CITIZEN': 'citizen',
        'DSWD_STAFF': 'dswd-staff',
        'LGU_STAFF': 'lgu-staff',
        'ADMIN': 'dswd-staff', // Admin uses DSWD staff theme
      };
      
      const newTheme = roleThemeMap[user.role] || 'citizen';
      setThemeState(newTheme);
    } else {
      // Default to citizen theme for unauthenticated users
      setThemeState('citizen');
    }
    
    setIsLoading(false);
  }, [user, isAuthenticated]);

  // Apply theme to document
  useEffect(() => {
    if (typeof window !== 'undefined') {
      document.documentElement.setAttribute('data-theme', theme);
      
      // Also set a CSS class for additional styling
      document.documentElement.className = document.documentElement.className
        .replace(/theme-\w+/g, '')
        .trim();
      document.documentElement.classList.add(`theme-${theme}`);
    }
  }, [theme]);

  const setTheme = (newTheme: Theme) => {
    setThemeState(newTheme);
    
    // Store theme preference in localStorage for persistence
    if (typeof window !== 'undefined') {
      localStorage.setItem('dsr-theme-preference', newTheme);
    }
  };

  // Load theme preference from localStorage on mount
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const savedTheme = localStorage.getItem('dsr-theme-preference') as Theme;
      if (savedTheme && ['citizen', 'dswd-staff', 'lgu-staff'].includes(savedTheme)) {
        setThemeState(savedTheme);
      }
    }
  }, []);

  const contextValue: ThemeContextType = {
    theme,
    setTheme,
    isLoading,
  };

  return (
    <ThemeContext.Provider value={contextValue}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = (): ThemeContextType => {
  const context = useContext(ThemeContext);
  
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  
  return context;
};

// Convenience hooks
export const useCurrentTheme = () => {
  const { theme } = useTheme();
  return theme;
};

export const useThemeClasses = () => {
  const { theme } = useTheme();
  
  const getThemeClasses = (baseClasses: string = '') => {
    const themeClasses = {
      'citizen': 'bg-neutral-50 text-neutral-900',
      'dswd-staff': 'bg-neutral-50 text-neutral-900',
      'lgu-staff': 'bg-neutral-50 text-neutral-900',
    };
    
    return `${baseClasses} ${themeClasses[theme]}`.trim();
  };
  
  return { getThemeClasses };
};

// Theme-aware component wrapper
interface ThemeAwareProps {
  children: React.ReactNode;
  className?: string;
}

export const ThemeAware: React.FC<ThemeAwareProps> = ({ children, className = '' }) => {
  const { theme } = useTheme();
  
  return (
    <div className={`theme-${theme} ${className}`} data-theme={theme}>
      {children}
    </div>
  );
};

export default ThemeProvider;
