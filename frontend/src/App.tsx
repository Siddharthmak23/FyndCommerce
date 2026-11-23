import { useState } from 'react';
import CreateOrder from './pages/CreateOrder';
import Orders from './pages/Orders';

export default function App() {
  const [page, setPage] = useState<'create'|'orders'>('create');
  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand"><span className="brand-mark">F</span><span>Fynd Commerce</span></div>
        <nav>
          <button className={page==='create'?'nav-active':''} onClick={()=>setPage('create')}>Create Order</button>
          <button className={page==='orders'?'nav-active':''} onClick={()=>setPage('orders')}>Orders</button>
        </nav>
      </header>
      <main>{page==='create' ? <CreateOrder/> : <Orders/>}</main>
    </div>
  );
}
