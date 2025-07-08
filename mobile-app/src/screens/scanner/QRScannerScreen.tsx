import React, { useState, useEffect } from 'react';
import {
  View,
  StyleSheet,
  Alert,
  Vibration,
  Dimensions,
  StatusBar,
} from 'react-native';
import {
  Text,
  Button,
  IconButton,
  Surface,
  ActivityIndicator,
} from 'react-native-paper';
import QRCodeScanner from 'react-native-qrcode-scanner';
import { RNCamera } from 'react-native-camera';
import { useNavigation } from '@react-navigation/native';

import { theme } from '../../theme/theme';
import { AuthService } from '../../services/AuthService';

interface QRData {
  type: 'psn' | 'household' | 'case' | 'verification';
  data: string;
  timestamp?: number;
  signature?: string;
}

const { width, height } = Dimensions.get('window');

const QRScannerScreen: React.FC = () => {
  const navigation = useNavigation();
  const [isScanning, setIsScanning] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [flashMode, setFlashMode] = useState(RNCamera.Constants.FlashMode.off);
  const [hasPermission, setHasPermission] = useState<boolean | null>(null);

  useEffect(() => {
    checkCameraPermission();
  }, []);

  const checkCameraPermission = async () => {
    try {
      const status = await RNCamera.getCameraStatus();
      setHasPermission(status === 'READY');
    } catch (error) {
      console.error('Failed to check camera permission:', error);
      setHasPermission(false);
    }
  };

  const onSuccess = async (e: any) => {
    if (!isScanning || isProcessing) return;

    setIsScanning(false);
    setIsProcessing(true);
    Vibration.vibrate(100);

    try {
      const qrData = parseQRCode(e.data);
      await processQRData(qrData);
    } catch (error) {
      console.error('QR processing failed:', error);
      Alert.alert(
        'Invalid QR Code',
        'The scanned QR code is not valid or supported.',
        [
          {
            text: 'Scan Again',
            onPress: () => {
              setIsScanning(true);
              setIsProcessing(false);
            },
          },
        ]
      );
    }
  };

  const parseQRCode = (data: string): QRData => {
    try {
      // Try to parse as JSON first
      const parsed = JSON.parse(data);
      
      if (parsed.type && parsed.data) {
        return parsed as QRData;
      }
      
      throw new Error('Invalid QR format');
    } catch (error) {
      // If not JSON, try to detect PSN format
      if (data.match(/^\d{4}-\d{4}-\d{4}$/)) {
        return {
          type: 'psn',
          data: data,
        };
      }
      
      // Check for household ID format
      if (data.startsWith('HH-') && data.length > 10) {
        return {
          type: 'household',
          data: data,
        };
      }
      
      // Check for case ID format
      if (data.startsWith('CASE-') && data.length > 15) {
        return {
          type: 'case',
          data: data,
        };
      }
      
      throw new Error('Unrecognized QR code format');
    }
  };

  const processQRData = async (qrData: QRData) => {
    try {
      switch (qrData.type) {
        case 'psn':
          await processPSNVerification(qrData.data);
          break;
        case 'household':
          await processHouseholdLookup(qrData.data);
          break;
        case 'case':
          await processCaseLookup(qrData.data);
          break;
        case 'verification':
          await processVerification(qrData);
          break;
        default:
          throw new Error(`Unsupported QR type: ${qrData.type}`);
      }
    } catch (error) {
      console.error('QR data processing failed:', error);
      Alert.alert(
        'Processing Failed',
        error.message || 'Failed to process the scanned QR code.',
        [
          {
            text: 'Scan Again',
            onPress: () => {
              setIsScanning(true);
              setIsProcessing(false);
            },
          },
        ]
      );
    }
  };

  const processPSNVerification = async (psn: string) => {
    try {
      const apiClient = AuthService.getApiClient();
      const response = await apiClient.get(`/data-management/philsys/verify/${psn}`);
      
      const verificationResult = response.data;
      
      Alert.alert(
        'PSN Verification Result',
        `PSN: ${psn}\nStatus: ${verificationResult.valid ? 'Valid' : 'Invalid'}\nName: ${verificationResult.firstName} ${verificationResult.lastName}`,
        [
          {
            text: 'View Details',
            onPress: () => {
              navigation.navigate('PersonDetails', { psn, verificationResult });
            },
          },
          {
            text: 'Scan Again',
            onPress: () => {
              setIsScanning(true);
              setIsProcessing(false);
            },
          },
        ]
      );
    } catch (error) {
      throw new Error('Failed to verify PSN. Please try again.');
    }
  };

  const processHouseholdLookup = async (householdId: string) => {
    try {
      const apiClient = AuthService.getApiClient();
      const response = await apiClient.get(`/registration/households/${householdId}`);
      
      const household = response.data;
      
      Alert.alert(
        'Household Found',
        `Household ID: ${householdId}\nHead: ${household.headOfHousehold.firstName} ${household.headOfHousehold.lastName}\nMembers: ${household.members.length}`,
        [
          {
            text: 'View Household',
            onPress: () => {
              navigation.navigate('HouseholdDetails', { householdId, household });
            },
          },
          {
            text: 'Scan Again',
            onPress: () => {
              setIsScanning(true);
              setIsProcessing(false);
            },
          },
        ]
      );
    } catch (error) {
      throw new Error('Household not found or access denied.');
    }
  };

  const processCaseLookup = async (caseId: string) => {
    try {
      const apiClient = AuthService.getApiClient();
      const response = await apiClient.get(`/grievance/cases/${caseId}`);
      
      const grievanceCase = response.data;
      
      Alert.alert(
        'Case Found',
        `Case ID: ${caseId}\nType: ${grievanceCase.caseType}\nStatus: ${grievanceCase.status}\nComplainant: ${grievanceCase.complainantName}`,
        [
          {
            text: 'View Case',
            onPress: () => {
              navigation.navigate('CaseDetails', { caseId, grievanceCase });
            },
          },
          {
            text: 'Scan Again',
            onPress: () => {
              setIsScanning(true);
              setIsProcessing(false);
            },
          },
        ]
      );
    } catch (error) {
      throw new Error('Case not found or access denied.');
    }
  };

  const processVerification = async (qrData: QRData) => {
    try {
      // Verify the signature if present
      if (qrData.signature) {
        const apiClient = AuthService.getApiClient();
        const response = await apiClient.post('/verification/validate', {
          data: qrData.data,
          signature: qrData.signature,
          timestamp: qrData.timestamp,
        });
        
        const isValid = response.data.valid;
        
        Alert.alert(
          'Verification Result',
          `Document verification: ${isValid ? 'Valid' : 'Invalid'}\nData: ${qrData.data}`,
          [
            {
              text: 'OK',
              onPress: () => {
                setIsScanning(true);
                setIsProcessing(false);
              },
            },
          ]
        );
      } else {
        throw new Error('No signature found for verification');
      }
    } catch (error) {
      throw new Error('Verification failed. Document may be invalid or tampered.');
    }
  };

  const toggleFlash = () => {
    setFlashMode(
      flashMode === RNCamera.Constants.FlashMode.off
        ? RNCamera.Constants.FlashMode.torch
        : RNCamera.Constants.FlashMode.off
    );
  };

  const handleClose = () => {
    navigation.goBack();
  };

  if (hasPermission === null) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" />
        <Text style={styles.loadingText}>Checking camera permission...</Text>
      </View>
    );
  }

  if (hasPermission === false) {
    return (
      <View style={styles.centerContainer}>
        <Text style={styles.errorText}>Camera permission denied</Text>
        <Button mode="contained" onPress={checkCameraPermission}>
          Request Permission
        </Button>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="black" />
      
      <QRCodeScanner
        onRead={onSuccess}
        flashMode={flashMode}
        showMarker={true}
        markerStyle={styles.marker}
        cameraStyle={styles.camera}
        topContent={
          <View style={styles.topContent}>
            <Text style={styles.instructionText}>
              Scan QR code for PSN verification, household lookup, or case details
            </Text>
          </View>
        }
        bottomContent={
          <View style={styles.bottomContent}>
            <Surface style={styles.controlsContainer}>
              <IconButton
                icon="close"
                size={30}
                iconColor="white"
                onPress={handleClose}
                style={styles.controlButton}
              />
              
              <IconButton
                icon={flashMode === RNCamera.Constants.FlashMode.off ? 'flash-off' : 'flash'}
                size={30}
                iconColor="white"
                onPress={toggleFlash}
                style={styles.controlButton}
              />
            </Surface>
            
            {isProcessing && (
              <View style={styles.processingContainer}>
                <ActivityIndicator size="large" color="white" />
                <Text style={styles.processingText}>Processing QR code...</Text>
              </View>
            )}
          </View>
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'black',
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: theme.colors.background,
    padding: 20,
  },
  camera: {
    height: height,
    width: width,
  },
  marker: {
    borderColor: theme.colors.primary,
    borderWidth: 3,
    borderRadius: 10,
  },
  topContent: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 20,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
  },
  instructionText: {
    fontSize: 18,
    color: 'white',
    textAlign: 'center',
    fontWeight: '500',
  },
  bottomContent: {
    flex: 1,
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 20,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
  },
  controlsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 25,
    paddingHorizontal: 20,
    paddingVertical: 10,
  },
  controlButton: {
    marginHorizontal: 20,
  },
  processingContainer: {
    alignItems: 'center',
    marginTop: 20,
  },
  processingText: {
    color: 'white',
    fontSize: 16,
    marginTop: 10,
  },
  loadingText: {
    marginTop: 10,
    fontSize: 16,
    color: theme.colors.onSurface,
  },
  errorText: {
    fontSize: 16,
    color: theme.colors.error,
    textAlign: 'center',
    marginBottom: 20,
  },
});

export default QRScannerScreen;
