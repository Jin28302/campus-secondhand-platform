/**
 * 认证相关 API 模块 - 用户注册接口
 *
 * 主要接口：
 * - registerUser(data): 普通用户注册，角色固定为"用户"
 * - registerMerchant(data): 商家注册，额外提供营业执照和身份证图片
 *
 * 所有请求均通过 @/utils/request 封装的 axios 实例发送，自动携带 Token
 */
import request from '@/utils/request'

/**
 * 普通用户注册
 * 将前端表单数据提交到后端 /register 接口
 * @param {Object} data - 来自 Register.vue 的表单数据
 * @param {string} data.name - 姓名
 * @param {string} data.phone - 手机号
 * @param {string} data.email - 邮箱
 * @param {string} data.city - 城市
 * @param {number} data.gender - 性别（1=男，0=女）
 * @param {string} data.bankAccount - 银行账号
 * @param {string} data.password - 密码
 * @param {string} data.captcha - 验证码
 * @returns {Promise} 注册结果
 */
export function registerUser(data) {
  return request.post('/register', { ...data, role: '用户' })
}

/**
 * 商家注册
 * 相比普通用户，额外包含营业执照和身份证图片字段
 * @param {Object} data - 来自 Register.vue 的商家注册表单数据
 * @param {string} data.businessLicense - 营业执照图片 URL
 * @param {string} data.idCard - 身份证图片 URL
 * @returns {Promise} 注册结果
 */
export function registerMerchant(data) {
  return request.post('/register', { ...data, role: '商家', licenseImg: data.businessLicense, idCardImg: data.idCard })
}
