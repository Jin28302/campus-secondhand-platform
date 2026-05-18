import request from '@/utils/request'

export function getCaptcha() {
  return request.get('/captcha', { responseType: 'blob' })
}

export function registerUser(data) {
  return request.post('/register', data)
}

export function registerMerchant(data) {
  return request.post('/register/merchant', data)
}
