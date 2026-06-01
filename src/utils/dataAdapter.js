/**
 * 数据适配器 - 后端字段名标准化与业务枚举值统一映射
 *
 * 背景：后端 MyBatis-Plus 返回的字段名可能是 camelCase 或下划线格式，且枚举值与前端约定不一致。
 * 本模块负责：
 * 1. 字段名标准化（下划线 → 驼峰）
 * 2. 常见同义字段映射（如 seller_id → merchant_id）
 * 3. 枚举值映射（后端 published/paid 等 → 前端 on/pending 等）
 * 4. 价格/图片格式化
 * 5. 通用递归键名映射工具
 */

// ======== 后端字段名 → 标准字段名映射表 ========
// 后端某些接口返回的字段名与前端约定不一致，这里做统一映射
export const fieldMap = {
  license_img: 'business_license',    // 营业执照图片
  id_card_img: 'id_card',             // 身份证图片
  seller_level: 'merchant_level',     // 商家等级
  wallet: 'wallet_balance',           // 钱包余额
  seller_id: 'merchant_id',           // 商家ID
  new_degree: 'condition',            // 新旧程度 → 商品状态
  sales_count: 'sales',               // 销量
  avg_rating: 'rating',               // 平均评分
  product_name: 'name',               // 商品名称
  product_image: 'image',             // 商品图片
  unit_price: 'price',               // 单价
  apply_time: 'created_at',           // 申请时间
  score: 'rating',                    // 评分
  reason: 'description',              // 原因 → 描述
}

/**
 * 将原始对象按 fieldMap 做字段名标准化
 * 如果同时存在原名和映射名，保留映射名并删除原名
 * @param {Object} raw - 原始数据对象
 * @returns {Object} 标准化后的对象
 */
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

// ======== 各数据类型转换函数 ========

/**
 * 用户数据转换：下划线 → 驼峰 + 枚举映射
 * @param {Object} raw - 原始用户数据
 * @returns {Object} 标准化用户数据
 */
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

/**
 * 商品数据转换：字段标准化 + 价格格式化 + 图片数组化
 * @param {Object} raw - 原始商品数据
 * @returns {Object} 标准化商品数据
 */
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
    image: raw.image || (raw.images?.[0]) || '',  // 首张图片单独提取
    createdAt: raw.created_at ?? raw.createdAt,
  }
}

/**
 * 订单数据转换：字段映射 + 子项列表转换
 * 注意：user_id → buyer_id（后端用 user_id，前端统一用 buyer_id）
 * @param {Object} raw - 原始订单数据
 * @returns {Object} 标准化订单数据
 */
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
    reviewed: !!raw.reviewed,                // 买家是否已评价
    merchantReviewed: !!(raw.merchant_reviewed ?? raw.merchantReviewed), // 商家是否已评价
    items: (raw.items || []).map(toOrderItem), // 订单子项转换
    refundReason: raw.refund_reason ?? raw.refundReason,
    createdAt: raw.created_at ?? raw.createdAt,
  }
}

/**
 * 订单子项转换
 * @param {Object} raw - 原始订单子项数据
 * @returns {Object} 标准化订单子项
 */
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

/**
 * 购物车项转换
 * @param {Object} raw - 原始购物车项数据
 * @returns {Object} 标准化购物车项
 */
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
    selected: false,  // 购物车项默认未选中
  }
}

/**
 * 评价数据转换
 * @param {Object} raw - 原始评价数据
 * @returns {Object} 标准化评价数据
 */
export function toReview(raw) {
  raw = normalize(raw)
  return {
    id: raw.id,
    orderId: raw.order_id ?? raw.orderId,
    reviewerId: raw.reviewer_id ?? raw.reviewerId,
    targetId: raw.target_id ?? raw.targetId,
    type: raw.type,            // 'product'（商品评价）或 'buyer'（买家评价）
    rating: raw.rating,
    content: raw.content,
    username: raw.username,    // 评价者用户名
    createdAt: raw.created_at ?? raw.createdAt,
  }
}

/**
 * 积分记录转换
 * @param {Object} raw - 原始积分记录数据
 * @returns {Object} 标准化积分记录
 */
export function toPointsRecord(raw) {
  raw = normalize(raw)
  return {
    id: raw.id,
    amount: raw.amount,        // 积分变动数量（正数=获得，负数=消耗）
    description: raw.description,
    createdAt: raw.created_at ?? raw.createdAt,
  }
}

// ======== 枚举值中文映射 ========

/** 订单状态中文名 */
export const ORDER_STATUS = {
  pending: '待发货',
  shipped: '待收货',
  completed: '已完成',
  refund_pending: '退货待审核',
  refunded: '已退货',
  refund_rejected: '退货被拒',
}

/** 订单状态对应的 Element Plus Tag 类型（颜色） */
export const ORDER_STATUS_TAG = {
  pending: '',
  shipped: 'warning',
  completed: 'success',
  refund_pending: 'danger',
  refunded: 'info',
  refund_rejected: '',
}

/** 商品状态中文名 */
export const PRODUCT_STATUS = {
  pending: '待审核',
  on: '在售',
  off: '已下架',
}

/** 用户审核状态中文名 */
export const USER_STATUS = {
  pending: '待审核',
  active: '正常',
  rejected: '已拒绝',
}

/** 用户角色中文名 */
export const USER_ROLE = {
  user: '普通用户',
  merchant: '商家',
  admin: '管理员',
}

// ======== 后端枚举值 → 前端标准枚举值映射 ========

/** 角色映射（后端 seller → 前端 merchant） */
export const roleMap = { seller: 'merchant', user: 'user', admin: 'admin' }

/** 用户状态映射（后端 normal → 前端 active） */
export const userStatusMap = { normal: 'active', banned: 'rejected', pending: 'pending' }

/** 商品状态映射（后端 published → 前端 on） */
export const productStatusMap = { published: 'on', off_shelf: 'off', pending: 'pending' }

/** 订单状态映射（后端枚举值 → 前端标准值） */
export const orderStatusMap = { pending: 'pending', shipped: 'shipped', completed: 'completed', returning: 'refund_pending', returned: 'refunded' }

/** 退货状态映射 */
export const returnStatusMap = { '待审核': 'pending', '同意': 'approved', '拒绝': 'rejected' }

/**
 * 通用枚举值转换函数
 * 将后端枚举值按映射表转换为前端标准值，若未匹配则返回原值
 * @param {Object} map - 枚举值映射表
 * @param {string} value - 原始枚举值
 * @returns {string} 转换后的枚举值
 */
export function mapEnum(map, value) {
  return map[value] ?? value
}

// ======== 价格计算工具函数 ========

/**
 * 计算积分可抵扣金额
 * 规则：100 积分 = 1 元
 * @param {number} points - 可用积分数量
 * @returns {string} 可抵扣金额（保留两位小数）
 */
export function calcPointsDiscount(points) {
  return (points / 100).toFixed(2)
}

/**
 * 计算使用积分后的最终支付金额
 * @param {number} totalAmount - 商品总额
 * @param {boolean} usePoints - 是否使用积分抵扣
 * @param {number} availablePoints - 可用积分数量
 * @returns {string} 最终支付金额（保留两位小数，最低为 0）
 */
export function calcFinalAmount(totalAmount, usePoints, availablePoints) {
  const discount = usePoints ? availablePoints / 100 : 0
  return Math.max(Number(totalAmount) - discount, 0).toFixed(2)
}

// ======== 适配函数（供 autoAdapt 自动调用） ========

/**
 * 用户数据适配：标准化字段 + 枚举值转换
 * @param {Object} user - 原始用户数据
 * @returns {Object} 适配后的用户数据
 */
export function adaptUser(user) {
  const r = normalize({ ...user })
  if ('user_id' in r && !('id' in r)) { r.id = r.user_id; delete r.user_id }
  if (r.role) r.role = mapEnum(roleMap, r.role)        // seller → merchant
  if (r.status) r.status = mapEnum(userStatusMap, r.status) // normal → active
  return r
}

/**
 * 商品数据适配：标准化字段 + 枚举转换 + 价格/图片格式化
 * @param {Object} product - 原始商品数据
 * @returns {Object} 适配后的商品数据
 */
export function adaptProduct(product) {
  const r = { ...product }
  if (r.status) r.status = mapEnum(productStatusMap, r.status)  // published → on
  // MyBatis-Plus 返回 camelCase 字段名，兼容两种格式
  const discount = r.discountPrice ?? r.discount_price
  const original = r.originalPrice ?? r.original_price
  r.price = Number(discount > 0 ? discount : original) || 0  // 优先使用折扣价
  r.condition = r.newDegree ?? r.new_degree ?? r.condition
  r.sales = r.salesCount ?? r.sales_count ?? r.sales
  r.rating = Number(r.avgRating ?? r.avg_rating ?? r.rating) || 0
  // 后端 images 是逗号分隔字符串，转为数组
  if (typeof r.images === 'string') {
    r.images = r.images ? r.images.split(',').map(s => s.trim()).filter(Boolean) : []
  }
  r.image = r.images?.[0] || ''  // 首张图片单独提取
  return r
}

/**
 * 订单数据适配：标准化字段 + 枚举映射 + 子项适配
 * @param {Object} order - 原始订单数据
 * @returns {Object} 适配后的订单数据
 */
export function adaptOrder(order) {
  const r = normalize({ ...order })
  if ('user_id' in r && !('buyer_id' in r)) { r.buyer_id = r.user_id; delete r.user_id }
  if (r.status) r.status = mapEnum(orderStatusMap, r.status)  // paid → pending
  if (r.items) r.items = r.items.map(item => normalize({ ...item }))
  return r
}

/**
 * 退货记录适配
 * @param {Object} record - 原始退货记录数据
 * @returns {Object} 适配后的退货记录
 */
export function adaptReturnRecord(record) {
  const r = normalize({ ...record })
  if (r.status) r.status = mapEnum(returnStatusMap, r.status)
  return r
}

/**
 * 积分记录适配
 * @param {Object} record - 原始积分记录数据
 * @returns {Object} 适配后的积分记录
 */
export function adaptPointRecord(record) {
  return normalize({ ...record })
}

// ======== 通用工具函数 ========

/**
 * 通用递归键名映射
 * 递归遍历对象的所有层级，按 map 替换键名
 * 支持嵌套对象和数组
 * @param {*} obj - 需要映射的对象/数组
 * @param {Object} map - 键名映射表 { oldKey: newKey }
 * @returns {*} 键名替换后的对象/数组
 */
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

// ======== 适配器入口集合 ========

/** 适配器函数映射表 */
const adapters = {
  user: adaptUser,
  product: adaptProduct,
  order: adaptOrder,
  returnRecord: adaptReturnRecord,
  pointRecord: adaptPointRecord,
}

/**
 * 统一适配入口
 * 根据 type 选择合适的适配器函数，自动处理单个对象和数组
 * @param {Object|Array} data - 需要适配的数据（单个对象或数组）
 * @param {string} type - 数据类型：'user' | 'product' | 'order' | 'returnRecord' | 'pointRecord'
 * @returns {Object|Array} 适配后的数据
 */
export function adaptData(data, type) {
  const fn = adapters[type]
  if (!fn) return data
  return Array.isArray(data) ? data.map(fn) : fn(data)
}
