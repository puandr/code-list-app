import { AuthStatus } from './AuthStatus'; // Assuming AuthStatus is in the same directory

export function Header() {
  return (
    <header className="flex justify-between items-center p-4 bg-gray-100 shadow mb-4">
      <h1 className="text-xl font-bold">Code List Application</h1>
      <AuthStatus />
    </header>
  );
}