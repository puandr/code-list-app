import { Routes, Route } from 'react-router-dom';
import { AuthenticationCallback } from './pages/AuthenticationCallback';
import HomePage from './pages/HomePage'; 

function App() {
  return (
    <div>
      {/* Header/Nav component might go here */}
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/authentication/callback" element={<AuthenticationCallback />} />
        {/* Other routes */}
      </Routes>
    </div>
  );
}

export default App;