# 飞机大战 —— Windows 端 Java 项目 Android 迁移方案

---

## 一、项目概述

### 1.1 项目简介

本项目是一款基于 Java Swing 开发的纵向卷轴射击游戏（飞机大战），由哈尔滨工业大学（深圳）课程实践驱动。玩家控制英雄飞机击毁不断涌现的敌机，拾取道具强化战力，在三种难度（简单 / 普通 / 困难）下争取最高分并记入排行榜。

核心技术指标：

| 指标 | 数值 |
|---|---|
| 源文件数量 | 51 个 Java 文件 |
| 包层级 | 9 个模块（application / aircraft / bullet / prop / factory / strategy / observer / leaderboard / basic） |
| 设计模式覆盖 | 单例、工厂方法、策略、观察者、模板方法、DAO 共 6 种 |
| 游戏实体 | 英雄机 1 种 + 敌机 4 种 + 子弹 2 种 + 道具 4 种 |
| 难度模式 | Easy / Normal / Hard / Demo 共 4 种 |

### 1.2 实施意义

1. **教学价值**：项目原始代码已体现丰富的面向对象设计模式（单例、工厂、策略、观察者、模板方法），迁移至 Android 平台后可进一步引入 MVVM 架构、生命周期管理等 Android 特有的工程实践，深化学生对移动开发范式的理解。

2. **平台拓展**：Windows 桌面端受众有限，Android 设备覆盖全球超 30 亿用户，迁移后可真机部署、触屏交互，游戏体验更自然。

3. **网络能力引入**：原项目为纯单机本地应用，迁移至 Android 后天然具备网络条件，可拓展在线排行榜、多人对战等社交化功能，提升项目技术深度和可玩性。

4. **工程能力锻炼**：跨平台迁移涉及 UI 框架替换、输入系统重写、音频引擎适配、数据持久化方案更换等一系列工程难题，能有效锻炼团队的架构设计与协作能力。

---

## 二、原有功能迁移分析

### 2.1 项目架构总览

```
edu.hitsz
├── basic/                  # 基础抽象层
│   └── AbstractFlyingObject        # 所有飞行对象的顶级父类
├── aircraft/               # 飞机实体
│   ├── AbstractAircraft            # 飞机抽象类（含 HP、射击策略）
│   ├── HeroAircraft                # 英雄机（单例模式）
│   ├── MobEnemy                    # 普通敌机
│   ├── EliteEnemy                  # 精英敌机
│   ├── ElitePlusEnemy              # 超级精英敌机
│   └── BossEnemy                   # Boss 敌机
├── bullet/                 # 子弹实体
│   ├── BaseBullet                  # 子弹抽象类
│   ├── HeroBullet                  # 英雄子弹
│   └── EnemyBullet                 # 敌机子弹（实现 BombObserver）
├── prop/                   # 道具实体
│   ├── AbstractProp                # 道具抽象类
│   ├── BloodProp                   # 加血道具
│   ├── BulletProp                  # 火力道具（升级散射）
│   ├── BulletPlusProp              # 超级火力道具（环射）
│   └── BombProp                    # 炸弹道具（观察者模式）
├── factory/                # 工厂模式
│   ├── EnemyFactory                # 敌机工厂接口
│   ├── MobEnemyFactory / EliteEnemyFactory / ElitePlusEnemyFactory / BossEnemyFactory
│   ├── PropFactory                 # 道具工厂接口
│   └── BloodPropFactory / BulletPropFactory / BulletPlusPropFactory / BombPropFactory
├── strategy/               # 策略模式（射击）
│   ├── ShootStrategy               # 策略接口
│   ├── StraightShootStrategy       # 直射（1~N 发直线子弹）
│   ├── ScatterShootStrategy        # 散射（扇形弹幕）
│   ├── CircleShootStrategy         # 环射（360° 弹幕）
│   └── NoShootStrategy             # 不射击（普通敌机）
├── observer/               # 观察者模式（炸弹）
│   ├── BombObserver                # 观察者接口
│   └── BombResult                  # 炸弹反应结果封装
├── leaderboard/            # 排行榜数据层
│   ├── ScoreRecord                 # 得分记录 POJO
│   ├── ScoreRecordDAO              # DAO 接口
│   └── impl/ScoreRecordFileDAO     # 文件持久化实现（单例）
└── application/            # 应用层（UI + 控制）
    ├── Main / DemoMain             # 入口
    ├── Game / DemoGame             # 游戏主循环面板
    ├── GameMainFrame               # 主窗口（CardLayout 管理多页面）
    ├── DifficultySettingPanel      # 难度选择界面
    ├── LeaderboardPanel            # 排行榜界面
    ├── ImageManager                # 图片资源管理（静态加载）
    ├── HeroController              # 鼠标控制英雄机
    ├── MusicManager / MusicThread  # 音乐音效播放
    └── gamemode/                   # 模板方法模式
        ├── AbstractGameMode        # 抽象游戏模式
        ├── EasyMode / NormalMode / HardMode / DemoMode
```

### 2.2 可直接复用的代码模块

以下模块的核心逻辑**与平台无关**，可在 Android 项目中直接复用或仅做最小改动：

| 模块 | 涉及类 | 可复用原因 |
|---|---|---|
| **游戏实体模型** | `AbstractAircraft`, `HeroAircraft`, `MobEnemy`, `EliteEnemy`, `ElitePlusEnemy`, `BossEnemy`, `BaseBullet`, `HeroBullet`, `EnemyBullet` | 纯 Java 逻辑，坐标、HP、速度、碰撞检测等计算不依赖 Swing 或 AWT |
| **道具系统** | `AbstractProp`, `BloodProp`, `BulletProp`, `BulletPlusProp`, `BombProp` | 道具激活逻辑（加血、切换射击策略）完全独立于 UI |
| **策略模式** | `ShootStrategy`, `StraightShootStrategy`, `ScatterShootStrategy`, `CircleShootStrategy`, `NoShootStrategy` | 射击弹道计算纯数学运算，无平台依赖 |
| **工厂模式** | 全部 8 个工厂类 | 仅用于创建实体对象，不涉及任何 UI 操作 |
| **观察者模式** | `BombObserver`, `BombResult` | 接口与数据封装类，纯 Java |
| **游戏模式** | `AbstractGameMode`, `EasyMode`, `NormalMode`, `HardMode`, `DemoMode` | 模板方法中的难度参数配置、Boss 触发逻辑、动态难度调节均为纯计算 |
| **数据模型** | `ScoreRecord`, `ScoreRecordDAO` | POJO 和 DAO 接口与平台无关 |

> **复用比例估算**：约 38/51 个文件（约 75%）的核心逻辑可直接迁移，需修改的仅是 `AbstractFlyingObject` 中与 `BufferedImage` 绑定的图片字段以及边界判定中对 `Main.WINDOW_WIDTH` 的引用。

### 2.3 需重新适配安卓平台的功能模块

| 功能模块 | 原实现 | 需适配原因 | Android 适配方案 |
|---|---|---|---|
| **游戏渲染引擎** | `Game` 继承 `JPanel`，重写 `paint(Graphics g)` 绘制背景、飞机、子弹、道具、分数 | `javax.swing.JPanel` 和 `java.awt.Graphics` 在 Android 上不可用 | 使用 `SurfaceView` + `Canvas` 双缓冲渲染，或引入轻量游戏框架（如 libGDX） |
| **游戏主循环** | `ScheduledThreadPoolExecutor` 每 40ms 执行一次任务（刷新、碰撞检测、后处理） | Android 推荐使用 `SurfaceView` 渲染线程或 `Choreographer` 同步 VSYNC | 在 `SurfaceView.Callback` 中启动独立渲染线程，使用 `Thread.sleep` 或 `Handler` 控制帧率 |
| **玩家输入控制** | `HeroController` 使用 `MouseAdapter` 监听鼠标拖拽 (`mouseDragged`) | Android 无鼠标事件，使用触摸屏 | 重写为 `View.OnTouchListener`，监听 `MotionEvent.ACTION_MOVE`，或加入虚拟摇杆、陀螺仪控制 |
| **图片资源管理** | `ImageManager` 使用 `ImageIO.read(FileInputStream)` 加载 `BufferedImage` | `javax.imageio` 和 `BufferedImage` 在 Android 上不可用 | 使用 `BitmapFactory.decodeResource()` 从 `res/drawable` 加载 `Bitmap`，或使用 `AssetManager` |
| **音乐与音效** | `MusicThread` 使用 `javax.sound.sampled` (`AudioInputStream`, `SourceDataLine`) 播放 WAV 文件 | `javax.sound.sampled` 在 Android 上不可用 | 背景音乐使用 `MediaPlayer`，短音效使用 `SoundPool`，支持并发播放和资源管理 |
| **窗口管理与页面切换** | `GameMainFrame`(JFrame) + `CardLayout` 管理设置、游戏、排行榜三个面板的切换 | Swing 窗口体系在 Android 不可用 | 使用 `Activity` + `Fragment` 实现页面管理，或采用 Jetpack Navigation 组件 |
| **难度选择界面** | `DifficultySettingPanel` 使用 JRadioButton、JCheckBox、JButton | Swing 组件不可用 | 使用 Android 原生 XML 布局（`RadioGroup`、`Switch`、`Button`），结合 Material Design 风格 |
| **排行榜界面** | `LeaderboardPanel` 使用 JTable 展示排名、JOptionPane 弹窗 | Swing 组件不可用 | 使用 `RecyclerView` + 自定义 Adapter 展示列表，`AlertDialog` 替代弹窗 |
| **数据持久化** | `ScoreRecordFileDAO` 使用 `java.nio.file` 读写 txt 文件 | Android 文件系统沙箱机制不同 | 使用 **SQLite + Room** ORM 框架，或轻量级方案 `SharedPreferences`（小数据量） |
| **飞行对象基类** | `AbstractFlyingObject` 中 `getImage()` / `getWidth()` / `getHeight()` 依赖 `BufferedImage` | 需替换为 Android `Bitmap` | 剥离图片引用，通过资源 ID 映射获取 `Bitmap`，宽高在加载时缓存 |
| **边界常量** | 多处硬编码 `Main.WINDOW_WIDTH (512)` / `Main.WINDOW_HEIGHT (768)` | Android 设备屏幕尺寸各异 | 运行时通过 `DisplayMetrics` 获取屏幕宽高，使用比例坐标或适配因子 |

### 2.4 设计模式迁移说明

原项目使用的 6 种设计模式全部可在 Android 平台保留，但部分需微调：

| 设计模式 | 原实现 | Android 迁移备注 |
|---|---|---|
| **单例模式** | `HeroAircraft`（双重检查锁）、`ScoreRecordFileDAO` | 保持不变，但需注意 Android Activity 重建时的生命周期问题 |
| **工厂方法** | `EnemyFactory` + 4 实现、`PropFactory` + 4 实现 | 完全复用，无需修改 |
| **策略模式** | `ShootStrategy` + 4 实现 | 完全复用，无需修改 |
| **观察者模式** | `BombObserver` + `BombResult` | 完全复用；如需 UI 联动可结合 `LiveData` |
| **模板方法** | `AbstractGameMode.startGame()` | 完全复用，`System.out.println` 可替换为 `Log.d` |
| **DAO 模式** | `ScoreRecordDAO` → `ScoreRecordFileDAO` | 接口保留，实现层替换为 Room 数据库 |

---

## 三、新增网络功能设计

### 3.1 功能一：在线排行榜与云端成绩同步

#### 功能内容

- 玩家游戏结束时，得分自动上传至云端服务器
- 支持按难度查看**全服排行榜**（Top 100）、**好友排行**、**周榜 / 月榜 / 总榜**
- 离线时得分暂存本地，恢复网络后自动同步
- 排行榜展示玩家昵称、头像、分数、难度、游戏时间

#### 设计原因

1. 原项目排行榜仅存储在本地 txt 文件中，数据孤立，无法与其他玩家比较
2. 全服排行榜能显著提升玩家竞争动力和留存率
3. 排行榜是单机游戏向社交化转型的最低成本入口

#### 技术实现思路

```
┌──────────────┐      HTTPS/JSON       ┌──────────────────────┐
│  Android 客户端 │ ◄─────────────────► │  云端服务（后端 API）  │
│              │                       │                      │
│  Room 本地缓存 │                       │  MySQL / MongoDB     │
│  Retrofit 网络 │                       │  RESTful API         │
│  离线队列      │                       │  Redis 排行榜缓存     │
└──────────────┘                       └──────────────────────┘
```

**客户端关键实现**：

| 层级 | 技术选型 | 职责 |
|---|---|---|
| 网络请求 | **Retrofit 2 + OkHttp** | 封装 REST API 调用（POST 上传分数、GET 拉取排行榜） |
| 数据缓存 | **Room + LiveData** | 本地 SQLite 缓存排行榜数据，离线可查看；LiveData 驱动 UI 更新 |
| 离线同步 | **WorkManager** | 检测网络恢复后自动批量上传暂存的离线得分 |
| 用户标识 | **设备 ID + 可选登录** | 初期可用设备唯一标识匿名上传，后期可接入微信 / QQ 登录 |

**核心 API 设计**：

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/scores` | `POST` | 上传得分记录（玩家名、分数、难度、时间戳） |
| `/api/leaderboard/{difficulty}` | `GET` | 获取指定难度排行榜（支持分页、时间范围筛选） |
| `/api/leaderboard/friends` | `GET` | 获取好友排行（需登录） |

**服务端关键实现**：

- 使用 **Redis Sorted Set** 存储实时排行榜，`ZADD` 写入分数、`ZREVRANGE` 取 Top N，O(logN) 复杂度
- MySQL 存储历史记录明细，Redis 做热点排名缓存
- 可部署于 Cloudflare Workers / 阿里云函数计算等 Serverless 平台，降低运维成本

---

### 3.2 功能二：实时双人协作对战模式

#### 功能内容

- 支持两名玩家通过网络连接，在同一战场协作作战
- 双方共享屏幕中的敌机、Boss、道具，各自操控自己的英雄机
- 一方拾取道具效果对双方可见（如炸弹清屏效果同步）
- 任一方英雄机阵亡，游戏结束，双方分数合计为团队总分
- 支持**房间制匹配**：创建房间 → 分享房间号 → 好友加入 → 开始游戏

#### 设计原因

1. 协作模式是射击游戏社交化的核心玩法，能显著提升用户粘性
2. 双人对战涉及实时状态同步、网络延迟补偿等技术难题，对项目的工程深度有较大提升
3. 基于现有的单机游戏框架，仅需在游戏循环中增加网络同步层，改动可控

#### 技术实现思路

```
┌───────────────┐   WebSocket   ┌──────────────────┐   WebSocket   ┌───────────────┐
│  Player A      │ ◄────────────► │  实时对战服务器    │ ◄────────────► │  Player B      │
│  (Host/Client) │              │  (房间管理+状态广播) │              │  (Client)      │
└───────────────┘              └──────────────────┘              └───────────────┘
```

**架构选型**：

| 组件 | 技术选型 | 说明 |
|---|---|---|
| 实时通信协议 | **WebSocket** | 全双工低延迟通信，适合游戏帧同步 |
| 客户端 WebSocket | **OkHttp WebSocket** | Android 原生支持，API 简洁 |
| 服务端 | **Netty / Spring WebSocket** | 高性能异步事件驱动框架，支撑万级并发连接 |
| 同步策略 | **帧同步 (Lockstep)** | 客户端发送操作指令，服务端广播，双方各自运算，保证一致性 |
| 房间管理 | **Redis Pub/Sub** | 管理房间创建 / 加入 / 销毁，高效的消息广播 |

**同步协议设计**：

客户端每帧（40ms）上报操作指令，服务端收集后广播：

```json
// 客户端 → 服务端（操作指令）
{
  "type": "INPUT",
  "frame": 1523,
  "playerId": "A",
  "heroX": 256,
  "heroY": 600
}

// 服务端 → 所有客户端（状态同步）
{
  "type": "SYNC",
  "frame": 1523,
  "players": [
    {"id": "A", "heroX": 256, "heroY": 600, "hp": 280},
    {"id": "B", "heroX": 180, "heroY": 550, "hp": 300}
  ],
  "events": [
    {"type": "ENEMY_SPAWN", "enemyType": "ELITE", "x": 300, "y": 20},
    {"type": "PROP_DROP", "propType": "BOMB", "x": 200, "y": 400}
  ]
}
```

**延迟补偿**：

- 客户端预测：本地立即渲染英雄机移动，不等待服务端确认
- 服务端权威：碰撞判定、伤害计算、道具效果以服务端为准
- 差值平滑：远端玩家位置使用线性插值（Lerp）平滑显示，避免抖动

**房间生命周期**：

```
创建房间 → 等待匹配 → 双方就绪 → 倒计时开始 → 游戏运行 → 任一方阵亡 → 结算展示
```

---

### 3.3 功能三（附加）：每日挑战与全服竞赛

#### 功能内容

- 服务端每日推送一组固定的敌机生成序列（种子随机数），所有玩家面对完全相同的关卡
- 玩家通关后上传分数，与全服玩家在公平条件下竞技
- 每日 / 每周颁发排名奖励（虚拟称号、特殊皮肤解锁）

#### 设计原因

1. 固定种子保证公平性，消除随机因素导致的分数不可比问题
2. 限时挑战创造紧迫感和回访动机，提升日活跃

#### 技术实现思路

- 服务端每日生成随机种子并通过 API 下发
- 客户端使用 `Random(seed)` 确定性生成敌机序列
- 游戏结束时上传分数 + 种子校验码，防止作弊

---

## 四、小组人员分工及依据

建议 4 人小组分工如下：

| 角色 | 成员 | 负责模块 | 分工依据 |
|---|---|---|---|
| **组长 / 架构师** | 郝卓远 | 项目整体架构设计；`AbstractFlyingObject` 基类适配（`BufferedImage` → `Bitmap`）；游戏主循环（`SurfaceView` + 渲染线程）；碰撞检测优化；屏幕适配方案 | 需要全局视角，理解原 Windows 代码架构和 Android 生命周期，能统筹协调各模块接口 |
| **游戏逻辑开发** | 魏屿桐 | 迁移全部游戏实体（aircraft / bullet / prop）；迁移设计模式模块（factory / strategy / observer）；迁移游戏模式（gamemode）；Demo 模式适配 | 模块数量最多但改动量最小（大部分可直接复用），适合对面向对象设计模式较熟悉的成员 |

### 协作约定

1. **接口先行**：成员 A 优先定义 Android 端的核心接口（渲染器接口、资源加载接口、网络同步接口），各成员面向接口编程
2. **Git 分支管理**：采用 `main` + `feature/*` 分支模型，每人在独立 feature 分支开发，通过 Pull Request 合入
3. **每周 Code Review**：至少一次全组 Code Review，确保代码风格统一、设计模式正确运用
4. **测试要求**：各模块提供单元测试（JUnit 5），游戏逻辑模块覆盖率 ≥ 60%

---


---

> **文档版本**：v1.0  
> **生成日期**：2026-03-18  
> **基于源码**：`AircraftWar-win/src/` (51 个 Java 源文件)
