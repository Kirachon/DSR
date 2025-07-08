'use client';

// User Management Page
// Interface for managing system users, roles, and permissions

import React, { useState, useEffect } from 'react';

import { UserFilters } from '@/components/admin/user-filters';
import { UserList } from '@/components/admin/user-list';
import { CreateUserModal } from '@/components/admin/create-user-modal';
import { EditUserModal } from '@/components/admin/edit-user-modal';
import { FormInput, FormSelect } from '@/components/forms';
import { Card, Button, Alert, Badge } from '@/components/ui';
import { useAuth } from '@/contexts';
import { registrationApi } from '@/lib/api';
import type { User, UserFilters as UserFiltersType, UserRole } from '@/types';

// User roles
const USER_ROLES = [
  { value: '', label: 'All Roles' },
  { value: 'CITIZEN', label: 'Citizen' },
  { value: 'LGU_STAFF', label: 'LGU Staff' },
  { value: 'DSWD_STAFF', label: 'DSWD Staff' },
  { value: 'CASE_WORKER', label: 'Case Worker' },
  { value: 'SYSTEM_ADMIN', label: 'System Admin' },
];

// User statuses
const USER_STATUSES = [
  { value: '', label: 'All Statuses' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
  { value: 'SUSPENDED', label: 'Suspended' },
  { value: 'PENDING_VERIFICATION', label: 'Pending Verification' },
];

// User Management page component
export default function UserManagementPage() {
  const { user: currentUser } = useAuth();

  // State management
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<UserFiltersType>({});
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  // Load users data
  useEffect(() => {
    loadUsers();
  }, [filters]);

  // Load users from API
  const loadUsers = async () => {
    try {
      setLoading(true);
      setError(null);

      // Get users from registration service
      const response = await registrationApi.getUsers({
        ...filters,
        search: searchQuery,
        page: 0,
        size: 50,
      });

      setUsers(response.content || []);
    } catch (err) {
      console.error('Failed to load users:', err);
      setError('Failed to load users. Please try again.');

      // Set empty array when API fails - no mock data fallback
      setUsers([]);
    } finally {
      setLoading(false);
    }
  };

  // Handle search
  const handleSearch = (query: string) => {
    setSearchQuery(query);
    setTimeout(() => loadUsers(), 300);
  };

  // Handle filter changes
  const handleFilterChange = (newFilters: UserFiltersType) => {
    setFilters(newFilters);
  };

  // Handle user creation
  const handleUserCreate = async (userData: any) => {
    try {
      const newUser = await registrationApi.createUser(userData);
      setUsers(prev => [newUser, ...prev]);
      setIsCreateModalOpen(false);
    } catch (err) {
      console.error('Failed to create user:', err);
      setError('Failed to create user. Please try again.');
    }
  };

  // Handle user edit
  const handleUserEdit = (user: User) => {
    setSelectedUser(user);
    setIsEditModalOpen(true);
  };

  // Handle user update
  const handleUserUpdate = async (userId: string, userData: any) => {
    try {
      const updatedUser = await registrationApi.updateUser(userId, userData);
      setUsers(prev => prev.map(user => user.id === userId ? updatedUser : user));
      setIsEditModalOpen(false);
      setSelectedUser(null);
    } catch (err) {
      console.error('Failed to update user:', err);
      setError('Failed to update user. Please try again.');
    }
  };

  // Handle user status change
  const handleUserStatusChange = async (userId: string, status: string) => {
    try {
      await registrationApi.updateUserStatus(userId, status);
      setUsers(prev => prev.map(user => user.id === userId ? { ...user, status } : user));
    } catch (err) {
      console.error('Failed to update user status:', err);
      setError('Failed to update user status. Please try again.');
    }
  };

  // Handle user deletion
  const handleUserDelete = async (userId: string) => {
    if (!confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      return;
    }

    try {
      await registrationApi.deleteUser(userId);
      setUsers(prev => prev.filter(user => user.id !== userId));
    } catch (err) {
      console.error('Failed to delete user:', err);
      setError('Failed to delete user. Please try again.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">User Management</h1>
          <p className="text-gray-600 mt-1">
            Manage system users, roles, and permissions
          </p>
        </div>

        <div className="flex space-x-3">
          <Button variant="outline" onClick={() => loadUsers()}>
            Refresh
          </Button>
          <Button onClick={() => setIsCreateModalOpen(true)}>
            Add New User
          </Button>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant="error" title="Error">
          {error}
        </Alert>
      )}

      {/* Search and Filters */}
      <Card className="p-6">
        <div className="flex flex-col lg:flex-row gap-4">
          <div className="flex-1">
            <FormInput
              label="Search Users"
              placeholder="Search by name, email, or phone number..."
              value={searchQuery}
              onChange={(e) => handleSearch(e.target.value)}
            />
          </div>
          <div className="lg:w-48">
            <FormSelect
              label="Role Filter"
              value={filters.role || ''}
              onChange={(e) => handleFilterChange({ ...filters, role: e.target.value as UserRole })}
              options={USER_ROLES}
            />
          </div>
          <div className="lg:w-48">
            <FormSelect
              label="Status Filter"
              value={filters.status || ''}
              onChange={(e) => handleFilterChange({ ...filters, status: e.target.value })}
              options={USER_STATUSES}
            />
          </div>
        </div>
      </Card>

      {/* User Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">
              {users.length}
            </div>
            <div className="text-sm text-gray-600">Total Users</div>
          </div>
        </Card>
        <Card className="p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">
              {users.filter(u => u.status === 'ACTIVE').length}
            </div>
            <div className="text-sm text-gray-600">Active Users</div>
          </div>
        </Card>
        <Card className="p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-yellow-600">
              {users.filter(u => u.status === 'PENDING_VERIFICATION').length}
            </div>
            <div className="text-sm text-gray-600">Pending Verification</div>
          </div>
        </Card>
        <Card className="p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-red-600">
              {users.filter(u => u.status === 'SUSPENDED').length}
            </div>
            <div className="text-sm text-gray-600">Suspended Users</div>
          </div>
        </Card>
      </div>

      {/* Users List */}
      <Card className="p-6">
        <UserList
          users={users}
          loading={loading}
          currentUser={currentUser}
          onUserEdit={handleUserEdit}
          onUserStatusChange={handleUserStatusChange}
          onUserDelete={handleUserDelete}
          onRefresh={loadUsers}
        />
      </Card>

      {/* Create User Modal */}
      <CreateUserModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSubmit={handleUserCreate}
      />

      {/* Edit User Modal */}
      <EditUserModal
        isOpen={isEditModalOpen}
        user={selectedUser}
        onClose={() => {
          setIsEditModalOpen(false);
          setSelectedUser(null);
        }}
        onSubmit={handleUserUpdate}
      />
    </div>
  );
}
