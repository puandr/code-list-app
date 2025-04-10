import { CodeRow } from './CodeRow';
interface Code {
    code: string;
    type: string;
    name: string;
    category: string;
}

interface CodeTableProps {
  codes: Code[];
}

export function CodeTable({ codes }: CodeTableProps) {
  if (!codes || codes.length === 0) {
    return <div className="text-center p-4">No codes to display.</div>;
  }

  return (
    <div className="overflow-x-auto shadow rounded-lg">
      <table className="min-w-full bg-white">
        <thead className="bg-gray-200">
          <tr>
            <th className="py-2 px-4 text-left font-semibold text-gray-600">Code</th>
            <th className="py-2 px-4 text-left font-semibold text-gray-600">Name</th>
            <th className="py-2 px-4 text-left font-semibold text-gray-600">Type</th>
            <th className="py-2 px-4 text-left font-semibold text-gray-600">Category</th>
          </tr>
        </thead>
        <tbody>
          {codes.map((code) => (
            <CodeRow key={code.code} code={code} />
          ))}
        </tbody>
      </table>
    </div>
  );
}