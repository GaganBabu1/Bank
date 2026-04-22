import { useState } from 'react';

const CreateAccount = () => {
  const [formData, setFormData] = useState({
    name: '',
    balance: ''
  });
  const [response, setResponse] = useState(null);
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

    if (!formData.name.trim()) {
      setError('Please enter account holder name');
      setLoading(false);
      return;
    }

    if (!formData.balance || parseFloat(formData.balance) < 0) {
      setError('Please enter a valid starting balance');
      setLoading(false);
      return;
    }

    try {
      const res = await fetch('http://localhost:8080/accounts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          accountHolderName: formData.name,
          balance: parseFloat(formData.balance)
        })
      });

      if (!res.ok) {
        throw new Error('Failed to create account');
      }

      const result = await res.json();
      setResponse(result);
      setFormData({ name: '', balance: '' });
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
          <h2>Create New Account</h2>
          <p>Open a new bank account in minutes</p>
        </div>

        {error && (
          <div className="alert alert-danger">
            <span>⚠️</span>
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">Account Holder Name *</label>
            <input
              type="text"
              id="name"
              name="name"
              placeholder="Enter your full name"
              value={formData.name}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="balance">Initial Balance (₹) *</label>
            <input
              type="number"
              id="balance"
              name="balance"
              placeholder="0.00"
              value={formData.balance}
              onChange={handleChange}
              step="0.01"
              min="0"
              required
            />
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>
      </div>

      {response && (
        <div className="card fade-in">
          <div className="alert alert-success">
            <span>✓</span>
            <span>Account created successfully!</span>
          </div>

          <div style={{ display: 'grid', gap: '1rem' }}>
            <div style={{ 
              padding: '1rem', 
              backgroundColor: 'var(--border-light)', 
              borderRadius: '8px',
              borderLeft: '4px solid var(--success)'
            }}>
              <p style={{ margin: '0 0 0.5rem 0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
                Account Number
              </p>
              <p style={{ margin: '0', fontSize: '1.3rem', fontWeight: '600' }}>
                {response.accountNumber}
              </p>
            </div>

            <div style={{ 
              padding: '1rem', 
              backgroundColor: 'var(--border-light)', 
              borderRadius: '8px',
              borderLeft: '4px solid var(--info)'
            }}>
              <p style={{ margin: '0 0 0.5rem 0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
                Account Holder
              </p>
              <p style={{ margin: '0', fontSize: '1.3rem', fontWeight: '600' }}>
                {response.accountHolderName}
              </p>
            </div>

            <div style={{ 
              padding: '1rem', 
              backgroundColor: 'var(--border-light)', 
              borderRadius: '8px',
              borderLeft: '4px solid var(--primary)'
            }}>
              <p style={{ margin: '0 0 0.5rem 0', color: 'var(--text-light)', fontSize: '0.9rem' }}>
                Initial Balance
              </p>
              <p style={{ margin: '0', fontSize: '1.3rem', fontWeight: '600' }}>
                ₹{response.balance.toLocaleString()}
              </p>
            </div>
          </div>

          <button 
            onClick={() => setResponse(null)} 
            className="btn btn-secondary btn-block"
            style={{ marginTop: '1rem' }}
          >
            Create Another Account
          </button>
        </div>
      )}
    </div>
  );
};

export default CreateAccount;
