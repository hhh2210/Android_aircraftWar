# Android Aircraft War

这是一个将 Windows 版 Java 飞机大战项目迁移到 Android 平台的课程实验项目。

## 项目说明

- 课程：软件构造实践
- 当前阶段：已完成实验二、实验三的主要要求
- 目标：将原有 Windows/Swing 版本迁移到 Android，并补齐难度选择页面与页面跳转流程

## 已完成内容

- 使用 `SurfaceView` 重构游戏主界面与渲染线程
- 将游戏主循环重构为 `SurfaceView` 绘制线程
- 使用触摸事件控制英雄机移动
- 使用 `BitmapFactory` 从 `res/drawable` 加载图片资源
- 迁移飞机、子弹、道具、工厂、策略、观察者等核心逻辑代码
- 保留并接入 `Easy / Normal / Hard` 三种难度模式
- 新增基于 XML 的难度选择页面
- 使用 `Intent` 从 `MainActivity` 跳转到 `GameActivity`
- 在 `GameActivity` 中按难度参数加载对应的游戏场景
- 补充了难度分发的本地单元测试和页面跳转相关的 Android 测试

## 运行方式

1. 使用 Android Studio 打开本项目
2. 等待 Gradle Sync 完成
3. 选择一个 Android 模拟器或真机
4. 点击 Run 运行应用

启动后会先进入难度选择页面。点击 `Easy`、`Normal` 或 `Hard` 后进入对应游戏场景，并可通过拖动控制英雄机。

## 团队协作

克隆仓库：

```bash
git clone https://github.com/hhh2210/Android_aircraftWar.git
```

分支约定：
- `main` 分支保持可编译状态，不直接在 `main` 上开发
- 开发新功能时从 `main` 创建功能分支，完成后合并回 `main`
- 提交信息使用中文

## 项目结构

```
app/src/main/java/edu/hitsz/
├── MainActivity.java          # 难度选择页
├── GameActivity.java          # 游戏承载页
├── GameDifficulty.java        # 难度参数与归一化工具
├── application/
│   ├── BaseGame.java          # SurfaceView 游戏主循环
│   ├── EasyGame.java          # 简单模式
│   ├── NormalGame.java        # 普通模式
│   ├── HardGame.java          # 困难模式
│   ├── ImageManager.java      # 图片资源管理
│   ├── Main.java              # 屏幕尺寸常量
│   └── gamemode/              # 游戏模式（简单/普通/困难）
├── aircraft/                  # 飞机实体（英雄机、敌机）
├── basic/                     # 基础抽象类
├── bullet/                    # 子弹
├── prop/                      # 道具
├── factory/                   # 工厂类
├── strategy/                  # 射击策略
└── observer/                  # 观察者模式
```

## 主要代码位置

- 难度选择页：`app/src/main/java/edu/hitsz/MainActivity.java`
- 游戏承载页：`app/src/main/java/edu/hitsz/GameActivity.java`
- SurfaceView 游戏页：`app/src/main/java/edu/hitsz/application/BaseGame.java`
- 难度参数定义：`app/src/main/java/edu/hitsz/GameDifficulty.java`
- 三种难度入口：`app/src/main/java/edu/hitsz/application/EasyGame.java`、`NormalGame.java`、`HardGame.java`
- 图片资源管理：`app/src/main/java/edu/hitsz/application/ImageManager.java`

## 构建验证

已通过命令行构建验证：

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

## 与课程实验安排的对应关系

根据当前三份实验 PDF，可对应为：

- 实验二重点是代码迁移，并在 Android Studio 模拟器中运行展示游戏界面
- 本仓库已完成实验二所需的核心迁移内容
- 实验三重点是难度选择页面、按钮监听、`Intent` 页面跳转和数据传递
- 本仓库已完成实验三所需的 XML 难度选择页与对应难度跳转逻辑
- 排行榜、音效/背景音乐、网络功能仍属于后续实验阶段内容

## 结合课程安排的后续方向

- 实验四：补充音效与背景音乐迁移
- 实验五：实现排行榜功能
- 实验六：继续开发网络功能
