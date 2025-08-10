'use client';

// User List Component
// Displays a list of system users with management actions

import React from 'react';

import { Button, Badge, Loading } from '@/components/ui';
import type { User } from '@/types';

// User list props interface
interface UserListProps {
  users: User[];
  loading: boolean;
  currentUser: User | null;
  onUserEdit: (user: User) => void;
  onUserStatusChange: (userId: string, status: string) => void;
  onUserDelete: (userId: string) => void;
  onRefresh: () => void;
}

// Status badge variant mapping
const getStatusVariant = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'success';
    case 'PENDING_VERIFICATION':
      return 'warning';
    case 'INACTIVE':
      return 'secondary';
    case 'SUSPENDED':
      return 'error';
    default:
      return 'secondary';
  }
};

// Role badge variant mapping
const getRoleVariant = (role: string) => {
  switch (role) {
    case 'SYSTEM_ADMIN':
      return 'error';
    case 'DSWD_STAFF':
      return 'warning';
    case 'LGU_STAFF':
      return 'info';
    case 'CASE_WORKER':
      return 'secondary';
    case 'CITIZEN':
      return 'primary';
    default:
      return 'secondary';
  }
};

// Format role display name
const formatRole = (role: string) => {
  switch (role) {
    case 'SYSTEM_ADMIN':
      return 'System Admin';
    case 'DSWD_STAFF':
      return 'DSWD Staff';
    case 'LGU_STAFF':
      return 'LGU Staff';
    case 'CASE_WORKER':
      return 'Case Worker';
    case 'CITIZEN':
      return 'Citizen';
    default:
      return role;
  }
};

// User List component
export const UserList: React.FC<UserListProps> = ({
  users,
  loading,
  currentUser,
  onUserEdit,
  onUserStatusChange,
  onUserDelete,
  onRefresh,
}) => {
  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <Loading text="Loading users..." />
      </div>
    );
  }

  if (users.length === 0) {
    return (
      <div className="text-center py-8">
        <div className="text-gray-500 mb-4">
          <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
          </svg>
        </div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">No users found</h3>
        <p className="text-gray-600 mb-4">
          No users match your current search criteria.
        </p>
        <Button onClick={onRefresh}>Refresh List</Button>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Users Table */}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                User
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Role
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Last Login
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Created
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {users.map((user) => {
              const isCurrentUser = currentUser?.id === user.id;
              return (
                <tr key={user.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <div className="flex-shrink-0 h-10 w-10">
                        <div className="h-10 w-10 rounded-full bg-primary-100 flex items-center justify-center">
                          <span className="text-sm font-medium text-primary-700">
                            {user.firstName.charAt(0)}{user.lastName.charAt(0)}
                          </span>
                        </div>
                      </div>
                      <div className="ml-4">
                        <div className="text-sm font-medium text-gray-900 flex items-center">
                          {user.firstName} {user.lastName}
                          {isCurrentUser && (
                            <span className="ml-2 inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
                              You
                            </span>
                          )}
                        </div>
                        <div className="text-sm text-gray-500">{user.email}</div>
                        {user.phoneNumber && (
                          <div className="text-sm text-gray-500">{user.phoneNumber}</div>
                        )}
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <Badge variant={getRoleVariant(user.role)}>
                      {formatRole(user.role)}
                    </Badge>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <Badge variant={getStatusVariant(user.status)}>
                        {user.status}
                      </Badge>
                      {!user.emailVerified && (
                        <span className="ml-2 text-xs text-yellow-600">
                          Email not verified
                        </span>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {user.lastLoginAt 
                      ? new Date(user.lastLoginAt).toLocaleDateString()
                      : 'Never'
                    }
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(user.createdAt).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div className="flex justify-end space-x-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => onUserEdit(user)}
                      >
                        Edit
                      </Button>
                      
                      {!isCurrentUser && (
                        <>
                          {user.status === 'ACTIVE' ? (
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => onUserStatusChange(user.id, 'SUSPENDED')}
                              className="text-yellow-600 hover:text-yellow-700"
                            >
                              Suspend
                            </Button>
                          ) : user.status === 'SUSPENDED' ? (
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => onUserStatusChange(user.id, 'ACTIVE')}
                              className="text-green-600 hover:text-green-700"
                            >
                              Activate
                            </Button>
                          ) : null}
                          
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => onUserDelete(user.id)}
                            className="text-red-600 hover:text-red-700"
                          >
                            Delete
                          </Button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-between">
        <div className="text-sm text-gray-700">
          Showing {users.length} users
        </div>
        <div className="flex space-x-2">
          <Button variant="outline" size="sm" disabled>
            Previous
          </Button>
          <Button variant="outline" size="sm" disabled>
            Next
          </Button>
        </div>
      </div>
    </div>
  );
};
