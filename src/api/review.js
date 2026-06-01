/**
 * 评价相关 API 模块 - 商品评价和买家评价
 *
 * 主要接口：
 * - getProductRatings(productId): 获取商品的评价列表
 * - getBuyerRatings(buyerId): 获取买家的评价列表
 * - createProductRating(data): 创建商品评价（商家评价商品）
 * - createBuyerRating(data): 创建买家评价
 * - getAllRatings({productId, buyerId, page, pageSize}): 合并两类评价，按时间排序，支持前端分页
 *
 * 所有请求均通过 @/utils/request 封装的 axios 实例发送
 */
import request from '@/utils/request'

/**
 * 获取商品评价列表
 * @param {string|number} productId - 商品ID
 * @returns {Promise<Array>} 评价列表，每项额外添加 type='product' 标记
 */
export function getProductRatings(productId) {
  return request.get(`/product/${productId}/ratings`)
    .then(res => (res.list || res).map(r => ({ ...r, type: 'product' })))
}

/**
 * 获取买家评价列表
 * @param {string|number} buyerId - 买家ID
 * @returns {Promise<Array>} 评价列表，每项额外添加 type='buyer' 标记
 */
export function getBuyerRatings(buyerId) {
  return request.get('/rating/buyer', { params: { buyerId } })
    .then(res => (res.list || res).map(r => ({ ...r, type: 'buyer' })))
}

/**
 * 创建商品评价（商家评商品 / 买家评商品）
 * 注意：接口路径为 /rating/seller，后端根据当前用户类型判断是商家还是买家评价
 * @param {Object} data - 评价数据
 * @param {string|number} data.orderId - 订单ID
 * @param {string|number} data.productId - 商品ID
 * @param {number} data.rating - 评分（1-5）
 * @param {string} data.content - 评价内容
 * @returns {Promise} 创建结果
 */
export function createProductRating(data) {
  return request.post('/rating/seller', {
    orderId: data.orderId,
    productId: data.productId,
    score: data.rating,   // 前端用 rating，后端用 score 字段名
    content: data.content,
  })
}

/**
 * 创建买家评价（商家评价买家）
 * @param {Object} data - 评价数据
 * @param {string|number} data.orderId - 订单ID
 * @param {string|number} data.buyerId - 买家ID
 * @param {number} data.rating - 评分（1-5）
 * @param {string} data.content - 评价内容
 * @returns {Promise} 创建结果
 */
export function createBuyerRating(data) {
  return request.post('/rating/buyer', {
    orderId: data.orderId,
    buyerId: data.buyerId,
    score: data.rating,
    content: data.content,
  })
}

/**
 * 合并获取两类评价（商品评价 + 买家评价）
 * 并行请求商品评价和买家评价 → 合并数组 → 按 created_at 降序排列 → 前端内存分页
 *
 * @param {Object} options - 查询选项
 * @param {string|number} [options.productId] - 商品ID（如果有则获取商品评价）
 * @param {string|number} [options.buyerId] - 买家ID（如果有则获取买家评价）
 * @param {number} [options.page=1] - 当前页码
 * @param {number} [options.pageSize=10] - 每页数量
 * @returns {Promise<{list: Array, total: number}>} 分页后的评价列表和总数
 */
export async function getAllRatings({ productId, buyerId, page = 1, pageSize = 10 } = {}) {
  const tasks = []
  if (productId) tasks.push(getProductRatings(productId))
  if (buyerId) tasks.push(getBuyerRatings(buyerId))
  const results = await Promise.all(tasks)
  const all = results.flat().sort((a, b) => new Date(b.createdAt || b.created_at) - new Date(a.createdAt || a.created_at))
  return {
    list: all.slice((page - 1) * pageSize, page * pageSize),
    total: all.length,
  }
}
