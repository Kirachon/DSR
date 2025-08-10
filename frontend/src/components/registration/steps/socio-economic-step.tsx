'use client';

// Socio-Economic Information Step Component
// Third step of household registration wizard

import React, { useState } from 'react';

import { FormInput, FormSelect } from '@/components/forms';
import { Button, Card, Modal } from '@/components/ui';
import type {
  SocioEconomicInfo,
  Asset,
  Vulnerability,
  HouseholdRegistrationData,
} from '@/types';

// Component props interface
interface SocioEconomicStepProps {
  data: SocioEconomicInfo;
  onUpdate: (data: Partial<HouseholdRegistrationData>) => void;
  errors: string[];
}

// Income source options
const INCOME_SOURCE_OPTIONS = [
  { value: '', label: 'Select Primary Income Source' },
  { value: 'EMPLOYMENT', label: 'Employment (Salary/Wages)' },
  { value: 'BUSINESS', label: 'Business/Self-Employment' },
  { value: 'AGRICULTURE', label: 'Agriculture/Farming' },
  { value: 'FISHING', label: 'Fishing' },
  { value: 'REMITTANCES', label: 'Remittances (OFW/Local)' },
  { value: 'PENSION', label: 'Pension/Retirement' },
  { value: 'SOCIAL_ASSISTANCE', label: 'Social Assistance/Welfare' },
  { value: 'OTHER', label: 'Other' },
];

// Housing type options
const HOUSING_TYPE_OPTIONS = [
  { value: '', label: 'Select Housing Type' },
  { value: 'SINGLE_DETACHED', label: 'Single Detached House' },
  { value: 'DUPLEX', label: 'Duplex' },
  { value: 'APARTMENT', label: 'Apartment' },
  { value: 'CONDOMINIUM', label: 'Condominium' },
  { value: 'TOWNHOUSE', label: 'Townhouse' },
  { value: 'INFORMAL_SETTLEMENT', label: 'Informal Settlement' },
  { value: 'BOARDING_HOUSE', label: 'Boarding House' },
  { value: 'INSTITUTION', label: 'Institution' },
  { value: 'OTHER', label: 'Other' },
];

// Housing ownership options
const HOUSING_OWNERSHIP_OPTIONS = [
  { value: '', label: 'Select Housing Ownership' },
  { value: 'OWNED', label: 'Owned' },
  { value: 'RENTED', label: 'Rented' },
  { value: 'RENT_FREE_WITH_CONSENT', label: 'Rent-free with consent of owner' },
  {
    value: 'RENT_FREE_WITHOUT_CONSENT',
    label: 'Rent-free without consent of owner',
  },
  { value: 'OTHER', label: 'Other' },
];

// Asset type options
const ASSET_TYPE_OPTIONS = [
  { value: '', label: 'Select Asset Type' },
  { value: 'REAL_ESTATE', label: 'Real Estate' },
  { value: 'VEHICLE', label: 'Vehicle' },
  { value: 'LIVESTOCK', label: 'Livestock' },
  { value: 'APPLIANCES', label: 'Appliances' },
  { value: 'ELECTRONICS', label: 'Electronics' },
  { value: 'JEWELRY', label: 'Jewelry' },
  { value: 'SAVINGS', label: 'Savings/Bank Deposits' },
  { value: 'INVESTMENTS', label: 'Investments' },
  { value: 'OTHER', label: 'Other' },
];

// Vulnerability type options
const VULNERABILITY_TYPE_OPTIONS = [
  { value: '', label: 'Select Vulnerability Type' },
  { value: 'POVERTY', label: 'Extreme Poverty' },
  { value: 'DISABILITY', label: 'Disability' },
  { value: 'CHRONIC_ILLNESS', label: 'Chronic Illness' },
  { value: 'ELDERLY', label: 'Elderly (Senior Citizen)' },
  { value: 'PREGNANT_LACTATING', label: 'Pregnant/Lactating Mother' },
  { value: 'CHILD_LABOR', label: 'Child Labor' },
  { value: 'OUT_OF_SCHOOL', label: 'Out-of-School Youth' },
  { value: 'UNEMPLOYED', label: 'Unemployed' },
  { value: 'INFORMAL_WORKER', label: 'Informal Worker' },
  { value: 'DISASTER_AFFECTED', label: 'Disaster Affected' },
  { value: 'CONFLICT_AFFECTED', label: 'Conflict Affected' },
  { value: 'OTHER', label: 'Other' },
];

// Severity options
const SEVERITY_OPTIONS = [
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
  { value: 'CRITICAL', label: 'Critical' },
];

// Initial asset data
const initialAssetData: Asset = {
  id: '',
  type: '',
  description: '',
  estimatedValue: 0,
};

// Initial vulnerability data
const initialVulnerabilityData: Vulnerability = {
  id: '',
  type: '',
  description: '',
  severity: 'LOW',
};

// Socio-Economic Step component
export const SocioEconomicStep: React.FC<SocioEconomicStepProps> = ({
  data,
  onUpdate,
  errors,
}) => {
  const [isAssetModalOpen, setIsAssetModalOpen] = useState(false);
  const [isVulnerabilityModalOpen, setIsVulnerabilityModalOpen] =
    useState(false);
  const [editingAsset, setEditingAsset] = useState<Asset | null>(null);
  const [editingVulnerability, setEditingVulnerability] =
    useState<Vulnerability | null>(null);
  const [assetData, setAssetData] = useState<Asset>(initialAssetData);
  const [vulnerabilityData, setVulnerabilityData] = useState<Vulnerability>(
    initialVulnerabilityData
  );

  // Handle socio-economic info updates
  const handleSocioEconomicChange = (
    field: keyof SocioEconomicInfo,
    value: any
  ) => {
    onUpdate({
      socioEconomicInfo: {
        ...data,
        [field]: value,
      },
    });
  };

  // Handle utility access changes
  const handleUtilityChange = (utility: string, value: boolean) => {
    handleSocioEconomicChange('accessToUtilities', {
      ...data.accessToUtilities,
      [utility]: value,
    });
  };

  // Asset management functions
  const handleAddAsset = () => {
    setEditingAsset(null);
    setAssetData({ ...initialAssetData, id: Date.now().toString() });
    setIsAssetModalOpen(true);
  };

  const handleEditAsset = (asset: Asset) => {
    setEditingAsset(asset);
    setAssetData(asset);
    setIsAssetModalOpen(true);
  };

  const handleSaveAsset = () => {
    if (!assetData.type || !assetData.description) return;

    let updatedAssets: Asset[];
    if (editingAsset) {
      updatedAssets = data.assets.map(asset =>
        asset.id === editingAsset.id ? assetData : asset
      );
    } else {
      updatedAssets = [...data.assets, assetData];
    }

    handleSocioEconomicChange('assets', updatedAssets);
    setIsAssetModalOpen(false);
  };

  const handleRemoveAsset = (assetId: string) => {
    const updatedAssets = data.assets.filter(asset => asset.id !== assetId);
    handleSocioEconomicChange('assets', updatedAssets);
  };

  // Vulnerability management functions
  const handleAddVulnerability = () => {
    setEditingVulnerability(null);
    setVulnerabilityData({
      ...initialVulnerabilityData,
      id: Date.now().toString(),
    });
    setIsVulnerabilityModalOpen(true);
  };

  const handleEditVulnerability = (vulnerability: Vulnerability) => {
    setEditingVulnerability(vulnerability);
    setVulnerabilityData(vulnerability);
    setIsVulnerabilityModalOpen(true);
  };

  const handleSaveVulnerability = () => {
    if (!vulnerabilityData.type || !vulnerabilityData.description) return;

    let updatedVulnerabilities: Vulnerability[];
    if (editingVulnerability) {
      updatedVulnerabilities = data.vulnerabilities.map(vuln =>
        vuln.id === editingVulnerability.id ? vulnerabilityData : vuln
      );
    } else {
      updatedVulnerabilities = [...data.vulnerabilities, vulnerabilityData];
    }

    handleSocioEconomicChange('vulnerabilities', updatedVulnerabilities);
    setIsVulnerabilityModalOpen(false);
  };

  const handleRemoveVulnerability = (vulnerabilityId: string) => {
    const updatedVulnerabilities = data.vulnerabilities.filter(
      vuln => vuln.id !== vulnerabilityId
    );
    handleSocioEconomicChange('vulnerabilities', updatedVulnerabilities);
  };

  return (
    <div className='space-y-8'>
      {/* Household Income Information */}
      <div>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Household Income Information
        </h4>
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          <FormInput
            label='Household Size'
            type='number'
            value={data.householdSize.toString()}
            onChange={value =>
              handleSocioEconomicChange(
                'householdSize',
                parseInt(
                  typeof value === 'string' ? value : value.target.value
                ) || 1
              )
            }
            placeholder='Number of household members'
            min='1'
            required
          />

          <FormInput
            label='Total Monthly Income (PHP)'
            type='number'
            value={data.totalMonthlyIncome.toString()}
            onChange={value =>
              handleSocioEconomicChange(
                'totalMonthlyIncome',
                parseFloat(
                  typeof value === 'string' ? value : value.target.value
                ) || 0
              )
            }
            placeholder='0.00'
            min='0'
            step='0.01'
            required
          />

          <FormSelect
            label='Primary Income Source *'
            value={data.primaryIncomeSource}
            onChange={value =>
              handleSocioEconomicChange('primaryIncomeSource', value)
            }
            options={INCOME_SOURCE_OPTIONS}
            required
          />
        </div>
      </div>

      {/* Housing Information */}
      <div>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Housing Information
        </h4>
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          <FormSelect
            label='Housing Type *'
            value={data.housingType}
            onChange={value => handleSocioEconomicChange('housingType', value)}
            options={HOUSING_TYPE_OPTIONS}
            required
          />

          <FormSelect
            label='Housing Ownership *'
            value={data.housingOwnership}
            onChange={value =>
              handleSocioEconomicChange('housingOwnership', value)
            }
            options={HOUSING_OWNERSHIP_OPTIONS}
            required
          />
        </div>
      </div>

      {/* Access to Utilities */}
      <div>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Access to Utilities
        </h4>
        <div className='grid grid-cols-2 md:grid-cols-4 gap-4'>
          <div className='flex items-center space-x-3'>
            <input
              type='checkbox'
              id='electricity'
              checked={data.accessToUtilities.electricity}
              onChange={e =>
                handleUtilityChange('electricity', e.target.checked)
              }
              className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
            />
            <label
              htmlFor='electricity'
              className='text-sm font-medium text-gray-700'
            >
              Electricity
            </label>
          </div>

          <div className='flex items-center space-x-3'>
            <input
              type='checkbox'
              id='water'
              checked={data.accessToUtilities.water}
              onChange={e => handleUtilityChange('water', e.target.checked)}
              className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
            />
            <label
              htmlFor='water'
              className='text-sm font-medium text-gray-700'
            >
              Water Supply
            </label>
          </div>

          <div className='flex items-center space-x-3'>
            <input
              type='checkbox'
              id='internet'
              checked={data.accessToUtilities.internet}
              onChange={e => handleUtilityChange('internet', e.target.checked)}
              className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
            />
            <label
              htmlFor='internet'
              className='text-sm font-medium text-gray-700'
            >
              Internet
            </label>
          </div>

          <div className='flex items-center space-x-3'>
            <input
              type='checkbox'
              id='sewerage'
              checked={data.accessToUtilities.sewerage}
              onChange={e => handleUtilityChange('sewerage', e.target.checked)}
              className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
            />
            <label
              htmlFor='sewerage'
              className='text-sm font-medium text-gray-700'
            >
              Sewerage
            </label>
          </div>
        </div>
      </div>

      {/* Assets Section */}
      <div>
        <div className='flex justify-between items-center mb-4'>
          <h4 className='text-lg font-medium text-gray-900'>
            Household Assets ({data.assets.length})
          </h4>
          <Button onClick={handleAddAsset}>Add Asset</Button>
        </div>

        {data.assets.length === 0 ? (
          <Card className='p-6 text-center'>
            <div className='text-gray-500'>
              <svg
                className='mx-auto h-12 w-12 text-gray-400 mb-4'
                fill='none'
                stroke='currentColor'
                viewBox='0 0 24 24'
              >
                <path
                  strokeLinecap='round'
                  strokeLinejoin='round'
                  strokeWidth={2}
                  d='M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4'
                />
              </svg>
              <h3 className='text-lg font-medium text-gray-900 mb-2'>
                No assets added
              </h3>
              <p className='text-gray-600 mb-4'>
                Add household assets to provide a complete picture of your
                economic situation.
              </p>
              <Button onClick={handleAddAsset}>Add First Asset</Button>
            </div>
          </Card>
        ) : (
          <div className='space-y-3'>
            {data.assets.map(asset => (
              <Card key={asset.id} className='p-4'>
                <div className='flex items-center justify-between'>
                  <div className='flex-1'>
                    <div className='flex items-center space-x-3'>
                      <div className='flex-1'>
                        <h5 className='font-medium text-gray-900'>
                          {asset.description}
                        </h5>
                        <div className='text-sm text-gray-600'>
                          <span>{asset.type.replace('_', ' ')}</span>
                          <span className='mx-2'>•</span>
                          <span>₱{asset.estimatedValue.toLocaleString()}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div className='flex items-center space-x-2'>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => handleEditAsset(asset)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => handleRemoveAsset(asset.id)}
                      className='text-red-600 hover:text-red-700 hover:border-red-300'
                    >
                      Remove
                    </Button>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>

      {/* Vulnerabilities Section */}
      <div>
        <div className='flex justify-between items-center mb-4'>
          <h4 className='text-lg font-medium text-gray-900'>
            Household Vulnerabilities ({data.vulnerabilities.length})
          </h4>
          <Button onClick={handleAddVulnerability}>Add Vulnerability</Button>
        </div>

        {data.vulnerabilities.length === 0 ? (
          <Card className='p-6 text-center'>
            <div className='text-gray-500'>
              <svg
                className='mx-auto h-12 w-12 text-gray-400 mb-4'
                fill='none'
                stroke='currentColor'
                viewBox='0 0 24 24'
              >
                <path
                  strokeLinecap='round'
                  strokeLinejoin='round'
                  strokeWidth={2}
                  d='M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z'
                />
              </svg>
              <h3 className='text-lg font-medium text-gray-900 mb-2'>
                No vulnerabilities identified
              </h3>
              <p className='text-gray-600 mb-4'>
                Identify any vulnerabilities that may affect your household's
                well-being.
              </p>
              <Button onClick={handleAddVulnerability}>
                Add Vulnerability
              </Button>
            </div>
          </Card>
        ) : (
          <div className='space-y-3'>
            {data.vulnerabilities.map(vulnerability => (
              <Card key={vulnerability.id} className='p-4'>
                <div className='flex items-center justify-between'>
                  <div className='flex-1'>
                    <div className='flex items-center space-x-3'>
                      <div className='flex-1'>
                        <div className='flex items-center space-x-2'>
                          <h5 className='font-medium text-gray-900'>
                            {vulnerability.description}
                          </h5>
                          <span
                            className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                              vulnerability.severity === 'CRITICAL'
                                ? 'bg-red-100 text-red-800'
                                : vulnerability.severity === 'HIGH'
                                  ? 'bg-orange-100 text-orange-800'
                                  : vulnerability.severity === 'MEDIUM'
                                    ? 'bg-yellow-100 text-yellow-800'
                                    : 'bg-green-100 text-green-800'
                            }`}
                          >
                            {vulnerability.severity}
                          </span>
                        </div>
                        <div className='text-sm text-gray-600'>
                          {vulnerability.type.replace('_', ' ')}
                        </div>
                      </div>
                    </div>
                  </div>
                  <div className='flex items-center space-x-2'>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => handleEditVulnerability(vulnerability)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() =>
                        handleRemoveVulnerability(vulnerability.id)
                      }
                      className='text-red-600 hover:text-red-700 hover:border-red-300'
                    >
                      Remove
                    </Button>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>

      {/* Asset Modal */}
      <Modal
        isOpen={isAssetModalOpen}
        onClose={() => setIsAssetModalOpen(false)}
        title={editingAsset ? 'Edit Asset' : 'Add Asset'}
      >
        <div className='space-y-4'>
          <FormSelect
            label='Asset Type *'
            value={assetData.type}
            onChange={value =>
              setAssetData(prev => ({
                ...prev,
                type: typeof value === 'string' ? value : value.target.value,
              }))
            }
            options={ASSET_TYPE_OPTIONS}
            required
          />

          <FormInput
            label='Description *'
            value={assetData.description}
            onChange={value =>
              setAssetData(prev => ({
                ...prev,
                description:
                  typeof value === 'string' ? value : value.target.value,
              }))
            }
            placeholder='Describe the asset'
            required
          />

          <FormInput
            label='Estimated Value (PHP)'
            type='number'
            value={assetData.estimatedValue.toString()}
            onChange={value =>
              setAssetData(prev => ({
                ...prev,
                estimatedValue:
                  parseFloat(
                    typeof value === 'string' ? value : value.target.value
                  ) || 0,
              }))
            }
            placeholder='0.00'
            min='0'
            step='0.01'
          />
        </div>

        <div className='flex justify-end space-x-3 mt-6'>
          <Button variant='outline' onClick={() => setIsAssetModalOpen(false)}>
            Cancel
          </Button>
          <Button onClick={handleSaveAsset}>
            {editingAsset ? 'Update Asset' : 'Add Asset'}
          </Button>
        </div>
      </Modal>

      {/* Vulnerability Modal */}
      <Modal
        isOpen={isVulnerabilityModalOpen}
        onClose={() => setIsVulnerabilityModalOpen(false)}
        title={
          editingVulnerability ? 'Edit Vulnerability' : 'Add Vulnerability'
        }
      >
        <div className='space-y-4'>
          <FormSelect
            label='Vulnerability Type *'
            value={vulnerabilityData.type}
            onChange={value =>
              setVulnerabilityData(prev => ({
                ...prev,
                type: typeof value === 'string' ? value : value.target.value,
              }))
            }
            options={VULNERABILITY_TYPE_OPTIONS}
            required
          />

          <FormInput
            label='Description *'
            value={vulnerabilityData.description}
            onChange={value =>
              setVulnerabilityData(prev => ({
                ...prev,
                description:
                  typeof value === 'string' ? value : value.target.value,
              }))
            }
            placeholder='Describe the vulnerability'
            required
          />

          <FormSelect
            label='Severity *'
            value={vulnerabilityData.severity}
            onChange={value =>
              setVulnerabilityData(prev => ({
                ...prev,
                severity: value as any,
              }))
            }
            options={SEVERITY_OPTIONS}
            required
          />
        </div>

        <div className='flex justify-end space-x-3 mt-6'>
          <Button
            variant='outline'
            onClick={() => setIsVulnerabilityModalOpen(false)}
          >
            Cancel
          </Button>
          <Button onClick={handleSaveVulnerability}>
            {editingVulnerability
              ? 'Update Vulnerability'
              : 'Add Vulnerability'}
          </Button>
        </div>
      </Modal>
    </div>
  );
};
