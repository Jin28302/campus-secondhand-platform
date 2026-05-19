import axios from 'axios'
import { adaptData } from './dataAdapter'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

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

function detectType(obj) {
  if (!obj || typeof obj !== 'object') return null
  const sample = Array.isArray(obj) ? obj[0] : obj
  if (!sample) return null
  if ('avg_rating' in sample || 'new_degree' in sample || 'sales_count' in sample) return 'product'
  if ('wallet' in sample || 'seller_level' in sample || 'license_img' in sample) return 'user'
  if ('unit_price' in sample || 'product_name' in sample) return 'order'
  if ('apply_time' in sample) return 'returnRecord'
  if ('score' in sample && 'amount' in sample) return 'pointRecord'
  return null
}

function autoAdapt(data) {
  if (!data || typeof data !== 'object') return data
  const result = { ...data }
  for (const [key, val] of Object.entries(result)) {
    if (Array.isArray(val) && val.length > 0) {
      const type = detectType(val[0])
      if (type) result[key] = adaptData(val, type)
    } else if (val && typeof val === 'object') {
      const type = detectType(val)
      if (type) result[key] = adaptData(val, type)
    }
  }
  const topType = detectType(data)
  if (topType) return adaptData(result, topType)
  return result
}

request.interceptors.response.use(
  (response) => autoAdapt(response.data),
  (error) => Promise.reject(error)
)

export default request
