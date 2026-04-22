import { useState } from 'react';

const TransactionHistory = () => {
  const [accountNumber, setAccountNumber] = useState('');
  const [transactions, setTransactions] = useState([]);
  const [account, setAccount] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searched, setSearched] = useState(false);

  const handleChange = (e) => {
    setAccountNumber(e.target.value);
  };

  const parseTransaction = (txnString) => {
    // Parse strings like "Deposited: 1000" or "Withdrawn: 500" or "Transferred: 200"
    const depositMatch = txnString.match(/Deposited:\s*([\d.]+)/);
    const withdrawMatch = txnString.match(/Withdrawn:\s*([\d.]+)/);
    const transferMatch = txnString.match(/Transferred:\s*([\d.]+)/);

    if (depositMatch) {
      return {
        type: 'DEPOSIT',
        amount: parseFloat(depositMatch[1]),
        display: txnString
      };
    } else if (withdrawMatch) {
      return {
        type: 'WITHDRAW',
        amount: parseFloat(withdrawMatch[1]),
        display: txnString
      };
    } else if (transferMatch) {
      return {
        type: 'TRANSFER',
        amount: parseFloat(transferMatch[1]),
        display: txnString
      };
    }
    return {
      type: 'UNKNOWN',
      amount: 0,
      display: txnString
    };
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSearched(true);

    if (!accountNumber.trim()) {
      setError('Please enter account number');
      setLoading(false);
      return;
    }

    try {
      const res = await fetch(
        `http://localhost:8080/accounts/${accountNumber}`
      );

      if (!res.ok) {
        throw new Error('Account not found');
      }

      const accountData = await res.json();
      setAccount(accountData);
      
      // Parse the transaction strings
      const parsedTxns = (accountData.transactions || []).map((txn) => parseTransaction(txn));
      setTransactions(parsedTxns);
    } catch (err) {
      setError(err.message || 'An error occurred');
      setTransactions([]);
      setAccount(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fade-in">
      <div className="card">
        <div className="card-header">
          <h2>Transaction History</h2>
          <p>View all transactions for your account</p>
        </div>

        {error && (
          <div className="alert alert-danger">
            <span>⚠️</span>
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem' }}>
            <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
              <label htmlFor="accountNumber">Account Number *</label>
              <input
                type="number"
                id="accountNumber"
                placeholder="Enter account number to view transactions"
                value={accountNumber}
                onChange={handleChange}
                required
              />
            </div>
            <button 
              type="submit" 
              className="btn btn-primary" 
              disabled={loading}
              style={{ alignSelf: 'flex-end', marginBottom: 0 }}
            >
              {loading ? 'Loading...' : 'Search'}
            </button>
          </div>
        </form>
      </div>

      {searched && (
        <div className="card fade-in">
          {transactions.length > 0 ? (
            <>
              <div className="card-header">
                <h3 style={{ margin: 0 }}>
                  {transactions.length} Transaction{transactions.length !== 1 ? 's' : ''} Found
                </h3>
                <p style={{ margin: '0.5rem 0 0 0', color: 'var(--text-light)' }}>
                  Account: {account?.accountNumber} | Holder: {account?.accountHolderName}
                </p>
              </div>

              <div style={{ overflowX: 'auto' }}>
                <table className="table">
                  <thead>
                    <tr>
                      <th>Transaction</th>
                      <th>Type</th>
                      <th>Amount</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transactions.map((txn, index) => (
                      <tr key={index}>
                        <td style={{ fontSize: '0.9rem' }}>
                          {txn.display}
                        </td>
                        <td>
                          <span className={`badge badge-${txn.type === 'DEPOSIT' ? 'success' : txn.type === 'WITHDRAW' ? 'danger' : 'info'}`}>
                            {txn.type}
                          </span>
                        </td>
                        <td style={{ fontWeight: '600' }}>
                          <span style={{
                            color: txn.type === 'DEPOSIT' ? 'var(--success)' : 'var(--danger)'
                          }}>
                            {txn.type === 'DEPOSIT' ? '+' : '-'}
                            ₹{txn.amount.toLocaleString('en-IN', { maximumFractionDigits: 2 })}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div style={{ 
                marginTop: '1.5rem', 
                padding: '1rem', 
                backgroundColor: 'var(--border-light)', 
                borderRadius: '8px',
                textAlign: 'center'
              }}>
                <p style={{ margin: '0.5rem 0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
                  Total Transactions
                </p>
                <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: '600' }}>
                  {transactions.length}
                </p>
              </div>

              <div style={{ 
                marginTop: '1rem', 
                padding: '1rem', 
                backgroundColor: 'var(--border-light)', 
                borderRadius: '8px',
                textAlign: 'center'
              }}>
                <p style={{ margin: '0.5rem 0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
                  Current Balance
                </p>
                <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: '600', color: 'var(--primary)' }}>
                  ₹{account?.balance.toLocaleString('en-IN', { maximumFractionDigits: 2 })}
                </p>
              </div>
            </>
          ) : (
            <div style={{ textAlign: 'center', padding: '2rem' }}>
              <p style={{ fontSize: '3rem', margin: '1rem 0' }}>📭</p>
              <h3 style={{ margin: '0 0 0.5rem 0', color: 'var(--text)' }}>
                No Transactions Found
              </h3>
              <p style={{ color: 'var(--text-light)', margin: '0' }}>
                This account has no transaction history yet.
              </p>
              {error && (
                <div style={{ marginTop: '1.5rem', padding: '1rem', backgroundColor: 'var(--border-light)', borderRadius: '8px', textAlign: 'left' }}>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-light)', margin: '0 0 0.5rem 0' }}>
                    Debug Info:
                  </p>
                  <pre style={{ fontSize: '0.75rem', overflow: 'auto', margin: 0, color: 'var(--danger)' }}>
                    {error}
                  </pre>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {!searched && (
        <div className="card" style={{ textAlign: 'center', padding: '3rem 2rem' }}>
          <p style={{ fontSize: '2.5rem', margin: '1rem 0' }}>📊</p>
          <h3 style={{ margin: '0 0 0.5rem 0', color: 'var(--text)' }}>
            Enter an account number to view transactions
          </h3>
          <p style={{ color: 'var(--text-light)', margin: '0' }}>
            Use the search form above to get started
          </p>
        </div>
      )}
    </div>
  );
};

export default TransactionHistory;
