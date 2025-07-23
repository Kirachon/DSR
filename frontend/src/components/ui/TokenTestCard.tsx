'use client';

import React from 'react';
import { Button } from './button';
import { Card } from './card';
import { useTheme } from '@/contexts/theme-context';

interface TokenTestCardProps {
  title: string;
  description: string;
}

export const TokenTestCard: React.FC<TokenTestCardProps> = ({ title, description }) => {
  const { theme, setTheme } = useTheme();

  return (
    <Card className="p-6 space-y-4">
      <div className="space-y-2">
        <h3 className="text-lg font-semibold text-philippine-government-primary-700">
          {title}
        </h3>
        <p className="text-neutral-600">{description}</p>
      </div>

      <div className="space-y-3">
        <div className="text-sm font-medium text-neutral-700">Current Theme: {theme}</div>
        
        <div className="flex flex-wrap gap-2">
          <Button
            variant="primary"
            size="sm"
            onClick={() => setTheme('citizen')}
            className={theme === 'citizen' ? 'ring-2 ring-offset-2 ring-philippine-government-primary-500' : ''}
          >
            Citizen Theme
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => setTheme('dswd-staff')}
            className={theme === 'dswd-staff' ? 'ring-2 ring-offset-2 ring-philippine-government-primary-500' : ''}
          >
            DSWD Staff Theme
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => setTheme('lgu-staff')}
            className={theme === 'lgu-staff' ? 'ring-2 ring-offset-2 ring-philippine-government-secondary-500' : ''}
          >
            LGU Staff Theme
          </Button>
        </div>

        <div className="space-y-2">
          <div className="text-sm font-medium text-neutral-700">Button Variants:</div>
          <div className="flex flex-wrap gap-2">
            <Button variant="primary" size="sm">Primary</Button>
            <Button variant="secondary" size="sm">Secondary</Button>
            <Button variant="outline" size="sm">Outline</Button>
            <Button variant="ghost" size="sm">Ghost</Button>
          </div>
        </div>

        <div className="space-y-2">
          <div className="text-sm font-medium text-neutral-700">Color Tokens:</div>
          <div className="grid grid-cols-3 gap-2 text-xs">
            <div className="space-y-1">
              <div className="h-8 bg-philippine-government-primary-500 rounded border"></div>
              <div className="text-center">Primary 500</div>
            </div>
            <div className="space-y-1">
              <div className="h-8 bg-philippine-government-secondary-500 rounded border"></div>
              <div className="text-center">Secondary 500</div>
            </div>
            <div className="space-y-1">
              <div className="h-8 bg-philippine-government-accent-500 rounded border"></div>
              <div className="text-center">Accent 500</div>
            </div>
            <div className="space-y-1">
              <div className="h-8 bg-semantic-success-500 rounded border"></div>
              <div className="text-center">Success 500</div>
            </div>
            <div className="space-y-1">
              <div className="h-8 bg-semantic-warning-500 rounded border"></div>
              <div className="text-center">Warning 500</div>
            </div>
            <div className="space-y-1">
              <div className="h-8 bg-semantic-error-500 rounded border"></div>
              <div className="text-center">Error 500</div>
            </div>
          </div>
        </div>

        <div className="space-y-2">
          <div className="text-sm font-medium text-neutral-700">Spacing Tokens:</div>
          <div className="space-y-1">
            <div className="h-4 bg-neutral-200 rounded" style={{ width: 'var(--dsr-spacing-4)' }}></div>
            <div className="text-xs">Spacing 4 (1rem)</div>
            <div className="h-4 bg-neutral-200 rounded" style={{ width: 'var(--dsr-spacing-8)' }}></div>
            <div className="text-xs">Spacing 8 (2rem)</div>
            <div className="h-4 bg-neutral-200 rounded" style={{ width: 'var(--dsr-spacing-16)' }}></div>
            <div className="text-xs">Spacing 16 (4rem)</div>
          </div>
        </div>

        <div className="space-y-2">
          <div className="text-sm font-medium text-neutral-700">Typography Tokens:</div>
          <div className="space-y-1">
            <div className="text-xs">Font Size XS</div>
            <div className="text-sm">Font Size SM</div>
            <div className="text-base">Font Size Base</div>
            <div className="text-lg">Font Size LG</div>
            <div className="text-xl">Font Size XL</div>
          </div>
        </div>
      </div>
    </Card>
  );
};
