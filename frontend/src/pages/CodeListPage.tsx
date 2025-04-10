import { useState, useEffect, useCallback } from 'react'; 
import { useAuth } from 'react-oidc-context';

import { Header } from '../components/Header';
import { SortControls } from '../components/SortControls';
import { CodeTable } from '../components/CodeTable';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import {
    getPrivateCodes,
    getDecodedCodes,
    Code,
    OrderByField,
    OrderByDirection
} from '../services/api'; // Adjust path as needed

export function CodeListPage() {
    const auth = useAuth();

    const [codes, setCodes] = useState<Code[]>([]); // Store fetched codes (always Code objects for table)
    const [isLoading, setIsLoading] = useState<boolean>(true); // Start loading initially
    const [error, setError] = useState<string | null>(null);
    const [sortBy, setSortBy] = useState<OrderByField>('code');
    const [sortDir, setSortDir] = useState<OrderByDirection>('asc');

    const fetchData = useCallback(async () => {
        if (auth.isLoading) {
            console.log("Auth is loading, skipping fetch.");
            return;
        }

        setIsLoading(true);
        setError(null);
        console.log(`Workspaceing data... Auth: ${auth.isAuthenticated}, Sort: ${sortBy} ${sortDir}`);

        try {
            let fetchedCodes: Code[] = [];
            if (auth.isAuthenticated && auth.user?.access_token) {
                fetchedCodes = await getPrivateCodes(auth.user.access_token, sortBy, sortDir);
            } else {
                console.log("User not authenticated, clearing codes.");
                fetchedCodes = []; 
            }
            setCodes(fetchedCodes);
        } catch (err: any) {
            console.error("API Error:", err);
            setError(err.message || 'Failed to fetch codes.');
            setCodes([]); 
        } finally {
            setIsLoading(false);
        }
    }, [auth.isAuthenticated, auth.isLoading, auth.user?.access_token, sortBy, sortDir]); 

    
    useEffect(() => {
        fetchData();
    }, [fetchData]); 

    const handleDecodeClick = async () => {
        if (!auth.isAuthenticated || !auth.user?.access_token) {
            setError("You must be logged in as an admin to decode codes.");
            return;
        }

        setIsLoading(true);
        setError(null);
        console.log("Fetching decoded codes...");

        try {
            const decoded = await getDecodedCodes(auth.user.access_token);
            setCodes(decoded); 
        } catch (err: any) {
            console.error("API Error fetching decoded codes:", err);
            setError(err.message || 'Failed to fetch decoded codes.');
        } finally {
            setIsLoading(false);
        }
    };

    const checkIsAdmin = (): boolean => {
        const roleClaim = auth.user?.profile?.role; 
        if (!roleClaim) {
          return false; 
        }
  
        if (Array.isArray(roleClaim)) {
          return (roleClaim as string[]).includes('admin');
        }
  
        if (typeof roleClaim === 'string') {
          return roleClaim === 'admin';
        }
  
        return false;
      };
  
      const isAdmin = checkIsAdmin(); 


    return (
        <div className="container mx-auto p-4">
            <Header />

            <main>
                <h2 className="text-2xl font-semibold mb-3">Code List</h2>

                <SortControls
                    sortBy={sortBy}
                    sortDir={sortDir}
                    onSortByChange={setSortBy}
                    onSortDirChange={setSortDir}
                    disabled={!auth.isAuthenticated}
                />

                {/* Conditionally render Decode button for admins */}
                {auth.isAuthenticated && isAdmin && (
                    <button
                        onClick={handleDecodeClick}
                        disabled={isLoading} 
                        className="my-2 p-2 border rounded bg-green-500 text-white hover:bg-green-600 disabled:opacity-50"
                    >
                        {isLoading ? 'Decoding...' : 'Decode Base64 Codes (Admin)'}
                    </button>
                )}

                {isLoading && <LoadingSpinner />}
                <ErrorMessage message={error} />
                {!isLoading && !error && <CodeTable codes={codes} />}
            </main>
        </div>
    );
}