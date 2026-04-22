import { useState } from 'react';

const Deposit = () => {
  const [formData, setFormData] = useState({
    accountNumber: '',
    amount: ''
  });
  const [response, setResponse] = useState(null);
  const [depositAmount, setDepositAmount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    if (!formData.accountNumber.trim()) {
      setError('Please enter account number');
      setLoading(false);
      return;
    }

    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      setError('Please enter a valid amount');
      setLoading(false);
      return;
    }

    try {
      const res = await fetch('http://localhost:8080/accounts/deposit', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          accountNumber: parseFloat(formData.accountNumber),
          amount: parseFloat(formData.amount)
        })
      });

      if (!res.ok) {
        throw new Error('Deposit failed. Please check the account number.');
      }

      const result = await res.json();
      setDepositAmount(parseFloat(formData.amount));
      setResponse(result);
      setFormData({ accountNumber: '', amount: '' });
    } catch (err) {
      setError(err.message || 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fade-in">
      <div className="card">
        <div className="card-header">
          <h2>Deposit Money</h2>
          <p>Add funds to your account</p>
        </div>

        {error && (
          <div className="alert alert-danger">
            <span>⚠️</span>
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="accountNumber">Account Number *</label>
              <input
                type="number"
                id="accountNumber"
                name="accountNumber"
                placeholder="Enter account number"
                value={formData.accountNumber}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="amount">Amount (₹) *</label>
              <input
                type="number"
                id="amount"
                name="amount"
                placeholder="0.00"
                value={formData.amount}
                onChange={handleChange}
                step="0.01"
                min="0.01"
                required
              />
            </div>
          </div>

          <button type="submit" className="btn btn-success btn-block" disabled={loading}>
            {loading ? 'Processing...' : 'Deposit Money'}
          </button>
        </form>
      </div>

      {response && (
        <div className="card fade-in">
          <div className="alert alert-success">
            <span>✓</span>
            <span>Deposit successful!</span>
          </div>

          <div style={{ display: 'grid', gap: '1rem' }}>
            <div style={{ 
              padding: '1rem', 
              backgroundColor: 'var(--border-light)', 
              borderRadius: '8px',
              borderLeft: '4px solid var(--success)'
            }}>
              <p style={{ margin: '0 0 0.5rem 0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
                Amount Deposited
              </p>
              <p style={{ margin: '0', fontSize: '1.5rem', fontWeight: '600', color: 'var(--success)' }}>
                +₹{depositAmount.toLocaleString('en-IN', { maximumFractionDigits: 2 })}
              </p>
            </div>

            <div style={{ 
              padding: '1rem', 
              backgroundColor: 'var(--border-light)', 
              borderRadius: '8px',
              borderLeft: '4px solid var(--info)'
            }}>
              <p style={{ margin: '0 0 0.5rem 0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
                New Balance
              </p>
              <p style={{ margin: '0', fontSize: '1.5rem', fontWeight: '600' }}>
                ₹{response.balance.toLocaleString('en-IN', { maximumFractionDigits: 2 })}
              </p>
            </div>

            {response.transactionId && (
              <div style={{ 
                padding: '1rem', 
                backgroundColor: 'var(--border-light)', 
                borderRadius: '8px',
                borderLeft: '4px solid var(--primary)'
              }}>
                <p style={{ margin: '0 0 0.5rem 0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
                  Transaction ID
                </p>
                <p style={{ margin: '0', fontSize: '0.95rem', fontWeight: '600', wordBreak: 'break-all' }}>
                  {response.transactionId}
                </p>
              </div>
            )}
          </div>

          <button 
            onClick={() => setResponse(null)} 
            className="btn btn-secondary btn-block"
            style={{ marginTop: '1rem' }}
          >
            Deposit More Money
          </button>
        </div>
      )}
    </div>
  );
};

export default Deposit;
