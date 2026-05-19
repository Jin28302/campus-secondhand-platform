import request from '@/utils/request'

export function getProductRatings(productId) {
  return request.get('/product_rating', { params: { product_id: productId } })
    .then(res => (res.list || res).map(r => ({ ...r, type: 'product' })))
}

export function getBuyerRatings(buyerId) {
  return request.get('/buyer_rating', { params: { buyer_id: buyerId } })
    .then(res => (res.list || res).map(r => ({ ...r, type: 'buyer' })))
}

export function createProductRating(data) {
  return request.post('/product_rating', {
    order_id: data.orderId,
    product_id: data.productId,
    score: data.rating,
    content: data.content,
  })
}

export function createBuyerRating(data) {
  return request.post('/buyer_rating', {
    order_id: data.orderId,
    buyer_id: data.buyerId,
    score: data.rating,
    content: data.content,
  })
}

// 合并两类评价，按 created_at 降序排列，支持前端分页
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
