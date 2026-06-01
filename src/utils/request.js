/**
 * Axios 请求封装 - 统一 HTTP 客户端
 *
 * 功能概述：
 * 1. 创建 axios 实例，统一 baseURL='/api'，超时 10 秒
 * 2. 请求拦截器：自动从 localStorage 读取 token 并注入 Authorization 头（Bearer）
 * 3. 响应拦截器：自动处理业务状态码（code===200 返回 data），自动调用 dataAdapter 做字段适配
 * 4. 401/403 响应：自动清除登录状态并跳转登录页
 * 5. 内置 autoAdapt：递归遍历响应数据，按特征字段自动检测数据类型（product/user/order/returnRecord/pointRecord）并调用对应适配器
 *
 * 用法：所有 API 调用都应 import request from '@/utils/request' 而非直接使用 axios
 */
import axios from 'axios'
import { adaptData } from './dataAdapter'

/**
 * 创建 axios 实例
 * baseURL: '/api' - 所有接口请求统一前缀
 * timeout: 10000ms - 请求超时时间
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

/**
 * 请求拦截器
 * 在每个请求发出前，从 localStorage 读取 token 并注入 Authorization 请求头
 * 格式：Bearer {token}
 */
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

/**
 * 判断对象是否包含指定字段之一
 * @param {Object} obj - 目标对象
 * @param {...string} names - 字段名列表
 * @returns {boolean} 是否包含至少一个字段
 */
function hasField(obj, ...names) {
  return names.some(n => n in obj)
}

/**
 * 根据特征字段自动检测数据类型
 * 用于在不明确后端返回数据类型时自动选择合适的适配器
 *
 * 检测规则（按优先级）：
 *   - product: 含 avgRating/avg_rating, newDegree/new_degree, salesCount/sales_count 等商品特征字段
 *   - user:    含 sellerLevel/seller_level, licenseImg/license_img, bankAccount/bank_account 等用户特征字段
 *   - order:   含 unitPrice/unit_price, productName/product_name, orderNo/order_no 等订单特征字段
 *   - returnRecord: 含 applyTime/apply_time
 *   - pointRecord:  含 score + amount
 *
 * @param {Object} obj - 要检测的原始数据对象
 * @returns {string|null} 数据类型名称或 null（无法识别）
 */
function detectType(obj) {
  if (!obj || typeof obj !== 'object') return null
  const sample = Array.isArray(obj) ? obj[0] : obj
  if (!sample) return null
  if (hasField(sample, 'avgRating', 'avg_rating', 'newDegree', 'new_degree', 'salesCount', 'sales_count')) return 'product'
  if (hasField(sample, 'sellerLevel', 'seller_level', 'licenseImg', 'license_img', 'bankAccount', 'bank_account')) return 'user'
  if (hasField(sample, 'unitPrice', 'unit_price', 'productName', 'product_name', 'orderNo', 'order_no')) return 'order'
  if (hasField(sample, 'applyTime', 'apply_time')) return 'returnRecord'
  if (hasField(sample, 'score') && hasField(sample, 'amount')) return 'pointRecord'
  return null
}

/**
 * 递归自动适配响应数据
 * 对顶层数组、嵌套对象中的数组/对象逐个检测数据类型并调用 adaptData 做字段标准化
 *
 * 处理逻辑：
 * 1. 顶层数组：检测元素类型后批量适配
 * 2. 嵌套对象：遍历属性，对数组值检测元素类型后适配，对子对象检测类型后适配
 * 3. 顶层对象本身也检测类型
 *
 * @param {*} data - 后端返回的原始数据
 * @returns {*} 适配后的数据
 */
function autoAdapt(data) {
  if (!data || typeof data !== 'object') return data

  // 顶层数组：对每个元素做适配
  if (Array.isArray(data)) {
    if (data.length > 0) {
      const type = detectType(data[0])
      if (type) return adaptData(data, type)
    }
    return data
  }

  const result = { ...data }
  for (const [key, val] of Object.entries(result)) {
    if (Array.isArray(val) && val.length > 0) {
      const type = detectType(val[0])
      if (type) result[key] = adaptData(val, type)
    } else if (val && typeof val === 'object' && !Array.isArray(val)) {
      const type = detectType(val)
      if (type) result[key] = adaptData(val, type)
    }
  }
  const topType = detectType(data)
  if (topType) return adaptData(result, topType)
  return result
}

/**
 * 响应拦截器
 * 统一处理响应数据：
 * 1. 解包标准 { code, data, msg } 格式：code===200 返回 data，否则 reject
 * 2. 自动调用 autoAdapt 做字段名标准化和枚举值映射
 * 3. 401/403 状态码：清除登录信息并跳转登录页
 */
request.interceptors.response.use(
  (response) => {
    const body = response.data
    // 标准 { code, data, msg } 格式
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) {
        return autoAdapt(body.data)
      }
      // 业务错误：将整个 body 作为错误返回，调用方可读取 msg
      return Promise.reject(body)
    }
    // 非标准格式直接适配
    return autoAdapt(body)
  },
  (error) => {
    const status = error.response?.status
    // 401 Unauthorized / 403 Forbidden 表示 Token 过期或无权限
    // 清除 localStorage 中的登录状态并跳转登录页
    if (status === 401 || status === 403) {
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      window.location.href = '/login'
    }
    return Promise.reject(error.response?.data || error)
  }
)

export default request
