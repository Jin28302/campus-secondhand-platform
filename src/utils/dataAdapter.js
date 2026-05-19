// 后端字段名 → 标准字段名映射表
export const fieldMap = {
  license_img: 'business_license',
  id_card_img: 'id_card',
  seller_level: 'merchant_level',
  wallet: 'wallet_balance',
  seller_id: 'merchant_id',
  new_degree: 'condition',
  sales_count: 'sales',
  avg_rating: 'rating',
  product_name: 'name',
  product_image: 'image',
  unit_price: 'price',
  apply_time: 'created_at',
  score: 'rating',
  reason: 'description',
}

// 将原始对象按 fieldMap 做字段名标准化
function normalize(raw) {
  const result = { ...raw }
  for (const [from, to] of Object.entries(fieldMap)) {
    if (from in result) {
      if (!(to in result)) result[to] = result[from]
      delete result[from]
    }
  }
  return result
}

// 字段名映射：下划线 → 驼峰
export function toUser(raw) {
  raw = normalize(raw)
  return {
    id: raw.id,
    name: raw.name,
    phone: raw.phone,
    email: raw.email,
    city: raw.city,
    gender: raw.gender,
    bankAccount: raw.bank_account ?? raw.bankAccount,
    role: raw.role,
    status: raw.status,
    walletBalance: raw.wallet_balance ?? raw.walletBalance,
    points: raw.points,
    businessLicense: raw.business_license ?? raw.businessLicense,
    idCard: raw.id_card ?? raw.idCard,
    merchantLevel: raw.merchant_level ?? raw.merchantLevel,
    createdAt: raw.created_at ?? raw.createdAt,
  }
}

export function toProduct(raw) {
  raw = normalize(raw)
  return {
    id: raw.id,
    merchantId: raw.merchant_id ?? raw.merchantId,
    merchantName: raw.merchant_name ?? raw.merchantName,
    name: raw.name,
    category: raw.category,
    description: raw.description,
    price: Number(raw.price),
    stock: raw.stock,
    condition: raw.condition,
    status: raw.status,
    sales: raw.sales,
    rating: Number(raw.rating),
    images: raw.images || [],
    image: raw.image || (raw.images?.[0]) || '',
    createdAt: raw.created_at ?? raw.createdAt,
  }
}

export function toOrder(raw) {
  raw = normalize(raw)
  // 订单中 user_id 映射为 buyer_id
  if ('user_id' in raw && !('buyer_id' in raw)) { raw.buyer_id = raw.user_id; delete raw.user_id }
  return {
    id: raw.id,
    orderNo: raw.order_no ?? raw.orderNo,
    buyerId: raw.buyer_id ?? raw.buyerId,
    merchantId: raw.merchant_id ?? raw.merchantId,
    merchantName: raw.merchant_name ?? raw.merchantName,
    buyerName: raw.buyer_name ?? raw.buyerName,
    totalAmount: Number(raw.total_amount ?? raw.totalAmount),
    pointsUsed: raw.points_used ?? raw.pointsUsed ?? 0,
    status: raw.status,
    reviewed: !!raw.reviewed,
    merchantReviewed: !!(raw.merchant_reviewed ?? raw.merchantReviewed),
    items: (raw.items || []).map(toOrderItem),
    refundReason: raw.refund_reason ?? raw.refundReason,
    createdAt: raw.created_at ?? raw.createdAt,
  }
}

export function toOrderItem(raw) {
  raw = normalize(raw)
  return {
    id: raw.id,
    orderId: raw.order_id ?? raw.orderId,
    productId: raw.product_id ?? raw.productId,
    name: raw.name,
    price: Number(raw.price),
    quantity: raw.quantity,
    image: raw.image,
  }
}

export function toCartItem(raw) {
  raw = normalize(raw)
  return {
    id: raw.id,
    productId: raw.product_id ?? raw.productId,
    merchantId: raw.merchant_id ?? raw.merchantId,
    merchantName: raw.merchant_name ?? raw.merchantName,
    name: raw.name,
    price: Number(raw.price),
    stock: raw.stock,
    image: raw.image,
    quantity: raw.quantity,
    selected: false,
  }
}

export function toReview(raw) {
  raw = normalize(raw)
  return {
    id: raw.id,
    orderId: raw.order_id ?? raw.orderId,
    reviewerId: raw.reviewer_id ?? raw.reviewerId,
    targetId: raw.target_id ?? raw.targetId,
    type: raw.type,
    rating: raw.rating,
    content: raw.content,
    username: raw.username,
    createdAt: raw.created_at ?? raw.createdAt,
  }
}

export function toPointsRecord(raw) {
  raw = normalize(raw)
  return {
    id: raw.id,
    amount: raw.amount,
    description: raw.description,
    createdAt: raw.created_at ?? raw.createdAt,
  }
}

// 枚举值中文映射
export const ORDER_STATUS = {
  pending: '待收货',
  completed: '已完成',
  refund_pending: '退货待审核',
  refunded: '已退货',
  refund_rejected: '退货被拒',
}

export const ORDER_STATUS_TAG = {
  pending: 'warning',
  completed: 'success',
  refund_pending: 'danger',
  refunded: 'info',
  refund_rejected: '',
}

export const PRODUCT_STATUS = {
  pending: '待审核',
  on: '在售',
  off: '已下架',
}

export const USER_STATUS = {
  pending: '待审核',
  active: '正常',
  rejected: '已拒绝',
}

export const USER_ROLE = {
  user: '普通用户',
  merchant: '商家',
  admin: '管理员',
}

// 后端枚举值 → 前端标准枚举值映射
export const roleMap = { seller: 'merchant', user: 'user', admin: 'admin' }

export const userStatusMap = { normal: 'active', banned: 'rejected', pending: 'pending' }

export const productStatusMap = { published: 'on', off_shelf: 'off', pending: 'pending' }

export const orderStatusMap = { paid: 'pending', completed: 'completed', returning: 'refund_pending', returned: 'refunded' }

export const returnStatusMap = { '待审核': 'pending', '同意': 'approved', '拒绝': 'rejected' }

// 通用枚举转换函数
export function mapEnum(map, value) {
  return map[value] ?? value
}

// 价格计算
export function calcPointsDiscount(points) {
  return (points / 100).toFixed(2)
}

export function calcFinalAmount(totalAmount, usePoints, availablePoints) {
  const discount = usePoints ? availablePoints / 100 : 0
  return Math.max(Number(totalAmount) - discount, 0).toFixed(2)
}

// 适配函数
export function adaptUser(user) {
  const r = normalize({ ...user })
  if ('user_id' in r && !('id' in r)) { r.id = r.user_id; delete r.user_id }
  if (r.role) r.role = mapEnum(roleMap, r.role)
  if (r.status) r.status = mapEnum(userStatusMap, r.status)
  return r
}

export function adaptProduct(product) {
  const r = normalize({ ...product })
  if (r.status) r.status = mapEnum(productStatusMap, r.status)
  if (r.discount_price && r.discount_price > 0) {
    r.price = r.discount_price
  } else {
    r.price = r.original_price
  }
  delete r.discount_price
  delete r.original_price
  return r
}

export function adaptOrder(order) {
  const r = normalize({ ...order })
  if ('user_id' in r && !('buyer_id' in r)) { r.buyer_id = r.user_id; delete r.user_id }
  if (r.status) r.status = mapEnum(orderStatusMap, r.status)
  if (r.items) r.items = r.items.map(item => normalize({ ...item }))
  return r
}

export function adaptReturnRecord(record) {
  const r = normalize({ ...record })
  if (r.status) r.status = mapEnum(returnStatusMap, r.status)
  return r
}

export function adaptPointRecord(record) {
  return normalize({ ...record })
}

// 通用递归键名映射
export function mapKeys(obj, map) {
  if (Array.isArray(obj)) return obj.map(item => mapKeys(item, map))
  if (obj === null || typeof obj !== 'object') return obj
  const result = {}
  for (const [key, val] of Object.entries(obj)) {
    const newKey = map[key] ?? key
    result[newKey] = mapKeys(val, map)
  }
  return result
}

// 适配器入口
const adapters = {
  user: adaptUser,
  product: adaptProduct,
  order: adaptOrder,
  returnRecord: adaptReturnRecord,
  pointRecord: adaptPointRecord,
}

export function adaptData(data, type) {
  const fn = adapters[type]
  if (!fn) return data
  return Array.isArray(data) ? data.map(fn) : fn(data)
}
