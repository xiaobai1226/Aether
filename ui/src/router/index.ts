import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/login/index.vue'
import Layout from '@/views/layout/index.vue'
import Netdisk from '@/views/netdisk/index.vue'
import FileList from '@/views/netdisk/components/FileList/index.vue'
import RecycleBin from '@/views/netdisk/components/RecycleBin/index.vue'
import DirectLinkList from '@/views/netdisk/components/DirectLinkList/index.vue'
import AdminLayout from '@/views/admin/layout/index.vue'
import UserList from '@/views/admin/components/UserList/index.vue'
import AdminFileList from '@/views/admin/components/FileList/index.vue'
import Dashboard from '@/views/admin/components/Dashboard/index.vue'
import Settings from '@/views/settings/index.vue'
import StorageSourceManagement from '@/views/settings/components/StorageSourceManagement/index.vue'

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
            },
            {
              path: 'directlink',
              name: '直链记录',
              component: DirectLinkList
            }
            // {
            //     path: "share",
            //     name: "分享记录",
            //     component: ShareList,
            // }
          ]
        },
        {
          path: 'settings',
          name: '设置',
          component: Settings,
          redirect: '/settings/storage',
          children: [
            {
              path: 'storage',
              name: '存储源管理',
              component: StorageSourceManagement
            }
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
      redirect: '/admin/dashboard',
      component: AdminLayout,
      children: [
        {
          path: 'dashboard',
          name: '仪表板',
          component: Dashboard
        },
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

// 路由守卫：检查用户是否有存储源
router.beforeEach((to, from, next) => {
  // 动态导入，避免循环依赖
  import('@/stores/account').then(({ useAccountStore }) => {
    const accountStore = useAccountStore()
    
    // 如果已登录且不是登录页面
    if (accountStore.accountInfo.token && to.path !== '/login') {
      // 如果没有存储源且不是前往设置页面
      if (accountStore.accountInfo.hasStorageSource === false && !to.path.startsWith('/settings')) {
        // 强制跳转到存储源管理页面
        next('/settings/storage')
        return
      }
    }
    
    next()
  })
})

export default router
