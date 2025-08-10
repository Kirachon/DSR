import PushNotification from 'react-native-push-notification';
import messaging from '@react-native-firebase/messaging';
import { Platform, Alert, Linking } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';

export interface NotificationData {
  id: string;
  title: string;
  body: string;
  data?: any;
  type: 'registration' | 'payment' | 'case' | 'system' | 'reminder';
  priority: 'high' | 'normal' | 'low';
  scheduledTime?: Date;
}

export interface NotificationPreferences {
  enabled: boolean;
  registrationUpdates: boolean;
  paymentNotifications: boolean;
  caseUpdates: boolean;
  systemAlerts: boolean;
  reminders: boolean;
  quietHours: {
    enabled: boolean;
    startTime: string; // HH:mm format
    endTime: string;   // HH:mm format
  };
}

class NotificationServiceClass {
  private isInitialized: boolean = false;
  private fcmToken: string | null = null;

  async initialize(): Promise<void> {
    try {
      await this.requestPermissions();
      await this.configurePushNotifications();
      await this.setupFirebaseMessaging();
      await this.loadPreferences();
      
      this.isInitialized = true;
      console.log('Notification service initialized successfully');
    } catch (error) {
      console.error('Notification service initialization failed:', error);
    }
  }

  private async requestPermissions(): Promise<boolean> {
    try {
      if (Platform.OS === 'ios') {
        const authStatus = await messaging().requestPermission();
        const enabled =
          authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
          authStatus === messaging.AuthorizationStatus.PROVISIONAL;

        if (!enabled) {
          Alert.alert(
            'Notifications Disabled',
            'Please enable notifications in Settings to receive important updates.',
            [
              { text: 'Cancel', style: 'cancel' },
              { text: 'Settings', onPress: () => Linking.openSettings() }
            ]
          );
        }

        return enabled;
      } else {
        // Android permissions are handled automatically
        return true;
      }
    } catch (error) {
      console.error('Failed to request notification permissions:', error);
      return false;
    }
  }

  private async configurePushNotifications(): Promise<void> {
    PushNotification.configure({
      onRegister: (token) => {
        console.log('Push notification token:', token);
      },

      onNotification: (notification) => {
        console.log('Notification received:', notification);
        this.handleNotificationReceived(notification);
      },

      onAction: (notification) => {
        console.log('Notification action:', notification);
        this.handleNotificationAction(notification);
      },

      onRegistrationError: (error) => {
        console.error('Push notification registration error:', error);
      },

      permissions: {
        alert: true,
        badge: true,
        sound: true,
      },

      popInitialNotification: true,
      requestPermissions: Platform.OS === 'ios',
    });

    // Create notification channels for Android
    if (Platform.OS === 'android') {
      this.createNotificationChannels();
    }
  }

  private createNotificationChannels(): void {
    const channels = [
      {
        channelId: 'dsr-registration',
        channelName: 'Registration Updates',
        channelDescription: 'Notifications about registration status and updates',
        importance: 4,
        vibrate: true,
      },
      {
        channelId: 'dsr-payment',
        channelName: 'Payment Notifications',
        channelDescription: 'Notifications about payment status and disbursements',
        importance: 4,
        vibrate: true,
      },
      {
        channelId: 'dsr-case',
        channelName: 'Case Updates',
        channelDescription: 'Notifications about grievance case status',
        importance: 3,
        vibrate: true,
      },
      {
        channelId: 'dsr-system',
        channelName: 'System Alerts',
        channelDescription: 'Important system notifications and maintenance alerts',
        importance: 4,
        vibrate: true,
      },
      {
        channelId: 'dsr-reminder',
        channelName: 'Reminders',
        channelDescription: 'Reminder notifications for pending actions',
        importance: 2,
        vibrate: false,
      },
    ];

    channels.forEach(channel => {
      PushNotification.createChannel(channel, (created) => {
        console.log(`Channel ${channel.channelId} created: ${created}`);
      });
    });
  }

  private async setupFirebaseMessaging(): Promise<void> {
    try {
      // Get FCM token
      this.fcmToken = await messaging().getToken();
      console.log('FCM Token:', this.fcmToken);

      // Save token to storage
      await AsyncStorage.setItem('fcm_token', this.fcmToken);

      // Listen for token refresh
      messaging().onTokenRefresh(async (token) => {
        console.log('FCM Token refreshed:', token);
        this.fcmToken = token;
        await AsyncStorage.setItem('fcm_token', token);
        // Send updated token to your backend
        await this.updateTokenOnServer(token);
      });

      // Handle background messages
      messaging().setBackgroundMessageHandler(async (remoteMessage) => {
        console.log('Background message received:', remoteMessage);
        await this.handleBackgroundMessage(remoteMessage);
      });

      // Handle foreground messages
      messaging().onMessage(async (remoteMessage) => {
        console.log('Foreground message received:', remoteMessage);
        await this.handleForegroundMessage(remoteMessage);
      });

      // Handle notification opened app
      messaging().onNotificationOpenedApp((remoteMessage) => {
        console.log('Notification opened app:', remoteMessage);
        this.handleNotificationOpened(remoteMessage);
      });

      // Check if app was opened from a notification
      const initialNotification = await messaging().getInitialNotification();
      if (initialNotification) {
        console.log('App opened from notification:', initialNotification);
        this.handleNotificationOpened(initialNotification);
      }

    } catch (error) {
      console.error('Firebase messaging setup failed:', error);
    }
  }

  async sendLocalNotification(notification: NotificationData): Promise<void> {
    try {
      const preferences = await this.getPreferences();
      
      if (!preferences.enabled || !this.shouldSendNotification(notification, preferences)) {
        console.log('Notification blocked by preferences:', notification.type);
        return;
      }

      const channelId = `dsr-${notification.type}`;
      
      PushNotification.localNotification({
        id: notification.id,
        title: notification.title,
        message: notification.body,
        channelId,
        priority: notification.priority === 'high' ? 'high' : 'default',
        importance: notification.priority === 'high' ? 'high' : 'default',
        vibrate: notification.priority === 'high',
        playSound: true,
        soundName: 'default',
        userInfo: notification.data || {},
        date: notification.scheduledTime,
      });

      console.log('Local notification sent:', notification.id);
    } catch (error) {
      console.error('Failed to send local notification:', error);
    }
  }

  async scheduleNotification(notification: NotificationData, scheduledTime: Date): Promise<void> {
    const scheduledNotification = {
      ...notification,
      scheduledTime,
    };

    await this.sendLocalNotification(scheduledNotification);
  }

  async cancelNotification(notificationId: string): Promise<void> {
    PushNotification.cancelLocalNotifications({ id: notificationId });
    console.log('Notification cancelled:', notificationId);
  }

  async cancelAllNotifications(): Promise<void> {
    PushNotification.cancelAllLocalNotifications();
    console.log('All notifications cancelled');
  }

  private shouldSendNotification(
    notification: NotificationData,
    preferences: NotificationPreferences
  ): boolean {
    // Check type-specific preferences
    switch (notification.type) {
      case 'registration':
        return preferences.registrationUpdates;
      case 'payment':
        return preferences.paymentNotifications;
      case 'case':
        return preferences.caseUpdates;
      case 'system':
        return preferences.systemAlerts;
      case 'reminder':
        return preferences.reminders;
      default:
        return true;
    }
  }

  private async handleNotificationReceived(notification: any): Promise<void> {
    console.log('Handling received notification:', notification);
    // Add custom logic for handling received notifications
  }

  private async handleNotificationAction(notification: any): Promise<void> {
    console.log('Handling notification action:', notification);
    // Add custom logic for handling notification actions
  }

  private async handleBackgroundMessage(remoteMessage: any): Promise<void> {
    console.log('Handling background message:', remoteMessage);
    
    // Create local notification for background messages
    if (remoteMessage.notification) {
      await this.sendLocalNotification({
        id: remoteMessage.messageId || Date.now().toString(),
        title: remoteMessage.notification.title || 'DSR Notification',
        body: remoteMessage.notification.body || '',
        type: remoteMessage.data?.type || 'system',
        priority: remoteMessage.data?.priority || 'normal',
        data: remoteMessage.data,
      });
    }
  }

  private async handleForegroundMessage(remoteMessage: any): Promise<void> {
    console.log('Handling foreground message:', remoteMessage);
    
    // Show in-app notification or update UI
    if (remoteMessage.notification) {
      await this.sendLocalNotification({
        id: remoteMessage.messageId || Date.now().toString(),
        title: remoteMessage.notification.title || 'DSR Notification',
        body: remoteMessage.notification.body || '',
        type: remoteMessage.data?.type || 'system',
        priority: remoteMessage.data?.priority || 'normal',
        data: remoteMessage.data,
      });
    }
  }

  private handleNotificationOpened(remoteMessage: any): void {
    console.log('Handling notification opened:', remoteMessage);
    
    // Navigate to appropriate screen based on notification data
    const notificationType = remoteMessage.data?.type;
    const targetScreen = remoteMessage.data?.screen;
    
    if (targetScreen) {
      // Use navigation service to navigate to the target screen
      console.log(`Navigate to screen: ${targetScreen}`);
    }
  }

  async getPreferences(): Promise<NotificationPreferences> {
    try {
      const preferencesJson = await AsyncStorage.getItem('notification_preferences');
      if (preferencesJson) {
        return JSON.parse(preferencesJson);
      }
    } catch (error) {
      console.error('Failed to get notification preferences:', error);
    }

    // Return default preferences
    return this.getDefaultPreferences();
  }

  async updatePreferences(preferences: Partial<NotificationPreferences>): Promise<void> {
    try {
      const currentPreferences = await this.getPreferences();
      const updatedPreferences = { ...currentPreferences, ...preferences };
      
      await AsyncStorage.setItem(
        'notification_preferences',
        JSON.stringify(updatedPreferences)
      );
      
      console.log('Notification preferences updated');
    } catch (error) {
      console.error('Failed to update notification preferences:', error);
      throw error;
    }
  }

  private getDefaultPreferences(): NotificationPreferences {
    return {
      enabled: true,
      registrationUpdates: true,
      paymentNotifications: true,
      caseUpdates: true,
      systemAlerts: true,
      reminders: true,
      quietHours: {
        enabled: false,
        startTime: '22:00',
        endTime: '07:00',
      },
    };
  }

  private async loadPreferences(): Promise<void> {
    const preferences = await this.getPreferences();
    console.log('Loaded notification preferences:', preferences);
  }

  private async updateTokenOnServer(token: string): Promise<void> {
    try {
      // Send token to your backend API
      // await AuthService.getApiClient().post('/notifications/token', { token });
      console.log('Token updated on server:', token);
    } catch (error) {
      console.error('Failed to update token on server:', error);
    }
  }

  getFCMToken(): string | null {
    return this.fcmToken;
  }

  isInitialized(): boolean {
    return this.isInitialized;
  }
}

export const NotificationService = new NotificationServiceClass();
