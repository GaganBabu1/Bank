import { Link, useLocation } from 'react-router-dom';

const Layout = ({ children }) => {
  const location = useLocation();

  const isActive = (path) => location.pathname === path;

  return (
    <div className="app-container">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <h1>💳 Bank</h1>
          <p>Modern Banking</p>
        </div>
        
        <nav>
          <ul className="sidebar-menu">
            <li>
              <Link 
                to="/" 
                className={isActive('/') ? 'active' : ''}
              >
                <span>📊</span>
                Dashboard
              </Link>
            </li>
            <li>
              <Link 
                to="/create-account" 
                className={isActive('/create-account') ? 'active' : ''}
              >
                <span>➕</span>
                Create Account
              </Link>
            </li>
            <li>
              <Link 
                to="/deposit" 
                className={isActive('/deposit') ? 'active' : ''}
              >
                <span>💰</span>
                Deposit
              </Link>
            </li>
            <li>
              <Link 
                to="/withdraw" 
                className={isActive('/withdraw') ? 'active' : ''}
              >
                <span>🏧</span>
                Withdraw
              </Link>
            </li>
            <li>
              <Link 
                to="/transfer" 
                className={isActive('/transfer') ? 'active' : ''}
              >
                <span>↔️</span>
                Transfer
              </Link>
            </li>
            <li>
              <Link 
                to="/history" 
                className={isActive('/history') ? 'active' : ''}
              >
                <span>📜</span>
                History
              </Link>
            </li>
          </ul>
        </nav>
      </aside>

      <main className="main-content">
        <header className="header">
          <h2 className="header-title">Banking System</h2>
          <div className="header-info">
            <span>🕐 {new Date().toLocaleTimeString()}</span>
          </div>
        </header>

        <div className="page-content">
          {children}
        </div>
      </main>
    </div>
  );
};

export default Layout;
