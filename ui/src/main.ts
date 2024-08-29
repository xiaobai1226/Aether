import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
// 引入初始化样式文件
import '@/styles/base.scss'
// 引入代码高亮
import hljsVuePlugin from '@highlightjs/vue-plugin'
import 'highlight.js/styles/atom-one-light.css'
// 批量引入常用语言库
import 'highlight.js/lib/common'


import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(hljsVuePlugin)
app.use(pinia)
app.use(router)

app.mount('#app')
