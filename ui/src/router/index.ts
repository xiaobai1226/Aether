import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/login/index.vue'
import Layout from '@/views/layout/index.vue'
import Netdisk from '@/views/netdisk/index.vue'
import FileList from '@/views/netdisk/components/FileList/index.vue'
import RecycleBin from '@/views/netdisk/components/RecycleBin/index.vue'
import ShareList from '@/views/netdisk/components/ShareList/index.vue'
import ShareCheck from '@/views/share/ShareCheck/index.vue'
import ShareContent from '@/views/share/ShareContent/index.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: Layout,
      redirect: '/netdisk/main',
      children: [
        {
          path: 'netdisk',
          name: '网盘',
          // meta: {
          //     needLogin: true,
          //     menuCode: "main"
          // },
          component: Netdisk,
          children: [
            {
              path: 'main',
              name: '首页',
              // meta: {
              //     needLogin: true,
              //     menuCode: "main"
              // },
              component: FileList
            },
            {
              path: 'recyclebin',
              name: '回收站',
              component: RecycleBin
            }
            // {
            //     path: "share",
            //     name: "分享记录",
            //     component: ShareList,
            // }
          ]
        }
      ]
    },
    // 登录页
    {
      path: '/login',
      component: Login
    }
    // {
    //     path: "/shareCheck/:shareId",
    //     name: "分享校验",
    //     component: ShareCheck
    // },
    // {
    //     path: "/share/:shareId",
    //     name: "分享",
    //     component: ShareContent
    // }
  ]
})

export default router
