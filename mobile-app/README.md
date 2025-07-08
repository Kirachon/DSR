# DSR Mobile Application

A React Native mobile application for the Dynamic Social Registry (DSR) system, providing citizens and government workers with secure, offline-capable access to social protection services in the Philippines.

## Features

### 🔐 Authentication & Security
- **JWT-based Authentication** with automatic token refresh
- **Biometric Login** (TouchID, FaceID, Fingerprint)
- **Secure Token Storage** using React Native Keychain
- **Multi-factor Authentication** support

### 📱 Offline Capabilities
- **Offline-first Architecture** with SQLite local storage
- **Background Synchronization** when network is available
- **Conflict Resolution** for offline data
- **Automatic Retry** mechanisms for failed sync operations

### 📷 QR Code Integration
- **PhilSys QR Scanning** for identity verification
- **Household QR Codes** for quick household lookup
- **Case QR Codes** for grievance case tracking
- **Document Verification** with digital signatures

### 🔔 Push Notifications
- **Firebase Cloud Messaging** integration
- **Multi-channel Notifications** (SMS, Email, Push, Voice)
- **Notification Preferences** management
- **Quiet Hours** configuration

### 🌍 Accessibility & Localization
- **Multi-language Support** (English, Filipino, Cebuano, Ilocano)
- **Responsive Design** for various screen sizes
- **Accessibility Features** for users with disabilities
- **Dark/Light Theme** support

## Technical Stack

- **Framework**: React Native 0.72+
- **Language**: TypeScript
- **Navigation**: React Navigation 6
- **State Management**: Zustand with persistence
- **UI Components**: React Native Paper
- **Forms**: React Hook Form with Yup validation
- **HTTP Client**: Axios with interceptors
- **Database**: SQLite for offline storage
- **Security**: React Native Keychain, Biometrics
- **Testing**: Jest, React Native Testing Library, Detox

## Prerequisites

- Node.js 16+
- React Native CLI
- Android Studio (for Android development)
- Xcode (for iOS development)
- Java 11+ (for Android)

## Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/dsr-mobile-app.git
   cd dsr-mobile-app
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **iOS Setup** (macOS only)
   ```bash
   cd ios && pod install && cd ..
   ```

4. **Android Setup**
   - Ensure Android SDK is installed
   - Create local.properties file in android/ directory
   - Add SDK path: `sdk.dir=/path/to/Android/Sdk`

## Configuration

1. **Environment Variables**
   Create `.env` file in the root directory:
   ```env
   API_BASE_URL=https://api.dsr.gov.ph/v1
   FIREBASE_PROJECT_ID=dsr-mobile-app
   SENTRY_DSN=your-sentry-dsn
   ```

2. **Firebase Configuration**
   - Add `google-services.json` to `android/app/`
   - Add `GoogleService-Info.plist` to `ios/DSRMobileApp/`

3. **Code Signing** (iOS)
   - Configure signing certificates in Xcode
   - Update bundle identifier and team ID

## Development

### Running the App

**Start Metro bundler:**
```bash
npm start
```

**Run on Android:**
```bash
npm run android
```

**Run on iOS:**
```bash
npm run ios
```

### Testing

**Unit Tests:**
```bash
npm test
```

**Test Coverage:**
```bash
npm run test:coverage
```

**E2E Tests:**
```bash
npm run test:e2e
```

### Code Quality

**Linting:**
```bash
npm run lint
```

**Type Checking:**
```bash
npx tsc --noEmit
```

## Building for Production

### Android

1. **Generate Release APK:**
   ```bash
   npm run build:android
   ```

2. **Generate AAB for Play Store:**
   ```bash
   cd android
   ./gradlew bundleRelease
   ```

### iOS

1. **Archive for App Store:**
   ```bash
   npm run build:ios
   ```

2. **Upload to App Store Connect:**
   - Open Xcode
   - Product → Archive
   - Upload to App Store Connect

## Project Structure

```
src/
├── components/          # Reusable UI components
├── screens/            # Screen components
├── services/           # API and business logic services
├── store/              # State management (Zustand)
├── navigation/         # Navigation configuration
├── hooks/              # Custom React hooks
├── utils/              # Utility functions
├── types/              # TypeScript type definitions
├── config/             # App configuration
├── theme/              # UI theme and styling
└── __tests__/          # Test files
```

## Key Services

### AuthService
Handles authentication, token management, and secure storage.

### OfflineService
Manages offline data storage, synchronization, and conflict resolution.

### BiometricService
Provides biometric authentication capabilities.

### NotificationService
Manages push notifications and user preferences.

## Security Considerations

- **Token Security**: JWT tokens stored in secure keychain
- **Biometric Data**: Never stored, only used for local authentication
- **API Communication**: HTTPS only with certificate pinning
- **Data Encryption**: Sensitive data encrypted at rest
- **Code Obfuscation**: Production builds are obfuscated

## Performance Optimization

- **Lazy Loading**: Screens and components loaded on demand
- **Image Optimization**: Automatic image compression and caching
- **Bundle Splitting**: Separate bundles for different features
- **Memory Management**: Proper cleanup of listeners and timers

## Accessibility

- **Screen Reader Support**: All components properly labeled
- **High Contrast**: Support for high contrast themes
- **Font Scaling**: Respects system font size settings
- **Touch Targets**: Minimum 44pt touch targets

## Troubleshooting

### Common Issues

1. **Metro bundler issues**
   ```bash
   npx react-native start --reset-cache
   ```

2. **Android build failures**
   ```bash
   cd android && ./gradlew clean && cd ..
   ```

3. **iOS build failures**
   ```bash
   cd ios && rm -rf Pods && pod install && cd ..
   ```

4. **Keychain issues**
   - Reset iOS Simulator
   - Clear app data on Android

### Debug Tools

- **Flipper**: For debugging React Native apps
- **Reactotron**: For state management debugging
- **Chrome DevTools**: For JavaScript debugging

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For technical support or questions:
- Email: dsr-mobile-support@dswd.gov.ph
- Documentation: https://docs.dsr.gov.ph/mobile
- Issue Tracker: https://github.com/your-org/dsr-mobile-app/issues
