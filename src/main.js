/**
 * 应用入口文件 - Vue 3 应用初始化
 *
 * 功能：
 * 1. 创建 Vue 应用实例
 * 2. 安装 Pinia（状态管理）
 * 3. 安装 Element Plus（UI 组件库）+ 全局样式
 * 4. 安装 Vue Router（路由）
 * 5. 装载到 index.html 中的 #app 元素
 *
 * 加载顺序：Pinia → ElementPlus → Router → mount
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)
app.use(createPinia())   // 状态管理
app.use(ElementPlus)     // UI 组件库
app.use(router)          // 路由
app.mount('#app')        // 挂载到 DOM
