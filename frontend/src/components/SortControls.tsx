import React from 'react';

type OrderByField = 'code' | 'category' | 'name';
type OrderByDirection = 'asc' | 'desc';

interface SortControlsProps {
  sortBy: OrderByField;
  sortDir: OrderByDirection;
  onSortByChange: (field: OrderByField) => void;
  onSortDirChange: (direction: OrderByDirection) => void;
  disabled?: boolean; 
}

export function SortControls({
  sortBy,
  sortDir,
  onSortByChange,
  onSortDirChange,
  disabled = false
}: SortControlsProps) {

  const handleSortFieldChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    onSortByChange(event.target.value as OrderByField);
  };

  const toggleSortDirection = () => {
    onSortDirChange(sortDir === 'asc' ? 'desc' : 'asc');
  };

  return (
    <div className="flex items-center space-x-2 my-2 p-2 bg-gray-50 rounded border">
      <label htmlFor="sort-by" className="font-medium">Sort By:</label>
      <select
        id="sort-by"
        value={sortBy}
        onChange={handleSortFieldChange}
        disabled={disabled}
        className="p-1 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
      >
        <option value="code">Code</option>
        <option value="category">Category</option>
        <option value="name">Name</option>
      </select>

      <button
        onClick={toggleSortDirection}
        disabled={disabled}
        title={`Sort Direction: ${sortDir === 'asc' ? 'Ascending' : 'Descending'}`}
        className="p-1 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
      >
        {sortDir === 'asc' ? '↑ Asc' : '↓ Desc'}
      </button>
      {disabled && <span className="text-sm text-gray-500">(Log in to enable sorting)</span>}
    </div>
  );
}