import { useState } from 'react';

const Transfer = () => {
  const [formData, setFormData] = useState({
    fromAccount: '',
    toAccount: '',
    amount: ''
  });
  const [response, setResponse] = useState(null);
  const [transferAmount, setTransferAmount] = useState(0);
  const [fromAccountDisplay, setFromAccountDisplay] = useState('');
  const [toAccountDisplay, setToAccountDisplay] = useState('');
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

    if (!formData.fromAccount.trim()) {
      setError('Please enter sender account number');
      setLoading(false);
      return;
    }

    if (!formData.toAccount.trim()) {
      setError('Please enter recipient account number');
      setLoading(false);
      return;
    }

    if (formData.fromAccount === formData.toAccount) {
      setError('Sender and recipient accounts cannot be the same');
      setLoading(false);
      return;
    }

    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      setError('Please enter a valid amount');
      setLoading(false);
      return;
    }

    try {
      const res = await fetch('http://localhost:8080/accounts/transfer', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          fromAccount: parseFloat(formData.fromAccount),
          toAccount: parseFloat(formData.toAccount),
          amount: parseFloat(formData.amount)
        })
      });

      if (!res.ok) {
        throw new Error('Transfer failed. Please check account numbers and balance.');
      }

      const result = await res.json();
      setTransferAmount(parseFloat(formData.amount));
      setFromAccountDisplay(formData.fromAccount);
      setToAccountDisplay(formData.toAccount);
      setResponse(result);
      setFormData({ fromAccount: '', toAccount: '', amount: '' });
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
          <h2>Transfer Money</h2>
          <p>Send money to another account instantly</p>
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
              <label htmlFor="fromAccount">From Account Number *</label>
              <input
                type="number"
                id="fromAccount"
                name="fromAccount"
                placeholder="Your account number"
                value={formData.fromAccount}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="toAccount">To Account Number *</label>
              <input
                type="number"
                id="toAccount"
                name="toAccount"
                placeholder="Recipient account number"
                value={formData.toAccount}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="amount">Transfer Amount (₹) *</label>
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

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Processing Transfer...' : 'Transfer Money'}
          </button>
        </form>
      </div>

      {response && (
        <div className="card fade-in">
          <div className="alert alert-success">
            <span>✓</span>
            <span>Transfer completed successfully!</span>
          </div>

          <div style={{ display: 'grid', gap: '1rem' }}>
            <div style={{ 
              padding: '1rem', 
              backgroundColor: 'var(--border-light)', 
              borderRadius: '8px',
              borderLeft: '4px solid var(--primary)'
            }}>
              <p style={{ margin: '0 0 0.5rem 0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
                From Account
              </p>
              <p style={{ margin: '0', fontSize: '1.1rem', fontWeight: '600' }}>
                {fromAccountDisplay}
              </p>
            </div>

            <div style={{ 
              padding: '1rem', 
              backgroundColor: 'var(--border-light)', 
              borderRadius: '8px',
              borderLeft: '4px solid var(--primary)'
            }}>
              <p style={{ margin: '0 0 0.5rem 0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
                To Account
              </p>
              <p style={{ margin: '0', fontSize: '1.1rem', fontWeight: '600' }}>
                {toAccountDisplay}
              </p>
            </div>

            <div style={{ 
              padding: '1rem', 
              backgroundColor: 'var(--border-light)', 
              borderRadius: '8px',
              borderLeft: '4px solid var(--success)'
            }}>
              <p style={{ margin: '0 0 0.5rem 0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
                Amount Transferred
              </p>
              <p style={{ margin: '0', fontSize: '1.5rem', fontWeight: '600', color: 'var(--success)' }}>
                ₹{transferAmount.toLocaleString('en-IN', { maximumFractionDigits: 2 })}
              </p>
            </div>

            {response.transactionId && (
              <div style={{ 
                padding: '1rem', 
                backgroundColor: 'var(--border-light)', 
                borderRadius: '8px',
                borderLeft: '4px solid var(--info)'
              }}>
                <p style={{ margin: '0 0 0.5rem 0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
                  Transaction ID
                </p>
                <p style={{ margin: '0', fontSize: '0.95rem', fontWeight: '600' }}>
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
            Make Another Transfer
          </button>
        </div>
      )}
    </div>
  );
};

export default Transfer;
