import { apiClient } from './client';
import type { Worker } from '../types/api';

export const getWorkers = async (): Promise<Worker[]> => {
  const response = await apiClient.get<Worker[]>('/system/workers');
  return response.data;
};
