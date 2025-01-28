import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/login/index.vue'
import Layout from '@/views/layout/index.vue'
import Netdisk from '@/views/netdisk/index.vue'
import FileList from '@/views/netdisk/components/FileList/index.vue'
import RecycleBin from '@/views/netdisk/components/RecycleBin/index.vue'
import AdminLayout from '@/views/admin/layout/index.vue'
import UserList from '@/views/admin/components/UserList/index.vue'
import AdminFileList from '@/views/admin/components/FileList/index.vue'

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
    },
    // 管理页
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        {
          path: 'userlist',
          name: '用户列表',
          component: UserList
        },
        {
          path: 'filelist',
          name: '文件列表',
          component: AdminFileList
        }
      ]
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
