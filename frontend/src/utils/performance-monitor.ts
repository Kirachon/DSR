/**
 * DSR Performance Monitoring Utilities
 * Tools for monitoring and optimizing component performance
 */

// Performance metrics interface
export interface PerformanceMetrics {
  componentName: string;
  renderTime: number;
  memoryUsage?: number;
  timestamp: number;
  props?: Record<string, any>;
  children?: number;
}

// Performance thresholds
export const PERFORMANCE_THRESHOLDS = {
  RENDER_TIME: {
    GOOD: 16, // 60fps
    ACCEPTABLE: 33, // 30fps
    POOR: 100, // Noticeable lag
  },
  MEMORY_USAGE: {
    GOOD: 1024 * 1024, // 1MB
    ACCEPTABLE: 5 * 1024 * 1024, // 5MB
    POOR: 10 * 1024 * 1024, // 10MB
  },
} as const;

// Performance monitoring class
class PerformanceMonitor {
  private metrics: PerformanceMetrics[] = [];
  private observers: Map<string, PerformanceObserver> = new Map();
  private enabled: boolean = process.env.NODE_ENV === 'development';

  constructor() {
    if (this.enabled && typeof window !== 'undefined') {
      this.initializeObservers();
    }
  }

  /**
   * Initialize performance observers
   */
  private initializeObservers(): void {
    // Measure component render times
    if ('PerformanceObserver' in window) {
      const renderObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        entries.forEach((entry) => {
          if (entry.name.startsWith('dsr-component-')) {
            this.recordMetric({
              componentName: entry.name.replace('dsr-component-', ''),
              renderTime: entry.duration,
              timestamp: entry.startTime,
            });
          }
        });
      });

      renderObserver.observe({ entryTypes: ['measure'] });
      this.observers.set('render', renderObserver);
    }

    // Monitor memory usage
    if ('memory' in performance) {
      setInterval(() => {
        const memoryInfo = (performance as any).memory;
        if (memoryInfo) {
          this.recordMemoryUsage(memoryInfo.usedJSHeapSize);
        }
      }, 5000); // Check every 5 seconds
    }
  }

  /**
   * Start measuring component performance
   */
  startMeasure(componentName: string, props?: Record<string, any>): void {
    if (!this.enabled) return;

    const measureName = `dsr-component-${componentName}`;
    const startMark = `${measureName}-start`;
    
    performance.mark(startMark);
    
    // Store props for later analysis
    if (props) {
      (window as any).__dsrPerfProps = {
        ...((window as any).__dsrPerfProps || {}),
        [componentName]: props,
      };
    }
  }

  /**
   * End measuring component performance
   */
  endMeasure(componentName: string): void {
    if (!this.enabled) return;

    const measureName = `dsr-component-${componentName}`;
    const startMark = `${measureName}-start`;
    const endMark = `${measureName}-end`;
    
    performance.mark(endMark);
    performance.measure(measureName, startMark, endMark);
    
    // Clean up marks
    performance.clearMarks(startMark);
    performance.clearMarks(endMark);
  }

  /**
   * Record performance metric
   */
  private recordMetric(metric: PerformanceMetrics): void {
    this.metrics.push(metric);
    
    // Keep only last 100 metrics to prevent memory leaks
    if (this.metrics.length > 100) {
      this.metrics = this.metrics.slice(-100);
    }

    // Log performance warnings
    this.checkPerformanceThresholds(metric);
  }

  /**
   * Record memory usage
   */
  private recordMemoryUsage(memoryUsage: number): void {
    const metric: PerformanceMetrics = {
      componentName: 'global',
      renderTime: 0,
      memoryUsage,
      timestamp: performance.now(),
    };
    
    this.recordMetric(metric);
  }

  /**
   * Check performance thresholds and log warnings
   */
  private checkPerformanceThresholds(metric: PerformanceMetrics): void {
    // Check render time
    if (metric.renderTime > PERFORMANCE_THRESHOLDS.RENDER_TIME.POOR) {
      console.warn(
        `🐌 Poor render performance detected for ${metric.componentName}: ${metric.renderTime.toFixed(2)}ms`
      );
    } else if (metric.renderTime > PERFORMANCE_THRESHOLDS.RENDER_TIME.ACCEPTABLE) {
      console.warn(
        `⚠️ Slow render performance for ${metric.componentName}: ${metric.renderTime.toFixed(2)}ms`
      );
    }

    // Check memory usage
    if (metric.memoryUsage && metric.memoryUsage > PERFORMANCE_THRESHOLDS.MEMORY_USAGE.POOR) {
      console.warn(
        `🧠 High memory usage detected: ${(metric.memoryUsage / 1024 / 1024).toFixed(2)}MB`
      );
    }
  }

  /**
   * Get performance metrics for a component
   */
  getMetrics(componentName?: string): PerformanceMetrics[] {
    if (componentName) {
      return this.metrics.filter(m => m.componentName === componentName);
    }
    return [...this.metrics];
  }

  /**
   * Get performance summary
   */
  getSummary(): {
    totalComponents: number;
    averageRenderTime: number;
    slowestComponent: string;
    fastestComponent: string;
    memoryUsage: number;
  } {
    const renderMetrics = this.metrics.filter(m => m.renderTime > 0);
    
    if (renderMetrics.length === 0) {
      return {
        totalComponents: 0,
        averageRenderTime: 0,
        slowestComponent: 'N/A',
        fastestComponent: 'N/A',
        memoryUsage: 0,
      };
    }

    const totalRenderTime = renderMetrics.reduce((sum, m) => sum + m.renderTime, 0);
    const averageRenderTime = totalRenderTime / renderMetrics.length;
    
    const sortedByRenderTime = [...renderMetrics].sort((a, b) => b.renderTime - a.renderTime);
    const slowestComponent = sortedByRenderTime[0]?.componentName || 'N/A';
    const fastestComponent = sortedByRenderTime[sortedByRenderTime.length - 1]?.componentName || 'N/A';
    
    const latestMemoryMetric = this.metrics
      .filter(m => m.memoryUsage)
      .sort((a, b) => b.timestamp - a.timestamp)[0];
    
    return {
      totalComponents: new Set(renderMetrics.map(m => m.componentName)).size,
      averageRenderTime,
      slowestComponent,
      fastestComponent,
      memoryUsage: latestMemoryMetric?.memoryUsage || 0,
    };
  }

  /**
   * Clear all metrics
   */
  clearMetrics(): void {
    this.metrics = [];
  }

  /**
   * Disable performance monitoring
   */
  disable(): void {
    this.enabled = false;
    this.observers.forEach(observer => observer.disconnect());
    this.observers.clear();
  }

  /**
   * Enable performance monitoring
   */
  enable(): void {
    this.enabled = true;
    if (typeof window !== 'undefined') {
      this.initializeObservers();
    }
  }
}

// Global performance monitor instance
export const performanceMonitor = new PerformanceMonitor();

/**
 * React hook for component performance monitoring
 */
export function usePerformanceMonitor(componentName: string, props?: Record<string, any>) {
  React.useEffect(() => {
    performanceMonitor.startMeasure(componentName, props);
    
    return () => {
      performanceMonitor.endMeasure(componentName);
    };
  }, [componentName, props]);
}

/**
 * Higher-order component for performance monitoring
 */
export function withPerformanceMonitor<P extends object>(
  WrappedComponent: React.ComponentType<P>,
  componentName?: string
) {
  const displayName = componentName || WrappedComponent.displayName || WrappedComponent.name || 'Component';
  
  const MonitoredComponent = React.forwardRef<any, P>((props, ref) => {
    usePerformanceMonitor(displayName, props);
    
    return <WrappedComponent {...props} ref={ref} />;
  });
  
  MonitoredComponent.displayName = `withPerformanceMonitor(${displayName})`;
  
  return MonitoredComponent;
}

/**
 * Performance monitoring decorator for class components
 */
export function performanceMonitorDecorator(componentName?: string) {
  return function <T extends React.ComponentType<any>>(target: T): T {
    return withPerformanceMonitor(target, componentName) as T;
  };
}

/**
 * Utility to measure function execution time
 */
export function measureExecutionTime<T extends (...args: any[]) => any>(
  fn: T,
  name?: string
): T {
  return ((...args: Parameters<T>) => {
    const startTime = performance.now();
    const result = fn(...args);
    const endTime = performance.now();
    
    console.log(`⏱️ ${name || fn.name} executed in ${(endTime - startTime).toFixed(2)}ms`);
    
    return result;
  }) as T;
}

/**
 * Utility to measure async function execution time
 */
export function measureAsyncExecutionTime<T extends (...args: any[]) => Promise<any>>(
  fn: T,
  name?: string
): T {
  return (async (...args: Parameters<T>) => {
    const startTime = performance.now();
    const result = await fn(...args);
    const endTime = performance.now();
    
    console.log(`⏱️ ${name || fn.name} executed in ${(endTime - startTime).toFixed(2)}ms`);
    
    return result;
  }) as T;
}

/**
 * Bundle size analyzer utility
 */
export function analyzeBundleSize(): void {
  if (typeof window === 'undefined') return;
  
  // Analyze loaded scripts
  const scripts = Array.from(document.querySelectorAll('script[src]'));
  let totalSize = 0;
  
  scripts.forEach(async (script) => {
    const src = (script as HTMLScriptElement).src;
    if (src) {
      try {
        const response = await fetch(src, { method: 'HEAD' });
        const size = parseInt(response.headers.get('content-length') || '0', 10);
        totalSize += size;
        
        console.log(`📦 Script: ${src.split('/').pop()} - ${(size / 1024).toFixed(2)}KB`);
      } catch (error) {
        console.warn(`Failed to analyze script size: ${src}`);
      }
    }
  });
  
  setTimeout(() => {
    console.log(`📦 Total bundle size: ${(totalSize / 1024 / 1024).toFixed(2)}MB`);
  }, 1000);
}

// Export React import for hooks
import React from 'react';
