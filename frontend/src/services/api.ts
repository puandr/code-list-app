import apiClient from './apiClient';
import type { AxiosRequestConfig } from 'axios';

export interface Code {
    code: string;
    type: string;
    name: string;
    category: string;
}
export interface UserInfo {
    name: string;
    roles: string[];
}
export type OrderByField = 'code' | 'category' | 'name';
export type OrderByDirection = 'asc' | 'desc';

const createAuthHeaders = (token: string): { Authorization: string } => ({
    Authorization: `Bearer ${token}`,
});

export const getPublicCodes = async (): Promise<string[]> => {
    try {
        const response = await apiClient.get<string[]>('/public/codes');
        return response.data;
    } catch (error) {
        console.error('Error fetching public codes:', error);
        throw error;
    }
};

export const getPublicCode = async (codeId: string): Promise<Code> => {
    try {
        const response = await apiClient.get<Code>(`/public/code/${codeId}`);
        return response.data;
    } catch (error) {
        console.error(`Error fetching public code ${codeId}:`, error);
        throw error;
    }
};

export const getPrivateCodes = async (
    token: string | undefined,
    orderBy: OrderByField,
    orderDir: OrderByDirection
): Promise<Code[]> => {
    if (!token) {
        console.warn('Attempted to fetch private codes without a token.');
        return []; 
    }
    try {
        const config: AxiosRequestConfig = {
            headers: createAuthHeaders(token),
            params: { 
                orderby: orderBy,
                orderbydirection: orderDir,
            },
        };
        const response = await apiClient.get<Code[]>('/private/codes', config);
        return response.data;
    } catch (error) {
        console.error('Error fetching private codes:', error);
        throw error;
    }
};

export const getUserInfo = async (token: string | undefined): Promise<UserInfo | null> => {
    if (!token) {
        console.warn('Attempted to fetch user info without a token.');
        return null;
    }
    try {
        const config: AxiosRequestConfig = {
            headers: createAuthHeaders(token),
        };
        const response = await apiClient.get<UserInfo>('/private/userinfo', config);
        return response.data;
    } catch (error) {
        console.error('Error fetching user info:', error);
        throw error;
    }
};

export const getDecodedCodes = async (token: string | undefined): Promise<Code[]> => {
    if (!token) {
        console.warn('Attempted to fetch decoded codes without a token.');
        return [];
    }
    try {
        const config: AxiosRequestConfig = {
            headers: createAuthHeaders(token),
        };
        const response = await apiClient.get<Code[]>('/private/decodedcodes', config);
        return response.data;
    } catch (error) {
        console.error('Error fetching decoded codes:', error);
        throw error;
    }
};