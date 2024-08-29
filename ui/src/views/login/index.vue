<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { LoginInfo } from '@/api/account/types'
import { getImageCaptcha } from '@/api/captcha'
import { useRouter } from 'vue-router'
import { useAccountStore } from '@/stores/account'
import { ResultErrorMsgEnum } from '@/enums/ResultErrorMsgEnum'
import Verify from '@/utils/Verify'
import { VerifyEnum } from '@/enums/VerifyEnum'
import { ElLoading } from 'element-plus'

const router = useRouter()

const accountStore = useAccountStore()

// 表单对象
const loginForm = ref<LoginInfo>({
  // 用户名
  username: '',
  // 密码
  password: '',
  // 图形验证码ID
  captchaId: '',
  // 验证码
  captchaCode: ''
  // 记住我
  // rememberMe: false
})

// 获取表单实例
const loginFormRef = ref()

// 规则对象
const rules = {
  username: [
    { required: true, message: ResultErrorMsgEnum.ERROR_USERNAME_EMPTY, trigger: 'blur' },
    { validator: Verify.username, message: ResultErrorMsgEnum.ERROR_USERNAME_FORMAT, trigger: 'blur' }
  ],
  password: [
    { required: true, message: ResultErrorMsgEnum.ERROR_PASSWARD_EMPTY, trigger: 'blur' },
    { validator: Verify.password, message: ResultErrorMsgEnum.ERROR_PASSWARD_FORMAT, trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, message: ResultErrorMsgEnum.ERROR_IMAGE_CAPTCHA_EMPTY, trigger: 'blur' },
    { len: VerifyEnum.CAPTCHA_CODE_LENGTH, message: ResultErrorMsgEnum.ERROR_IMAGE_CAPTCHA_FORMAT, trigger: 'blur' }
  ]
}

/**
 * 验证码图片Base64字符串
 */
const captchaBase64 = ref()

// 登录方法
const handleLogin = () => {
  loginFormRef.value.validate((valid: boolean) => {
    // 所有表单都通过校验才为true
    if (valid) {
      const loadingInstance = ElLoading.service({ fullscreen: true })
      accountStore.accountLogin(loginForm.value)
        .then(() => {
          loadingInstance.close()
          // 跳转首页
          router.replace({ path: '/' })
        })
        .catch(() => {
          // 验证失败，重新生成验证码
          getCaptcha()
          loadingInstance.close()
        })
    }
  })
}

// 获取图形验证码
const getCaptcha = () => {
  getImageCaptcha().then(({ data }) => {
    const { captchaId, captchaBase64Data } = data
    loginForm.value.captchaId = captchaId
    captchaBase64.value = captchaBase64Data
  })
}

onMounted(() => {
  getCaptcha()
})
</script>

<template>
  <div class="login-body">
    <div class="bg"></div>
    <div class="login-panel">
      <!-- TODO rules警告待解决，代码能生效 -->
      <el-form class="login-register" :model="loginForm" :rules="rules" ref="loginFormRef" @submit.prevent status-icon
               @keyup.enter="handleLogin">
        <div class="login-title">Netdisk</div>
        <!-- input输入 -->
        <el-form-item prop="username">
          <el-input size="large" clearable placeholder="请输入用户名" v-model.trim="loginForm.username">
            <template #prefix>
              <span class="iconfont icon-account"></span>
            </template>
          </el-input>
        </el-form-item>
        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input type="password" size="large" placeholder="请输入密码" v-model.trim="loginForm.password"
                    show-password>
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
        <!-- 验证码 -->
        <el-form-item prop="captchaCode">
          <div class="check-code-panel">
            <el-input size="large" placeholder="请输入验证码" v-model.trim="loginForm.captchaCode">
              <template #prefix>
                <span class="iconfont icon-yanzhengyanzhengma"></span>
              </template>
            </el-input>

            <img :src="captchaBase64" class="check-code" @click="getCaptcha" />
          </div>
        </el-form-item>
        <!-- 其他选项 -->
        <el-form-item>
          <!--          <div class="remember-me-panel">-->
          <!--            <el-checkbox v-model.trim="loginForm.rememberMe">记住我</el-checkbox>-->
          <!--          </div>-->
          <div class="no-account">
            <!-- TODO 补全忘记密码与没有账号 -->
            <a href="javascript:void(0)" class="a-link" @click="showPanel(2)">忘记密码？</a>
            <a href="javascript:void(0)" class="a-link" @click="showPanel(0)">没有账号？</a>
          </div>
        </el-form-item>
        <!-- 登录按钮 -->
        <el-form-item>
          <el-button type="primary" class="op-btn" size="large" @click="handleLogin">
            <span>登录</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-body {
  height: calc(100vh);
  background-size: cover;
  background: url(@/assets/images/login_bg.jpg);
  display: flex;

  .bg {
    flex: 1;
    background-position: center;
    background-size: 800px;
    background-repeat: no-repeat;
    background-image: url(@/assets/images/login_img.png);
  }
}

.login-panel {
  width: 430px;
  margin-right: 15%;
  margin-top: calc((100vh - 500px) / 2);

  .login-register {
    padding: 25px;
    background: #fff;
    border-radius: 5px;

    .login-title {
      text-align: center;
      font-size: 18px;
      font-weight: bold;
      margin-bottom: 20px;
    }

    .remember-me-panel {
      width: 100%;
    }

    .no-account {
      width: 100%;
      display: flex;
      justify-content: space-between;
    }

    .op-btn {
      width: 100%;
    }
  }

  .check-code-panel {
    width: 100%;
    display: flex;

    .check-code {
      margin-left: 5px;
      cursor: pointer;
    }
  ;
  }
}
</style>