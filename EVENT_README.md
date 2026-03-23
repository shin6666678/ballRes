# 事件上报系统

## 一、整体架构设计

```
┌─────────────────────────────────────────────────────────┐
│                      EventManager                       │
│  (单例，统一管理事件分发)                                 │
│                                                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │ConsoleSender│  │NetworkSender│  │ FutureSender│ ...│
│  │  (控制台)   │  │  (网络请求)  │  │  (扩展)     │     │
│  └─────────────┘  └─────────────┘  └─────────────┘    │
└─────────────────────────────────────────────────────────┘
```

### 核心组件

1. **Event** - 事件数据模型
   - `name`: 事件名称
   - `params`: 事件参数
   - `timestamp`: 时间戳

2. **EventSender** - 发送器接口
   - 定义 `send(event: Event)` 方法
   - 方便扩展新的发送方式

3. **EventManager** - 事件管理器
   - `registerSender()`: 注册发送器
   - `trackEvent()`: 上报事件
   - 使用 `CoroutineScope(Dispatchers.IO)` 不阻塞主线程

### 已实现发送器

| 发送器 | 说明 |
|--------|------|
| ConsoleSender | 打印到控制台，用于调试 |
| NetworkSender | 模拟网络请求，异步发送 |

---

## 二、如何运行

### 编译 APK
```bash
./gradlew assembleDebug
```

### 安装到设备
```bash
./gradlew installDebug
```

### 触发的事件

| 事件名 | 触发条件 |
|--------|----------|
| `home_page_view` | 进入首页 |
| `camera_opened` | 打开相机 |
| `camera_closed` | 关闭相机 |
| `highlight_threshold_reached` | 点亮数量达到10 |

### 查看日志
```bash
adb logcat | grep -E "\[Event\]|\[Network\]"
```

---

## 三、改进

### 1. 批量发送
当前每条事件立即发送，高频事件会浪费资源。
- **改进**: 增加事件缓冲，达到一定数量或时间间隔后批量发送

### 2. 重试机制
网络失败时事件会丢失。
- **改进**: 增加本地缓存和重试队列

### 3. 用户ID绑定
- **改进**: 统一管理设备ID和用户ID

### 4. 压缩加密
- **改进**: 对事件数据进行压缩和加密传输

### 5. 更多发送器
- **改进**: 实现 FileSender（本地日志）、FirebaseSender 等
