# Experiment 6 Socket Online Battle Plan

## Goal

在 Android 版飞机大战中补上实验六要求的联机功能，满足以下最小验收：

1. 两名玩家可建立对战连接。
2. 任意一方分数变化时，另一方页面实时看到更新后的分数。
3. 双方都死亡后，两边同步展示最终比分。

本次实现只做课程实验要求，不把单机游戏改造成强同步实时对战。

## Chosen Approach

采用 `Socket` 长连接，不采用纯 `HTTP` 轮询。

原因：

- 需求本质是持续连接和双向事件推送，`Socket` 与问题形状一致。
- 调试时能直接看“连接建立 / 消息发送 / 消息接收 / 连接关闭”四类事件，链路短。
- 可以复用 JDK `ServerSocket` 和 Android 侧后台线程，不需要先引入额外服务框架。

## What Already Exists

- [MainActivity.java](/Users/larry_1/Opensource/Android_aircraftWar/app/src/main/java/edu/hitsz/MainActivity.java) 已有“单机 / 联机”入口，但联机仍是 toast。
- [GameActivity.java](/Users/larry_1/Opensource/Android_aircraftWar/app/src/main/java/edu/hitsz/GameActivity.java) 已经有按难度装配游戏视图、接收游戏结束消息、处理 Activity 生命周期的骨架。
- [BaseGame.java](/Users/larry_1/Opensource/Android_aircraftWar/app/src/main/java/edu/hitsz/application/BaseGame.java) 已有独立渲染线程、得分累加、游戏结束分发，是最合适的联机事件接入点。
- [MainActivityNavigationTest.java](/Users/larry_1/Opensource/Android_aircraftWar/app/src/androidTest/java/edu/hitsz/MainActivityNavigationTest.java) 和 [GameActivityTest.java](/Users/larry_1/Opensource/Android_aircraftWar/app/src/androidTest/java/edu/hitsz/GameActivityTest.java) 已有 Activity 跳转测试样式，可复用同类测试方法。

## Step 0 Scope Challenge

### Existing code that solves part of the problem

- 游戏循环、得分变更、死亡判定已经在 `BaseGame` 里稳定运行，不重写。
- Activity 到游戏视图的装配已由 `GameActivity` 证明可行，不新建复杂导航流。
- Android 主线程回调已在 `GameActivity` 通过 `Handler(Looper.getMainLooper())` 使用，不发明新的线程回调机制。

### Minimum change set

最小实现只做三件事：

1. 新增一个轻量 Socket 客户端，把本地分数和死亡事件发出去，并接收对端状态。
2. 在游戏 HUD 上显示“对手分数 + 连接状态”。
3. 新增一个简单 Java Socket 服务端，负责两人配对和状态广播。

### Complexity check

计划会触达 8 个以上文件，但新增核心类控制在 2 个以内：

- `OnlineGameActivity`
- `OnlineMatchClient`

服务端作为独立实验辅助文件存在，不进入 Android 运行时依赖图。

这是可以接受的复杂度，因为：

- Android 运行时改动仍围绕 `MainActivity` / `BaseGame` / `AndroidManifest` 这条主线展开。
- 没有引入数据库、服务发现、断线重连、鉴权、房间系统这些扩张项。

### Search check

- **[Layer 1]** Android 网络请求不能放主线程，官方建议后台线程 + 主线程回调。[Android background threads](https://developer.android.com/develop/background-work/background-tasks/asynchronous/java-threads)
- **[Layer 1]** JDK 自带 `ServerSocket.accept()` 足够承担实验服务器职责。[JDK 11 ServerSocket](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/ServerSocket.html)
- **[Layer 1]** 目标 SDK 36，当前只需要 `INTERNET` 权限；Android 16 本地网络保护仍是 opt-in，Android 17 才强制收口。[Android local network permission](https://developer.android.com/privacy-and-security/local-network-permission)
- **[Layer 3]** 对“实时同步得分”来说，HTTP 轮询是把事件流伪装成定时采样。能做，但调试成本更高，需求贴合度更差。

### TODO cross-reference

仓库当前没有 `TODOS.md`，本次计划中会把明确 defer 的项记在“NOT in scope”。

### Completeness check

本次不做 shortcut 到“只显示连接成功”，而是做完整的实验闭环：

- 连接建立
- 分数实时广播
- 双死结算广播
- 明确错误状态

但不把范围扩到真正的帧同步联机，这是另一个项目，不是这次要 boil 的 lake。

## Architecture Review

### Recommendation

采用“**本地单机逻辑 + 联机比分同步**”架构，不同步敌机、子弹、坐标。

这是最合理的边界。用户体验上确实不是严格公平对战，但完全满足实验要求，而且不会把 [BaseGame.java](/Users/larry_1/Opensource/Android_aircraftWar/app/src/main/java/edu/hitsz/application/BaseGame.java) 变成不可验证的大改。

### Data Flow

```text
MainActivity
   |
   | online mode + difficulty + host/port
   v
OnlineGameActivity
   |
   +--> create BaseGame (local loop still authoritative for self)
   |
   +--> start OnlineMatchClient on background thread
              |
              v
        TCP Socket Server
              |
     broadcast match state
              |
              v
OnlineMatchClient listener -> main thread -> BaseGame HUD / result dialog
```

### Runtime message protocol

使用简单文本协议，一行一条消息，便于 logcat 和服务端 stdout 联调：

```text
Client -> Server
JOIN
SCORE|120
GAME_OVER|230
QUIT

Server -> Client
ASSIGN|1
STATE|<p1Score>|<p1Alive>|<p2Score>|<p2Alive>|<phase>
RESULT|<p1Score>|<p2Score>
ERROR|<message>
MATCH_FULL
PEER_LEFT
```

选择文本协议而不是 JSON，原因很简单：更少依赖，更容易肉眼看日志。

### Production-like failure scenarios

1. 客户端连接成功但第二位玩家迟迟未加入。
   - 方案：HUD 显示 `Waiting for opponent`，本地游戏可继续运行。
2. 任意一方中途断线。
   - 方案：服务器广播 `PEER_LEFT`，客户端显示 `Opponent disconnected`。
3. 本地死亡已发送，但结果包未回来。
   - 方案：保留本地 Game Over 画面，等待服务端 `RESULT` 再弹结算框。
4. 第三个客户端误连。
   - 方案：服务器返回 `MATCH_FULL` 后立即关闭连接。

## Code Quality Review

### Opinionated recommendation

不要把网络逻辑散进 `BaseGame` 的渲染线程里。

`BaseGame` 只做两类事：

- 向外发事件：`score changed`, `self game over`
- 接收外部状态：`opponent score`, `match status`

网络连接、读写线程、协议解析都留在 `OnlineMatchClient`。这样符合“explicit > clever”和“minimal diff”。

### DRY boundary

难度分发不要复制一套在线版本 `EasyOnlineGame / NormalOnlineGame / HardOnlineGame`。

直接复用 `GameActivity.createGameViewByDifficulty()` 的思路，在 `OnlineGameActivity` 里按同一难度创建对应 `BaseGame` 子类，再给它挂联机能力。

### Inline diagram candidates

如果实现完成后逻辑稍复杂，建议在这些文件加 ASCII 注释：

- `OnlineMatchClient`：连接、读线程、写消息通道
- `BaseGame`：本地事件到联机同步的路径

## Test Review

### Code Path Coverage

```text
CODE PATH COVERAGE
===========================
[+] MainActivity online launch
    ├── [GAP] choose offline -> existing GameActivity path
    └── [GAP] choose online -> OnlineGameActivity with host/port extras

[+] OnlineMatchClient
    ├── [GAP] parse ASSIGN
    ├── [GAP] parse STATE with playerIndex=1
    ├── [GAP] parse STATE with playerIndex=2
    ├── [GAP] parse RESULT
    ├── [GAP] parse PEER_LEFT
    └── [GAP] unknown message fallback

[+] BaseGame online hooks
    ├── [GAP] score change emits network callback once per new score
    ├── [GAP] game over emits final score once
    └── [GAP] HUD renders opponent score and match status

[+] OnlineGameActivity flow
    ├── [GAP] local death sends GAME_OVER and waits for result
    ├── [GAP] remote result shows final dialog
    └── [GAP] connection failure shows clear error and exits safely

USER FLOW COVERAGE
===========================
[+] Happy path
    ├── [GAP] [->E2E] two clients connect, both see live scores
    └── [GAP] [->E2E] both die, both see final result

[+] Error path
    ├── [GAP] server unreachable
    ├── [GAP] opponent disconnects mid-match
    └── [GAP] match already full

─────────────────────────────────
COVERAGE: 0/15 paths tested
  Code paths: 0/12
  User flows: 0/3
QUALITY:  ★★★: 0  ★★: 0  ★: 0
GAPS: 15 paths need tests (2 need E2E)
─────────────────────────────────
```

### Required tests

1. `app/src/test/java/edu/hitsz/online/OnlineMessageParserTest.java`
   - 覆盖 `ASSIGN / STATE / RESULT / PEER_LEFT / MATCH_FULL / malformed`。
2. `app/src/test/java/edu/hitsz/online/OnlineHudStateTest.java`
   - 覆盖 player1 / player2 视角下，对手分数映射是否正确。
3. `app/src/androidTest/java/edu/hitsz/MainActivityOnlineNavigationTest.java`
   - 验证在线模式下 difficulty 按钮跳到 `OnlineGameActivity`，并带上 host/port。
4. 手工双端冒烟
   - 两个模拟器或一个模拟器 + 一个真机，连接同一服务端。

### Test plan artifact summary

QA 重点不是“打得爽”，而是：

- 看得到连接建立
- 看得到分数变化被广播
- 看得到双死结算被广播
- 看得到异常状态不是静默失败

## Performance Review

这次没有数据库，没有图片网络传输，也没有高频序列化大对象。

主要性能边界只有两个：

1. **消息发送频率**
   - 只在分数变更时发，不在每帧发。
2. **主线程更新**
   - 只更新少量 HUD 文本，不做额外渲染线程竞争。

没有发现必须上缓存、线程池或消息队列的理由。保持 boring。

## Failure Modes

| Codepath | Real failure | Test? | Error handling? | User-visible? |
|---|---|---:|---:|---:|
| connect socket | 服务器没启动 | planned | yes | yes |
| read loop | 服务端异常关闭 | planned | yes | yes |
| score sync | 短时间重复分数上报 | planned | yes | yes |
| game over sync | 本地已死但结果未达 | planned | yes | yes |
| second player wait | 长时间无人加入 | manual | yes | yes |

当前没有“无测试 + 无处理 + 静默失败”的 critical gap 设计项。

## NOT in Scope

- 敌机、子弹、道具、英雄坐标同步  
  这是强同步联机，不是本次实验要求。
- 房间列表、邀请码、重连机制  
  会显著拉长实现和调试链路。
- 联机结果写入排行榜  
  与实验六核心目标无关，先不把本地排名逻辑和联机结算耦合。
- TLS / 加密 / 线上部署  
  本次是局域网/本机调试实验。

## Worktree Parallelization Strategy

Sequential implementation, no parallelization opportunity.

原因：主线都围绕 `MainActivity`、`OnlineGameActivity`、`BaseGame`、联机协议这同一条路径，拆并行反而更容易冲突。

## Implementation Plan

1. 新增联机计划文档并锁定最小范围。
2. 在 AndroidManifest 中补网络权限，注册 `OnlineGameActivity`。
3. 在 `MainActivity` 加入在线模式选择状态和联机启动入口。
4. 新增 `OnlineGameActivity`，负责：
   - 读取难度与 host/port
   - 创建游戏视图
   - 建立 Socket 客户端
   - 收到结果后弹结算框
5. 新增 `OnlineMatchClient`，负责：
   - 后台连接
   - 消息读写
   - 主线程回调
6. 修改 `BaseGame`：
   - 增加在线 HUD 状态
   - 在分数变化时发出回调
   - 在 game over 时发出最终得分回调
7. 新增轻量解析单测。
8. 新增独立 Java 服务端文件和运行说明。
9. 执行 `./gradlew testDebugUnitTest` 与 `./gradlew assembleDebug`。

## Detailed Review Verdict

- Step 0: Scope accepted as-is, already reduced to score-only sync.
- Architecture Review: 0 unresolved issues.
- Code Quality Review: 0 unresolved issues.
- Test Review: 15 gaps identified and converted into implementation requirements.
- Performance Review: 0 blocking issues.
- Lake Score: choose complete Socket score-sync flow, not partial fake demo.
