import { useEffect, useState } from 'react';
import { createOrder, getProducts } from '../api/client';
import type { Product } from '../types';

export default function CreateOrder() {
  const [products,setProducts]=useState<Product[]>([]);
  const [cart,setCart]=useState<Record<number,number>>({});
  const [loading,setLoading]=useState(true);
  const [submitting,setSubmitting]=useState(false);
  const [error,setError]=useState('');
  const [success,setSuccess]=useState('');

  const load=async()=>{ setLoading(true); setError(''); try{setProducts(await getProducts());}catch{setError('Unable to load the catalogue.');}finally{setLoading(false);} };
  useEffect(()=>{load();},[]);

  const setQuantity=(product:Product, quantity:number)=>{
    if(quantity<0 || quantity>product.stockQuantity) return;
    setCart(prev=>{
      const next={...prev};
      if(quantity===0) delete next[product.id]; else next[product.id]=quantity;
      return next;
    });
  };
  const remove=(id:number)=>setCart(prev=>{const next={...prev};delete next[id];return next;});
  const total=products.reduce((sum,p)=>sum+(cart[p.id]||0)*p.price,0);
  const cartItems=products.filter(p=>cart[p.id]);

  const place=async()=>{
    if(submitting || cartItems.length===0)return;
    setSubmitting(true);setError('');setSuccess('');
    try {
      const order=await createOrder(cartItems.map(p=>({productId:p.id,quantity:cart[p.id]})));
      setSuccess(`Order ${order.orderReference} placed successfully.`);
      setCart({});
      await load();
    } catch(e:any) {
      setError(e?.response?.data?.message || 'Could not place the order.');
    } finally {setSubmitting(false);}
  };
  const money=(n:number)=>new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR',maximumFractionDigits:0}).format(n);

  return (
    <div className="page">
      <div className="page-heading"><div><p className="eyebrow">NEW SALE</p><h1>Create Order</h1></div></div>
      {error&&<div className="alert error">{error}</div>}
      {success&&<div className="alert success">{success}</div>}
      {loading?<div className="loading">Loading catalogue...</div>:(
        <div className="create-layout">
          <section>
            <div className="section-title"><h2>Product catalogue</h2><span>{products.length} items</span></div>
            <div className="product-grid">
              {products.map(p=>{
                const qty=cart[p.id]||0;
                const out=p.stockQuantity===0;
                return <article className={`product-card ${out?'out':''}`} key={p.id}>
                  <div className="product-icon">{p.name.split(' ').map(w=>w[0]).join('').slice(0,2)}</div>
                  <div className="product-info"><h3>{p.name}</h3><p>{p.description}</p></div>
                  <div className="product-bottom"><div><strong>{money(p.price)}</strong><small className={out?'stock-out':'stock'}>{out?'Out of stock':`${p.stockQuantity} in stock`}</small></div>
                  {out?<button className="add-button" disabled>Unavailable</button>:(
                    <div className="quantity">
                      <button disabled={qty===0} onClick={()=>setQuantity(p,qty-1)}>−</button>
                      <span>{qty}</span>
                      <button disabled={qty>=p.stockQuantity} onClick={()=>setQuantity(p,qty+1)}>+</button>
                    </div>
                  )}</div>
                </article>
              })}
            </div>
          </section>
          <aside className="cart">
            <div className="section-title"><h2>Order summary</h2><span>{cartItems.length} {cartItems.length===1?'item':'items'}</span></div>
            {cartItems.length===0?<div className="empty-cart"><div className="empty-icon">0</div><p>Your cart is empty</p><small>Add products to begin an order</small></div>:(
              <div className="cart-items">{cartItems.map(p=><div className="cart-item" key={p.id}><div><strong>{p.name}</strong><small>{cart[p.id]} × {money(p.price)}</small></div><div className="cart-price"><strong>{money(cart[p.id]*p.price)}</strong><button onClick={()=>remove(p.id)} aria-label={`Remove ${p.name}`}>×</button></div></div>)}</div>
            )}
            <div className="cart-total"><span>Grand total</span><strong>{money(total)}</strong></div>
            <button className="place-button" disabled={cartItems.length===0||submitting} onClick={place}>{submitting?'Placing order...':'Place Order'}</button>
          </aside>
        </div>
      )}
    </div>
  );
}
