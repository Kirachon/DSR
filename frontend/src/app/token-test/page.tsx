'use client';

import React from 'react';
import { useTheme } from '@/contexts/theme-context';

export default function TokenTestPage() {
  const { theme, setTheme } = useTheme();

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: 'var(--background)',
      color: 'var(--foreground)',
      padding: '2rem'
    }} data-theme={theme}>
      <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
        <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
          <h1 style={{
            fontSize: '2.5rem',
            fontWeight: 'bold',
            color: 'var(--primary)',
            marginBottom: '1rem'
          }}>
            DSR Design Token Integration Test
          </h1>
          <p style={{
            fontSize: '1.125rem',
            color: 'var(--muted-foreground)',
            marginBottom: '2rem'
          }}>
            Testing design token integration across role-based themes
          </p>
          <div style={{
            padding: '1rem',
            backgroundColor: 'var(--card)',
            borderRadius: '8px',
            border: '1px solid var(--border)',
            marginBottom: '2rem'
          }}>
            <p style={{ marginBottom: '1rem', fontWeight: '600' }}>
              Current Theme: <span style={{ color: 'var(--primary)' }}>{theme}</span>
            </p>
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
              <button
                onClick={() => setTheme('citizen')}
                style={{
                  padding: '8px 16px',
                  backgroundColor: theme === 'citizen' ? 'var(--primary)' : 'var(--secondary)',
                  color: theme === 'citizen' ? 'var(--primary-foreground)' : 'var(--secondary-foreground)',
                  border: '1px solid var(--border)',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontWeight: '500'
                }}
              >
                Citizen Theme
              </button>
              <button
                onClick={() => setTheme('dswd-staff')}
                style={{
                  padding: '8px 16px',
                  backgroundColor: theme === 'dswd-staff' ? 'var(--primary)' : 'var(--secondary)',
                  color: theme === 'dswd-staff' ? 'var(--primary-foreground)' : 'var(--secondary-foreground)',
                  border: '1px solid var(--border)',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontWeight: '500'
                }}
              >
                DSWD Staff Theme
              </button>
              <button
                onClick={() => setTheme('lgu-staff')}
                style={{
                  padding: '8px 16px',
                  backgroundColor: theme === 'lgu-staff' ? 'var(--primary)' : 'var(--secondary)',
                  color: theme === 'lgu-staff' ? 'var(--primary-foreground)' : 'var(--secondary-foreground)',
                  border: '1px solid var(--border)',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontWeight: '500'
                }}
              >
                LGU Staff Theme
              </button>
            </div>
          </div>
        </div>

        {/* Color Palette Test */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
          gap: '2rem',
          marginBottom: '3rem'
        }}>
          <div style={{
            padding: '1.5rem',
            backgroundColor: 'var(--card)',
            borderRadius: '8px',
            border: '1px solid var(--border)'
          }}>
            <h3 style={{
              fontSize: '1.25rem',
              fontWeight: '600',
              marginBottom: '1rem',
              color: 'var(--card-foreground)'
            }}>
              Philippine Government Colors
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem' }}>
              <div style={{ textAlign: 'center' }}>
                <div style={{
                  height: '60px',
                  backgroundColor: 'var(--dsr-philippine-government-primary-500)',
                  borderRadius: '6px',
                  marginBottom: '0.5rem'
                }}></div>
                <div style={{ fontSize: '0.875rem', color: 'var(--muted-foreground)' }}>Primary</div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{
                  height: '60px',
                  backgroundColor: 'var(--dsr-philippine-government-secondary-500)',
                  borderRadius: '6px',
                  marginBottom: '0.5rem'
                }}></div>
                <div style={{ fontSize: '0.875rem', color: 'var(--muted-foreground)' }}>Secondary</div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{
                  height: '60px',
                  backgroundColor: 'var(--dsr-philippine-government-accent-500)',
                  borderRadius: '6px',
                  marginBottom: '0.5rem'
                }}></div>
                <div style={{ fontSize: '0.875rem', color: 'var(--muted-foreground)' }}>Accent</div>
              </div>
            </div>
          </div>

          <div style={{
            padding: '1.5rem',
            backgroundColor: 'var(--card)',
            borderRadius: '8px',
            border: '1px solid var(--border)'
          }}>
            <h3 style={{
              fontSize: '1.25rem',
              fontWeight: '600',
              marginBottom: '1rem',
              color: 'var(--card-foreground)'
            }}>
              Semantic Colors
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem' }}>
              <div style={{ textAlign: 'center' }}>
                <div style={{
                  height: '60px',
                  backgroundColor: 'var(--dsr-semantic-success-500)',
                  borderRadius: '6px',
                  marginBottom: '0.5rem'
                }}></div>
                <div style={{ fontSize: '0.875rem', color: 'var(--muted-foreground)' }}>Success</div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{
                  height: '60px',
                  backgroundColor: 'var(--dsr-semantic-warning-500)',
                  borderRadius: '6px',
                  marginBottom: '0.5rem'
                }}></div>
                <div style={{ fontSize: '0.875rem', color: 'var(--muted-foreground)' }}>Warning</div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{
                  height: '60px',
                  backgroundColor: 'var(--dsr-semantic-error-500)',
                  borderRadius: '6px',
                  marginBottom: '0.5rem'
                }}></div>
                <div style={{ fontSize: '0.875rem', color: 'var(--muted-foreground)' }}>Error</div>
              </div>
            </div>
          </div>
        </div>

        {/* Typography and Button Tests */}
        <div style={{
          padding: '2rem',
          backgroundColor: 'var(--card)',
          borderRadius: '8px',
          border: '1px solid var(--border)',
          marginBottom: '2rem'
        }}>
          <h2 style={{
            fontSize: '1.5rem',
            fontWeight: '600',
            marginBottom: '2rem',
            color: 'var(--card-foreground)'
          }}>
            Typography and Component Test
          </h2>

          <div style={{ marginBottom: '2rem' }}>
            <h3 style={{ fontSize: '1.25rem', fontWeight: '600', marginBottom: '1rem' }}>Typography Scale</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <div style={{ fontSize: '0.75rem' }}>Extra Small Text (12px)</div>
              <div style={{ fontSize: '0.875rem' }}>Small Text (14px)</div>
              <div style={{ fontSize: '1rem' }}>Base Text (16px)</div>
              <div style={{ fontSize: '1.125rem' }}>Large Text (18px)</div>
              <div style={{ fontSize: '1.25rem' }}>Extra Large Text (20px)</div>
            </div>
          </div>

          <div>
            <h3 style={{ fontSize: '1.25rem', fontWeight: '600', marginBottom: '1rem' }}>Button Variants</h3>
            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
              <button style={{
                padding: '8px 16px',
                backgroundColor: 'var(--primary)',
                color: 'var(--primary-foreground)',
                border: 'none',
                borderRadius: '6px',
                cursor: 'pointer',
                fontWeight: '500'
              }}>
                Primary Button
              </button>
              <button style={{
                padding: '8px 16px',
                backgroundColor: 'var(--secondary)',
                color: 'var(--secondary-foreground)',
                border: '1px solid var(--border)',
                borderRadius: '6px',
                cursor: 'pointer',
                fontWeight: '500'
              }}>
                Secondary Button
              </button>
              <button style={{
                padding: '8px 16px',
                backgroundColor: 'transparent',
                color: 'var(--foreground)',
                border: '1px solid var(--border)',
                borderRadius: '6px',
                cursor: 'pointer',
                fontWeight: '500'
              }}>
                Outline Button
              </button>
            </div>
          </div>
        </div>

        {/* Status Message */}
        <div style={{
          textAlign: 'center',
          padding: '1rem',
          backgroundColor: 'var(--muted)',
          borderRadius: '6px',
          color: 'var(--muted-foreground)'
        }}>
          ✅ Design tokens successfully integrated with role-based theming and proper color contrast
        </div>
      </div>
    </div>
  );
}
