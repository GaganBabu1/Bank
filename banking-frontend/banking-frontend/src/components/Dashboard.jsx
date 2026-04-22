import { useState } from 'react';

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalAccounts: 0,
    totalBalance: 0,
    totalTransactions: 0
  });

  return (
    <div className="fade-in">
      <div className="card">
        <div className="card-header">
          <h2>Welcome to Your Bank</h2>
          <p>Manage your accounts and transactions with ease</p>
        </div>
      </div>

      <div className="grid">
        <div className="stat-card">
          <h3>Total Accounts</h3>
          <p className="value">{stats.totalAccounts}</p>
          <p>Active accounts</p>
        </div>

        <div className="stat-card">
          <h3>Total Balance</h3>
          <p className="value">₹{stats.totalBalance.toLocaleString()}</p>
          <p>Across all accounts</p>
        </div>

        <div className="stat-card">
          <h3>Transactions</h3>
          <p className="value">{stats.totalTransactions}</p>
          <p>All time</p>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h2>Quick Actions</h2>
          <p>Common banking operations</p>
        </div>
        
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
          <div style={{ 
            padding: '1.5rem', 
            border: '1px solid var(--border)', 
            borderRadius: '8px',
            textAlign: 'center',
            cursor: 'pointer',
            transition: 'var(--transition)'
          }}
          onMouseEnter={(e) => e.currentTarget.style.boxShadow = 'var(--shadow-lg)'}
          onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'var(--shadow)'}
          >
            <h3 style={{ fontSize: '2rem', margin: '0 0 0.5rem 0' }}>➕</h3>
            <h4 style={{ margin: '0 0 0.5rem 0' }}>Create Account</h4>
            <p style={{ margin: '0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
              Open a new bank account
            </p>
          </div>

          <div style={{ 
            padding: '1.5rem', 
            border: '1px solid var(--border)', 
            borderRadius: '8px',
            textAlign: 'center',
            cursor: 'pointer',
            transition: 'var(--transition)'
          }}
          onMouseEnter={(e) => e.currentTarget.style.boxShadow = 'var(--shadow-lg)'}
          onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'var(--shadow)'}
          >
            <h3 style={{ fontSize: '2rem', margin: '0 0 0.5rem 0' }}>💰</h3>
            <h4 style={{ margin: '0 0 0.5rem 0' }}>Deposit Money</h4>
            <p style={{ margin: '0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
              Add funds to your account
            </p>
          </div>

          <div style={{ 
            padding: '1.5rem', 
            border: '1px solid var(--border)', 
            borderRadius: '8px',
            textAlign: 'center',
            cursor: 'pointer',
            transition: 'var(--transition)'
          }}
          onMouseEnter={(e) => e.currentTarget.style.boxShadow = 'var(--shadow-lg)'}
          onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'var(--shadow)'}
          >
            <h3 style={{ fontSize: '2rem', margin: '0 0 0.5rem 0' }}>🏧</h3>
            <h4 style={{ margin: '0 0 0.5rem 0' }}>Withdraw</h4>
            <p style={{ margin: '0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
              Withdraw cash from account
            </p>
          </div>

          <div style={{ 
            padding: '1.5rem', 
            border: '1px solid var(--border)', 
            borderRadius: '8px',
            textAlign: 'center',
            cursor: 'pointer',
            transition: 'var(--transition)'
          }}
          onMouseEnter={(e) => e.currentTarget.style.boxShadow = 'var(--shadow-lg)'}
          onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'var(--shadow)'}
          >
            <h3 style={{ fontSize: '2rem', margin: '0 0 0.5rem 0' }}>↔️</h3>
            <h4 style={{ margin: '0 0 0.5rem 0' }}>Transfer Money</h4>
            <p style={{ margin: '0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
              Send money to another account
            </p>
          </div>

          <div style={{ 
            padding: '1.5rem', 
            border: '1px solid var(--border)', 
            borderRadius: '8px',
            textAlign: 'center',
            cursor: 'pointer',
            transition: 'var(--transition)'
          }}
          onMouseEnter={(e) => e.currentTarget.style.boxShadow = 'var(--shadow-lg)'}
          onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'var(--shadow)'}
          >
            <h3 style={{ fontSize: '2rem', margin: '0 0 0.5rem 0' }}>📜</h3>
            <h4 style={{ margin: '0 0 0.5rem 0' }}>Transaction History</h4>
            <p style={{ margin: '0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
              View your account statements
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
