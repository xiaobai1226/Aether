# Aether UseCase 单元测试

## 概述

本目录包含了 Aether 网盘系统 UseCase 层的单元测试。这些测试使用 JUnit 5 和 Mockito 框架编写，确保核心业务逻辑的正确性。

## 测试结构

```
test/
├── java/
│   └── com/
│       └── xiaobai1226/
│           └── aether/
│               └── core/
│                   └── usecase/
│                       ├── file/
│                       │   ├── MoveFileUseCaseTest.java          # 文件移动测试
│                       │   ├── CopyFileUseCaseTest.java          # 文件复制测试
│                       │   ├── DeleteFileUseCaseTest.java        # 文件删除测试
│                       │   ├── RenameFileUseCaseTest.java        # 文件重命名测试
│                       │   ├── CreateFolderUseCaseTest.java      # 创建文件夹测试
│                       │   └── DownloadFileUseCaseTest.java      # 文件下载测试（待添加）
│                       └── recycle/
│                           ├── RestoreFileUseCaseTest.java       # 回收站还原测试（待添加）
│                           └── PurgeRecycleUseCaseTest.java      # 彻底删除测试（待添加）
└── resources/
    └── application-test.yml                                       # 测试环境配置
```

## 已完成的测试

### 1. MoveFileUseCaseTest - 文件移动用例测试
- ✅ 正常移动文件到文件夹
- ✅ 源文件列表为空时抛出异常
- ✅ 目标文件夹不存在时抛出异常
- ✅ 移动到当前文件夹时抛出异常
- ✅ 目标文件夹存在同名文件时抛出异常
- ✅ 移动文件夹到其子文件夹时抛出异常
- ✅ 移动文件到根目录

### 2. CopyFileUseCaseTest - 文件复制用例测试
- ✅ 正常复制文件到文件夹
- ✅ 源文件列表为空时抛出异常
- ✅ 目标文件夹不存在时抛出异常
- ✅ 复制到当前文件夹时抛出异常
- ✅ 目标文件夹存在同名文件时抛出异常
- ✅ 空间不足时抛出异常
- ✅ 复制文件夹及其子文件

### 3. DeleteFileUseCaseTest - 文件删除用例测试
- ✅ 正常删除文件到回收站
- ✅ 删除文件时ID列表为空时抛出异常
- ✅ 删除不存在的文件时抛出异常
- ✅ 删除文件夹及其子文件
- ✅ 批量删除多个文件
- ✅ 删除部分文件不存在时抛出异常

### 4. RenameFileUseCaseTest - 文件重命名用例测试
- ✅ 正常重命名文件
- ✅ 重命名不存在的文件时抛出异常
- ✅ 重命名为已存在的名称时抛出异常
- ✅ 重命名为相同名称时成功（无变化）
- ✅ 重命名操作失败时抛出异常
- ✅ 重命名文件夹

### 5. CreateFolderUseCaseTest - 创建文件夹用例测试
- ✅ 在根目录创建文件夹
- ✅ 在指定路径下创建文件夹
- ✅ 在不存在的路径下创建文件夹时抛出异常
- ✅ 创建空名称文件夹时由Service层处理
- ✅ 创建同名文件夹时由Service层处理
- ✅ 创建深层嵌套文件夹

## 运行测试

### 使用 Gradle 运行所有测试

```bash
# 在项目根目录执行
./gradlew :aether-core:test

# 或者在 aether-core 目录执行
cd aether-core
../gradlew test
```

### 运行特定测试类

```bash
./gradlew :aether-core:test --tests "com.xiaobai1226.aether.core.usecase.file.MoveFileUseCaseTest"
```

### 运行特定测试方法

```bash
./gradlew :aether-core:test --tests "com.xiaobai1226.aether.core.usecase.file.MoveFileUseCaseTest.testMoveFileToFolderSuccess"
```

### 查看测试报告

测试完成后，可以在以下位置查看详细的测试报告：

```
aether-core/build/reports/tests/test/index.html
```

## 测试技术栈

- **JUnit 5**: 测试框架
- **Mockito**: Mock 框架，用于模拟依赖
- **AssertJ**: 断言库（可选，提供更流畅的断言）
- **H2 Database**: 内存数据库，用于测试数据库相关功能

## 测试原则

1. **隔离性**: 每个测试用例相互独立，不依赖其他测试
2. **可重复性**: 测试可以重复运行，结果一致
3. **快速执行**: 使用 Mock 避免真实的外部依赖（数据库、文件系统等）
4. **全面覆盖**: 测试正常路径和异常路径
5. **清晰命名**: 测试方法名称清晰表达测试意图

## 测试覆盖率目标

- **UseCase 层**: 80%+ 代码覆盖率
- **核心业务逻辑**: 100% 分支覆盖率

## 后续计划

- [ ] 添加 DownloadFileUseCaseTest
- [ ] 添加 RestoreFileUseCaseTest
- [ ] 添加 PurgeRecycleUseCaseTest
- [ ] 添加集成测试
- [ ] 配置测试覆盖率报告工具（JaCoCo）

## 注意事项

1. 测试使用 Mockito 模拟所有外部依赖，不会访问真实数据库或文件系统
2. 测试配置文件 `application-test.yml` 使用 H2 内存数据库
3. 每个测试类使用 `@ExtendWith(MockitoExtension.class)` 启用 Mockito
4. 测试方法使用 `@DisplayName` 注解提供清晰的中文描述

## 贡献指南

在添加新的 UseCase 时，请同时：
1. 创建对应的测试类
2. 至少覆盖正常流程和主要异常场景
3. 使用清晰的测试方法命名
4. 更新本 README 文档