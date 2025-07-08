import React, { useState, useEffect } from 'react';
import {
  View,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  Alert,
} from 'react-native';
import {
  Text,
  TextInput,
  Button,
  Card,
  Title,
  Paragraph,
  ActivityIndicator,
  IconButton,
  Divider,
} from 'react-native-paper';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';

import { AuthService, LoginCredentials } from '../../services/AuthService';
import { BiometricService } from '../../services/BiometricService';
import { useAuthStore } from '../../store/authStore';
import { theme } from '../../theme/theme';

interface LoginFormData {
  username: string;
  password: string;
}

const loginSchema = yup.object().shape({
  username: yup
    .string()
    .required('Username is required')
    .min(3, 'Username must be at least 3 characters'),
  password: yup
    .string()
    .required('Password is required')
    .min(6, 'Password must be at least 6 characters'),
});

const LoginScreen: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [canUseBiometrics, setCanUseBiometrics] = useState(false);
  const { login } = useAuthStore();

  const {
    control,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm<LoginFormData>({
    resolver: yupResolver(loginSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  });

  useEffect(() => {
    checkBiometricAvailability();
  }, []);

  const checkBiometricAvailability = async () => {
    try {
      const available = await BiometricService.canUseBiometrics();
      setCanUseBiometrics(available);
    } catch (error) {
      console.error('Failed to check biometric availability:', error);
    }
  };

  const onSubmit = async (data: LoginFormData) => {
    setIsLoading(true);
    try {
      const credentials: LoginCredentials = {
        username: data.username,
        password: data.password,
      };

      const response = await AuthService.login(credentials);
      await login(response.user, response.tokens);

      // Optionally prompt for biometric setup
      if (!canUseBiometrics) {
        promptBiometricSetup();
      }
    } catch (error) {
      console.error('Login failed:', error);
      Alert.alert(
        'Login Failed',
        error.message || 'Please check your credentials and try again.',
        [{ text: 'OK' }]
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleBiometricLogin = async () => {
    try {
      setIsLoading(true);
      const result = await BiometricService.loginWithBiometrics();
      
      if (result.success) {
        // In a real implementation, you would validate the biometric signature
        // with your backend and get user credentials
        Alert.alert('Success', 'Biometric login successful!');
        // For demo purposes, we'll just show success
      } else {
        Alert.alert(
          'Biometric Login Failed',
          result.error || 'Biometric authentication failed'
        );
      }
    } catch (error) {
      console.error('Biometric login error:', error);
      Alert.alert('Error', 'Biometric login failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const promptBiometricSetup = () => {
    Alert.alert(
      'Enable Biometric Login',
      'Would you like to enable biometric login for faster access?',
      [
        { text: 'Not Now', style: 'cancel' },
        {
          text: 'Enable',
          onPress: async () => {
            const success = await BiometricService.setupBiometricAuthentication();
            if (success) {
              setCanUseBiometrics(true);
            }
          },
        },
      ]
    );
  };

  const handleForgotPassword = () => {
    Alert.alert(
      'Forgot Password',
      'Please contact your system administrator to reset your password.',
      [{ text: 'OK' }]
    );
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView
        contentContainerStyle={styles.scrollContainer}
        keyboardShouldPersistTaps="handled"
      >
        <View style={styles.logoContainer}>
          <Title style={styles.title}>Dynamic Social Registry</Title>
          <Paragraph style={styles.subtitle}>
            Secure access to your social protection services
          </Paragraph>
        </View>

        <Card style={styles.loginCard}>
          <Card.Content>
            <Title style={styles.cardTitle}>Sign In</Title>

            <Controller
              control={control}
              name="username"
              render={({ field: { onChange, onBlur, value } }) => (
                <TextInput
                  label="Username"
                  mode="outlined"
                  value={value}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={!!errors.username}
                  style={styles.input}
                  autoCapitalize="none"
                  autoCorrect={false}
                  textContentType="username"
                  left={<TextInput.Icon icon="account" />}
                />
              )}
            />
            {errors.username && (
              <Text style={styles.errorText}>{errors.username.message}</Text>
            )}

            <Controller
              control={control}
              name="password"
              render={({ field: { onChange, onBlur, value } }) => (
                <TextInput
                  label="Password"
                  mode="outlined"
                  value={value}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={!!errors.password}
                  style={styles.input}
                  secureTextEntry={!showPassword}
                  textContentType="password"
                  left={<TextInput.Icon icon="lock" />}
                  right={
                    <TextInput.Icon
                      icon={showPassword ? 'eye-off' : 'eye'}
                      onPress={() => setShowPassword(!showPassword)}
                    />
                  }
                />
              )}
            />
            {errors.password && (
              <Text style={styles.errorText}>{errors.password.message}</Text>
            )}

            <Button
              mode="contained"
              onPress={handleSubmit(onSubmit)}
              loading={isLoading}
              disabled={isLoading}
              style={styles.loginButton}
              contentStyle={styles.buttonContent}
            >
              {isLoading ? 'Signing In...' : 'Sign In'}
            </Button>

            <Button
              mode="text"
              onPress={handleForgotPassword}
              style={styles.forgotButton}
            >
              Forgot Password?
            </Button>

            {canUseBiometrics && (
              <>
                <Divider style={styles.divider} />
                <View style={styles.biometricContainer}>
                  <Text style={styles.biometricText}>Or use biometric login</Text>
                  <IconButton
                    icon="fingerprint"
                    size={40}
                    onPress={handleBiometricLogin}
                    disabled={isLoading}
                    style={styles.biometricButton}
                  />
                </View>
              </>
            )}
          </Card.Content>
        </Card>

        <View style={styles.footer}>
          <Paragraph style={styles.footerText}>
            Department of Social Welfare and Development
          </Paragraph>
          <Paragraph style={styles.versionText}>Version 1.0.0</Paragraph>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  scrollContainer: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 20,
  },
  logoContainer: {
    alignItems: 'center',
    marginBottom: 40,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: theme.colors.primary,
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: theme.colors.onSurfaceVariant,
    textAlign: 'center',
  },
  loginCard: {
    elevation: 4,
    marginBottom: 20,
  },
  cardTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 20,
    color: theme.colors.onSurface,
  },
  input: {
    marginBottom: 8,
  },
  errorText: {
    color: theme.colors.error,
    fontSize: 12,
    marginBottom: 8,
    marginLeft: 4,
  },
  loginButton: {
    marginTop: 16,
    marginBottom: 8,
  },
  buttonContent: {
    paddingVertical: 8,
  },
  forgotButton: {
    marginBottom: 8,
  },
  divider: {
    marginVertical: 16,
  },
  biometricContainer: {
    alignItems: 'center',
  },
  biometricText: {
    fontSize: 14,
    color: theme.colors.onSurfaceVariant,
    marginBottom: 8,
  },
  biometricButton: {
    backgroundColor: theme.colors.primaryContainer,
  },
  footer: {
    alignItems: 'center',
    marginTop: 20,
  },
  footerText: {
    fontSize: 12,
    color: theme.colors.onSurfaceVariant,
    textAlign: 'center',
  },
  versionText: {
    fontSize: 10,
    color: theme.colors.onSurfaceVariant,
    marginTop: 4,
  },
});

export default LoginScreen;
