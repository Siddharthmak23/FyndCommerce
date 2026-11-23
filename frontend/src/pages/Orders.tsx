import { useEffect, useState } from 'react';
import { getOrders, getOrder, updateOrderStatus } from '../api/client';
import type { Order } from '../types';

export default function Orders(){
  const [orders,setOrders]=useState<Order[]>([]);
  const [reference,setReference]=useState('');
  const [status,setStatus]=useState('ALL');
  const [loading,setLoading]=useState(true);
  const [error,setError]=useState('');
  const [selected,setSelected]=useState<Order|null>(null);
  const [acting,setActing]=useState(false);

  const load=async()=>{
    setLoading(true);setError('');
    try{setOrders(await getOrders(reference,status));}catch{setError('Unable to load orders.');}finally{setLoading(false);}
  };
  useEffect(()=>{load();},[status]);

  const open=async(id:number)=>{
    try{setSelected(await getOrder(id));}catch{setError('Unable to load order details.');}
  };
  const act=async(next:string)=>{
    if(!selected||acting)return;
    if(next==='CANCELLED'&&!window.confirm('Cancel this order and restore all item stock?'))return;
    setActing(true);setError('');
    try{
      const updated=await updateOrderStatus(selected.id,next);
      setSelected(updated);
      await load();
    }catch(e:any){setError(e?.response?.data?.message||'Unable to update the order.');}finally{setActing(false);}
  };
  const money=(n:number)=>new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR',maximumFractionDigits:0}).format(n);
  const date=(v:string)=>new Intl.DateTimeFormat('en-IN',{day:'2-digit',month:'short',year:'numeric',hour:'2-digit',minute:'2-digit'}).format(new Date(v));

  return (
    <div className="page">
      <div className="page-heading"><div><p className="eyebrow">FULFILMENT</p><h1>Orders</h1></div></div>
      {error&&<div className="alert error">{error}</div>}
      <div className="filters">
        <form onSubmit={e=>{e.preventDefault();load();}} className="search-form">
          <input value={reference} onChange={e=>setReference(e.target.value)} placeholder="Search by order reference" />
          <button type="submit">Search</button>
        </form>
        <select value={status} onChange={e=>setStatus(e.target.value)}>
          {['ALL','PLACED','PACKED','SHIPPED','CANCELLED'].map(s=><option key={s} value={s}>{s==='ALL'?'All statuses':s.charAt(0)+s.slice(1).toLowerCase()}</option>)}
        </select>
      </div>
      {loading?<div className="loading">Loading orders...</div>:orders.length===0?(
        <div className="empty-orders"><h3>No orders found</h3><p>Try changing the search or status filter.</p></div>
      ):(
        <div className="table-wrap"><table>
          <thead><tr><th>Order reference</th><th>Creation date</th><th>Total amount</th><th>Status</th><th></th></tr></thead>
          <tbody>{orders.map(o=><tr key={o.id}>
            <td><strong>{o.orderReference}</strong></td><td>{date(o.createdAt)}</td><td>{money(o.totalAmount)}</td>
            <td><span className={`badge ${o.status.toLowerCase()}`}>{o.status}</span></td>
            <td><button className="details-button" onClick={()=>open(o.id)}>View Details</button></td>
          </tr>)}</tbody>
        </table></div>
      )}
      {selected&&<div className="modal-backdrop" onMouseDown={e=>{if(e.target===e.currentTarget)setSelected(null);}}>
        <div className="modal">
          <div className="modal-header"><div><p className="eyebrow">ORDER DETAILS</p><h2>{selected.orderReference}</h2></div><button onClick={()=>setSelected(null)}>×</button></div>
          <div className="modal-meta"><span className={`badge ${selected.status.toLowerCase()}`}>{selected.status}</span><small>{date(selected.createdAt)}</small></div>
          <div className="detail-list">{selected.items.map(i=><div className="detail-row" key={i.id}><div><strong>{i.productName}</strong><small>{i.quantity} × {money(i.unitPrice)}</small></div><strong>{money(i.subtotal)}</strong></div>)}</div>
          <div className="detail-total"><span>Grand total</span><strong>{money(selected.totalAmount)}</strong></div>
          {selected.status==='PLACED'&&<div className="actions"><button className="primary-action" disabled={acting} onClick={()=>act('PACKED')}>Mark as Packed</button><button className="cancel-action" disabled={acting} onClick={()=>act('CANCELLED')}>Cancel Order</button></div>}
          {selected.status==='PACKED'&&<div className="actions"><button className="primary-action" disabled={acting} onClick={()=>act('SHIPPED')}>Mark as Shipped</button><button className="cancel-action" disabled={acting} onClick={()=>act('CANCELLED')}>Cancel Order</button></div>}
        </div>
      </div>}
    </div>
  );
}
