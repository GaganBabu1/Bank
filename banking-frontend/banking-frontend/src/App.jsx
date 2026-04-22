
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './components/Dashboard';
import CreateAccount from './components/CreateAccount';
import Deposit from './components/Deposit';
import Withdraw from './components/Withdraw';
import Transfer from './components/Transfer';
import TransactionHistory from './components/TransactionHistory';
import './App.css';

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/create-account" element={<CreateAccount />} />
          <Route path="/deposit" element={<Deposit />} />
          <Route path="/withdraw" element={<Withdraw />} />
          <Route path="/transfer" element={<Transfer />} />
          <Route path="/history" element={<TransactionHistory />} />
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;