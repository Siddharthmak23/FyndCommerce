export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Order {
  id: number;
  orderReference: string;
  createdAt: string;
  updatedAt: string;
  totalAmount: number;
  status: 'PLACED' | 'PACKED' | 'SHIPPED' | 'CANCELLED';
  items: OrderItem[];
}
