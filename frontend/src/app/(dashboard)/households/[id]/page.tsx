import React from 'react';

import { HouseholdDetailClient } from './household-detail-client';

interface HouseholdDetailPageProps {
  params: Promise<{
    id: string;
  }>;
}

export default async function HouseholdDetailPage({ params }: HouseholdDetailPageProps) {
  const { id } = await params;

  return <HouseholdDetailClient id={id} />;
}


