
interface ErrorMessageProps {
  message: string | null;
}

export function ErrorMessage({ message }: ErrorMessageProps) {
  if (!message) {
    return null;
  }
  return (
    <div className="p-4 my-2 text-red-700 bg-red-100 border border-red-400 rounded" role="alert">
      <p>Error: {message}</p>
    </div>
  );
}