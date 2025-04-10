interface Code {
    code: string;
    type: string;
    name: string;
    category: string;
}


interface CodeRowProps {
  code: Code;
}

export function CodeRow({ code }: CodeRowProps) {
  return (
    <tr className="border-b hover:bg-gray-50">
      <td className="py-2 px-4">{code.code}</td>
      <td className="py-2 px-4">{code.name}</td>
      <td className="py-2 px-4">{code.type}</td>
      <td className="py-2 px-4">{code.category}</td>
    </tr>
  );
}