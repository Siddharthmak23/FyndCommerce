import axios from 'axios';
import type { Product, Order } from '../types';

const api = axios.create({ baseURL: 'http://localhost:8080/api' });

export const getProducts = () => api.get<Product[]>('/products').then(r => r.data);
export const createOrder = (items: { productId: number; quantity: number }[]) =>
  api.post<Order>('/orders', { items }).then(r => r.data);
export const getOrders = (reference?: string, status?: string) =>
  api.get<Order[]>('/orders', { params: { reference: reference || undefined, status: status || 'ALL' } }).then(r => r.data);
export const getOrder = (id: number) => api.get<Order>(`/orders/${id}`).then(r => r.data);
export const updateOrderStatus = (id: number, status: string) =>
  api.patch<Order>(`/orders/${id}/status`, { status }).then(r => r.data);
