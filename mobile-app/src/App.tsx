import React, { useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { Provider as PaperProvider } from 'react-native-paper';
import { QueryClient, QueryClientProvider } from 'react-query';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { StatusBar, Platform, PermissionsAndroid } from 'react-native';

// Screens
import SplashScreen from './screens/SplashScreen';
import LoginScreen from './screens/auth/LoginScreen';
import BiometricLoginScreen from './screens/auth/BiometricLoginScreen';
import DashboardScreen from './screens/dashboard/DashboardScreen';
import RegistrationScreen from './screens/registration/RegistrationScreen';
import QRScannerScreen from './screens/scanner/QRScannerScreen';
import OfflineScreen from './screens/offline/OfflineScreen';

// Services
import { AuthService } from './services/AuthService';
import { OfflineService } from './services/OfflineService';
import { NotificationService } from './services/NotificationService';
import { BiometricService } from './services/BiometricService';

// Store
import { useAuthStore } from './store/authStore';
import { useOfflineStore } from './store/offlineStore';

// Theme
import { theme } from './theme/theme';

// Types
import { RootStackParamList } from './types/navigation';

const Stack = createStackNavigator<RootStackParamList>();
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 3,
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 10 * 60 * 1000, // 10 minutes
    },
  },
});

const App: React.FC = () => {
  const { isAuthenticated, isLoading, initialize } = useAuthStore();
  const { isOffline, initializeOfflineMode } = useOfflineStore();

  useEffect(() => {
    initializeApp();
  }, []);

  const initializeApp = async () => {
    try {
      // Request permissions
      await requestPermissions();
      
      // Initialize services
      await AuthService.initialize();
      await OfflineService.initialize();
      await NotificationService.initialize();
      await BiometricService.initialize();
      
      // Initialize stores
      await initialize();
      await initializeOfflineMode();
      
    } catch (error) {
      console.error('App initialization failed:', error);
    }
  };

  const requestPermissions = async () => {
    if (Platform.OS === 'android') {
      try {
        const permissions = [
          PermissionsAndroid.PERMISSIONS.CAMERA,
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
          PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION,
          PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
          PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
        ];

        const granted = await PermissionsAndroid.requestMultiple(permissions);
        
        Object.keys(granted).forEach(permission => {
          if (granted[permission] !== PermissionsAndroid.RESULTS.GRANTED) {
            console.warn(`Permission ${permission} not granted`);
          }
        });
      } catch (error) {
        console.error('Permission request failed:', error);
      }
    }
  };

  if (isLoading) {
    return <SplashScreen />;
  }

  return (
    <SafeAreaProvider>
      <PaperProvider theme={theme}>
        <QueryClientProvider client={queryClient}>
          <NavigationContainer>
            <StatusBar
              barStyle="dark-content"
              backgroundColor={theme.colors.surface}
              translucent={false}
            />
            <Stack.Navigator
              screenOptions={{
                headerShown: false,
                gestureEnabled: true,
                cardStyleInterpolator: ({ current, layouts }) => ({
                  cardStyle: {
                    transform: [
                      {
                        translateX: current.progress.interpolate({
                          inputRange: [0, 1],
                          outputRange: [layouts.screen.width, 0],
                        }),
                      },
                    ],
                  },
                }),
              }}
            >
              {!isAuthenticated ? (
                // Auth Stack
                <>
                  <Stack.Screen name="Login" component={LoginScreen} />
                  <Stack.Screen name="BiometricLogin" component={BiometricLoginScreen} />
                </>
              ) : (
                // Main App Stack
                <>
                  <Stack.Screen name="Dashboard" component={DashboardScreen} />
                  <Stack.Screen name="Registration" component={RegistrationScreen} />
                  <Stack.Screen name="QRScanner" component={QRScannerScreen} />
                  {isOffline && (
                    <Stack.Screen name="Offline" component={OfflineScreen} />
                  )}
                </>
              )}
            </Stack.Navigator>
          </NavigationContainer>
        </QueryClientProvider>
      </PaperProvider>
    </SafeAreaProvider>
  );
};

export default App;
