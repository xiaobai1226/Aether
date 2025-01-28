<script setup lang="ts">
import { ref, computed } from 'vue'
import type {
  GetUserListByPageRequest,
  GetUserListByPageResponse,
  AddUserRequest,
  UserInfo, UpdateUserRequest
} from '@/api/admin/user/types'
import { addUser, getUserListByPage, updateUser } from '@/api/admin/user'
import Utils from '@/utils/Utils'
import Dialog from '@/components/Dialog.vue'
import { ByteConversionFators } from '@/enums/ByteConversionFators'
import Verify from '@/utils/Verify'
import { ResultErrorMsgEnum } from '@/enums/ResultErrorMsgEnum'
import { ElMessage } from 'element-plus'

const layout = computed(() => {
  return `total, prev, pager, next, jumper`
})

/**
 * 初始化列表数据
 */
const tableData = ref<GetUserListByPageResponse>({
  list: [],
  pageNum: 1,
  pageSize: 30,
  total: 0,
  totalPage: 0
})

/**
 * 加载用户列表
 */
const loadDataList = () => {
  const getUserListByPageRequest: GetUserListByPageRequest = {
    pageNum: tableData.value.pageNum,
    pageSize: tableData.value.pageSize
  }

  // 请求后台获取文件列表
  getUserListByPage(getUserListByPageRequest).then(({ data }) => {
    if (data == null) {
      tableData.value = {
        list: [],
        pageNum: 1,
        pageSize: 30,
        total: 0,
        totalPage: 0
      }
    } else {
      tableData.value.totalPage = data.totalPage
      tableData.value.total = data.total
      tableData.value.list = data.list
    }
  }).catch(() => {
  })
}

loadDataList()

/**
 * 切换每页的大小
 * @param size
 */
const handlePageSizeChange = (size: number) => {
  tableData.value.pageSize = size
  tableData.value.pageNum = 1
  loadDataList()
}

/**
 * 切换页码
 * @param pageNum
 */
const handlePageNoChange = (pageNum: number) => {
  tableData.value.pageNum = pageNum
  loadDataList()
}

/**
 * 新增用户表单对象
 */
const addOrUpdateUserForm = ref({
  username: '',
  nickname: '',
  password: '',
  roleId: 0,
  status: true,
  totalStorage: 5,
  totalStorageUnit: ByteConversionFators['MB']
})

// 新增规则对象
const addUserRules = {
  username: [
    { required: true, message: ResultErrorMsgEnum.ERROR_USERNAME_EMPTY, trigger: 'blur' },
    { validator: Verify.username, message: ResultErrorMsgEnum.ERROR_USERNAME_FORMAT, trigger: 'blur' }
  ],
  password: [
    { required: true, message: ResultErrorMsgEnum.ERROR_PASSWARD_EMPTY, trigger: 'blur' },
    { validator: Verify.password, message: ResultErrorMsgEnum.ERROR_PASSWARD_FORMAT, trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: ResultErrorMsgEnum.ERROR_ROLE_EMPTY, trigger: 'blur' }
  ],
  status: [
    { required: true, message: ResultErrorMsgEnum.ERROR_USER_STATUS_EMPTY, trigger: 'blur' }
  ],
  totalStorage: [
    { required: true, message: ResultErrorMsgEnum.ERROR_TOTAL_STORAGE_EMPTY, trigger: 'blur' }
  ]
}

// 修改规则对象
const updateUserRules = {
  username: [
    { required: true, message: ResultErrorMsgEnum.ERROR_USERNAME_EMPTY, trigger: 'blur' },
    { validator: Verify.username, message: ResultErrorMsgEnum.ERROR_USERNAME_FORMAT, trigger: 'blur' }
  ],
  password: [
    { validator: Verify.password, message: ResultErrorMsgEnum.ERROR_PASSWARD_FORMAT, trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: ResultErrorMsgEnum.ERROR_ROLE_EMPTY, trigger: 'blur' }
  ],
  status: [
    { required: true, message: ResultErrorMsgEnum.ERROR_USER_STATUS_EMPTY, trigger: 'blur' }
  ],
  totalStorage: [
    { required: true, message: ResultErrorMsgEnum.ERROR_TOTAL_STORAGE_EMPTY, trigger: 'blur' }
  ]
}

const addOrUpdateUserFormRef = ref()

/**
 * 新增用户Dialog配置对象
 */
const addOrUpdateUserDialogConfig = ref({
  // 0 新增 1 修改
  type: 0,
  show: false,
  title: '新增用户',
  buttons: [
    {
      text: '新增',
      type: 'primary',
      click: () => {
        addOrUpdateUser()
      }
    }
  ]
})

/**
 * 关闭新建用户弹窗
 */
const closeAddOrUpdateUserDialog = () => {
  addOrUpdateUserDialogConfig.value.show = false
}

/**
 * 当前要修改的用户ID
 */
const currentUpdateUser = ref<UserInfo>()

/**
 * 打开新建用户弹窗
 * @param type 0 新建 1 修改
 * @param userInfo 用户信息
 */
const showAddOrUpdateUserDialog = (type: number, userInfo: UserInfo | null) => {
  addOrUpdateUserDialogConfig.value.type = type
  if (type == 0) {
    currentUpdateUser.value = undefined
    addOrUpdateUserDialogConfig.value.title = '新增用户'
    addOrUpdateUserDialogConfig.value.buttons[0].text = '新增'
  } else if (type == 1) {
    if (!userInfo) {
      ElMessage.warning(ResultErrorMsgEnum.ERROR_UPDATE_USER_EMPTY)
      return
    }
    currentUpdateUser.value = userInfo
    addOrUpdateUserForm.value.username = userInfo.username
    addOrUpdateUserForm.value.nickname = userInfo.nickname
    addOrUpdateUserForm.value.roleId = userInfo.roleId
    addOrUpdateUserForm.value.status = userInfo.status === 1

    const totalStorageArray = Utils.getSizeStrArray(userInfo.totalStorage)
    addOrUpdateUserForm.value.totalStorage = Number(totalStorageArray[0])
    addOrUpdateUserForm.value.totalStorageUnit = ByteConversionFators[totalStorageArray[1]]

    addOrUpdateUserDialogConfig.value.title = '修改用户'
    addOrUpdateUserDialogConfig.value.buttons[0].text = '修改'
  } else {
    return
  }

  addOrUpdateUserDialogConfig.value.show = true
}

/**
 * 新增或修改用户
 */
const addOrUpdateUser = () => {
  if (currentUpdateUser.value === undefined) {
    handleAddUser()
  } else {
    handleUpdateUser(currentUpdateUser.value)
  }
}

/**
 * 新增用户
 */
const handleAddUser = () => {
  addOrUpdateUserFormRef.value.validate((valid: boolean) => {
    // 所有表单都通过校验才为true
    if (valid) {
      const addUserRequest: AddUserRequest = {
        username: addOrUpdateUserForm.value.username,
        nickname: addOrUpdateUserForm.value.nickname ? addOrUpdateUserForm.value.nickname : undefined,
        password: addOrUpdateUserForm.value.password,
        roleId: addOrUpdateUserForm.value.roleId,
        status: addOrUpdateUserForm.value.status ? 1 : 0,
        totalStorage: addOrUpdateUserForm.value.totalStorage * addOrUpdateUserForm.value.totalStorageUnit
      }

      addUser(addUserRequest).then(() => {
        closeAddOrUpdateUserDialog()
        loadDataList()
      })
    }
  })
}

/**
 * 更新用户
 */
const handleUpdateUser = (userInfo: UserInfo) => {
  addOrUpdateUserFormRef.value.validate((valid: boolean) => {
    // 所有表单都通过校验才为true
    if (valid) {
      const updateUserRequest: UpdateUserRequest = {
        id: userInfo.id,
        username: (addOrUpdateUserForm.value.username !== userInfo.username) ? addOrUpdateUserForm.value.username : undefined,
        nickname: (addOrUpdateUserForm.value.nickname !== userInfo.nickname) ? addOrUpdateUserForm.value.nickname : undefined,
        password: addOrUpdateUserForm.value.password ? addOrUpdateUserForm.value.password : undefined,
        roleId: (addOrUpdateUserForm.value.roleId !== userInfo.roleId) ? addOrUpdateUserForm.value.roleId : undefined,
        status: ((addOrUpdateUserForm.value.status ? 1 : 0) !== userInfo.status) ? (addOrUpdateUserForm.value.status ? 1 : 0) : undefined,
        totalStorage: ((addOrUpdateUserForm.value.totalStorage * addOrUpdateUserForm.value.totalStorageUnit) !== userInfo.totalStorage) ? (addOrUpdateUserForm.value.totalStorage * addOrUpdateUserForm.value.totalStorageUnit) : undefined
      }

      if (updateUserRequest.username === undefined && updateUserRequest.nickname === undefined && updateUserRequest.password === undefined && updateUserRequest.roleId === undefined && updateUserRequest.status === undefined && updateUserRequest.totalStorage === undefined) {
        closeAddOrUpdateUserDialog()
        return
      }
      updateUser(updateUserRequest).then(() => {
        closeAddOrUpdateUserDialog()
        loadDataList()
      })
    }
  })
}

/**
 * 更新用户状态
 */
const updateUserStatus = (userInfo: UserInfo) => {
  const updateUserRequest: UpdateUserRequest = {
    id: userInfo.id,
    status: (userInfo.status === 1) ? 0 : 1
  }

  updateUser(updateUserRequest).then(() => {
    loadDataList()
  })
}
</script>

<template>
  <div class="container">
    <div class="top">
      <div class="top-op">
        <el-button type="success" @click="showAddOrUpdateUserDialog(0,null)">
          <span class="iconfont icon-xinzengyonghu"></span>
          新增用户
        </el-button>

        <div class="iconfont icon-refresh" @click="loadDataList"></div>
      </div>
    </div>

    <div class="data-list">
      <el-table :data="tableData.list" stripe border>
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column prop="username" label="用户名" width="100" />
        <el-table-column prop="nickname" label="昵称" width="100" />
        <el-table-column prop="roleId" label="角色" width="108">
          <template #default="scope">
            <el-tag
              :type="scope.row.roleId === 1 ? 'success' : 'primary'"
              disable-transitions
            >{{ scope.row.roleId === 1 ? '超级管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="lastLoginTime" label="最近一次登录时间" width="180" />
        <el-table-column prop="status" label="用户状态" width="85">
          <template #default="scope">
            <el-tag
              :type="scope.row.status === 1 ? 'success' : 'danger'"
              disable-transitions
            >{{ scope.row.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="存储空间" width="250">
          <template #default="scope">
            {{ Utils.sizeToStr(scope.row.usedStorage) }} / {{ Utils.sizeToStr(scope.row.totalStorage) }}
            <el-progress type="circle"
                         :percentage="Number(((scope.row.usedStorage / scope.row.totalStorage) * 100).toFixed(2))"
                         :width="50">
              <template #default="{ percentage }">
                <span class="percentage-value">{{ percentage }}%</span>
              </template>
            </el-progress>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="showAddOrUpdateUserDialog(1,scope.row)">
              编辑
            </el-button>
            <el-popconfirm :title="scope.row.status === 0 ? '确认启用该用户？': '确认禁用该用户？'" width="160"
                           @confirm="updateUserStatus(scope.row)">
              <template #reference>
                <el-button size="small" :type="(scope.row.status === 0 ? 'success':'warning')">
                  {{ (scope.row.status === 0 ? '启用' : '禁用') }}
                </el-button>
              </template>
            </el-popconfirm>
            <!--            <el-button size="small" type="danger">-->
            <!--              删除-->
            <!--            </el-button>-->
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination v-if="tableData.total" background :total="tableData.total"
                       :page-sizes="[15, 30, 50, 100]"
                       :page-size="tableData.pageSize" v-model:current-page="tableData.pageNum"
                       :layout="layout" @current-change="handlePageNoChange" @size-change="handlePageSizeChange"
                       style="text-align: right" />
      </div>
    </div>
  </div>


  <Dialog :show="addOrUpdateUserDialogConfig.show" width="600px" :showCannel="false"
          :title="addOrUpdateUserDialogConfig.title"
          :buttons="addOrUpdateUserDialogConfig.buttons" @close="addOrUpdateUserDialogConfig.show=false">
    <el-form :model="addOrUpdateUserForm" label-width="auto" style="max-width: 600px"
             :rules="currentUpdateUser === undefined ? addUserRules : updateUserRules"
             ref="addOrUpdateUserFormRef">
      <el-form-item label="用户名" prop="username">
        <el-input placeholder="请输入用户名" v-model.trim="addOrUpdateUserForm.username" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input placeholder="请输入昵称" v-model.trim="addOrUpdateUserForm.nickname" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input type="password" placeholder="请输入密码" v-model.trim="addOrUpdateUserForm.password" show-password />
      </el-form-item>
      <el-form-item label="角色" prop="roleId">
        <el-radio-group v-model="addOrUpdateUserForm.roleId">
          <el-radio :value="0">普通用户</el-radio>
          <el-radio :value="1">超级管理员</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="是否启用" prop="status">
        <el-switch v-model="addOrUpdateUserForm.status" />
      </el-form-item>
      <el-form-item label="存储空间" prop="totalStorage">
        <el-input-number v-model.trim="addOrUpdateUserForm.totalStorage" style="width: 150px" />
        <el-select v-model="addOrUpdateUserForm.totalStorageUnit" style="width: 70px">
          <template v-for="key in ByteConversionFators" :key="key">
            <el-option v-if="isNaN(key)" :label="key" :value="ByteConversionFators[key]" />
          </template>
        </el-select>
      </el-form-item>
    </el-form>
  </Dialog>
</template>

<style scoped lang="scss">
.container {
  width: 100%;
}

.top {
  margin-top: 20px;
  margin-left: 20px;

  .top-op {
    display: flex;
    align-items: center;

    .icon-xinzengyonghu {
      font-size: 20px;
    }

    .icon-refresh {
      cursor: pointer;
      margin-left: 20px;
    }
  }
}

.data-list {
  margin-top: 20px;
  margin-left: 20px;
  margin-right: 20px;
}

.percentage-value {
  display: block;
  font-size: 10px;
}

.pagination {
  padding-top: 10px;
  padding-right: 10px;

  .el-pagination {
    justify-content: right;
  }
}
</style>