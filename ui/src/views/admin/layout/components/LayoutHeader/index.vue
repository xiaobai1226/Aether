<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAccountStore } from '@/stores/account'
import Avatar from '@/components/Avatar.vue'
import UpdateAvatar from '@/views/layout/components/LayoutHeader/UpdateAvatar.vue'
import UpdatePassword from '@/views/layout/components/LayoutHeader/UpdatePassword.vue'
import { logout } from '@/api/v1/account'
import Confirm from '@/utils/Confirm'

const router = useRouter()

// 从pinia获取用户数据
const accountStore = useAccountStore()

/**
 * 退出登录方法
 */
const handleLogout = () => {
  Confirm('确定要退出吗', () => {
    logout().then(() => {
      // 清除用户数据
      accountStore.clearAccountInfo()

      // 跳转登录页面
      router.push('/login')
    }).catch(() => {
    })
  })
}

// 修改头像
const updateAvatarRef = ref()
const updateAvatar = () => {
  // 显示修改头像弹窗
  updateAvatarRef.value.show(accountStore.accountInfo.nickname)
}

const avatarRef = ref()

/**
 * 重载头像
 */
const reloadAvatar = () => {
  avatarRef.value.updateAvatarImage()
}

// 修改密码
const updatePasswordRef = ref()
const updatePassword = () => {
  // 显示修改密码弹窗
  updatePasswordRef.value.show()
}

const toMain = () => {
  router.push('/')
}
</script>

<template>
  <div class="header">
    <div class="logo">
      <span class="iconfont icon-NAS"></span>
      <div class="name">Aether 管理</div>
    </div>
    <div class="right-panel">
      <span class="iconfont icon-main" @click="toMain"></span>

      <el-dropdown>
        <div class="user-info">
          <!-- 头像 -->
          <div class="avatar">
            <Avatar ref="avatarRef" :width="46" />
          </div>
          <span class="nick-name">{{ accountStore.accountInfo.nickname }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="updateAvatar">修改头像</el-dropdown-item>
            <el-dropdown-item @click="updatePassword">修改密码</el-dropdown-item>
            <el-dropdown-item @click="handleLogout">退出</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
  <UpdateAvatar ref="updateAvatarRef" @updateAvatar="reloadAvatar" />
  <UpdatePassword ref="updatePasswordRef" />
</template>

<style scoped lang="scss">
.header {
  box-shadow: 0 3px 10px 0 rgb(0 0 0 / 6%);
  height: 56px;
  padding-left: 24px;
  padding-right: 24px;
  position: relative;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .logo {
    display: flex;
    align-items: center;

    .icon-NAS {
      font-size: 40px;
      color: #1296db;
    }

    .name {
      font-weight: bold;
      margin-left: 5px;
      font-size: 20px;
      color: #05a1f5;
    }
  }

  .right-panel {
    display: flex;
    align-items: center;

    .icon-main {
      font-size: 18px;
      font-weight: bold;
      cursor: pointer;
    }

    .user-info {
      margin-left: 10px;
      display: flex;
      align-items: center;
      cursor: pointer;

      .avatar {
        margin: 0 5px 0 15px;
      }

      .nick-name {
        color: #05a1f5;
      }
    }
  }
}
</style>