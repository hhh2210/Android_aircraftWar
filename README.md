# Android Aircraft War

这是一个将 Windows 版 Java 飞机大战项目迁移到 Android 平台的课程实验项目。

## 项目说明

- 课程：软件构造实践 实验二
- 目标：将原有 Windows/Swing 版本迁移到 Android
- 当前实现：已完成基于 `SurfaceView` 的游戏主界面迁移，可在 Android Studio 模拟器中运行

## 已完成内容

- 使用 `MainActivity + SurfaceView` 作为 Android 入口
- 将游戏主循环重构为 `SurfaceView` 绘制线程
- 使用触摸事件控制英雄机移动
- 使用 `BitmapFactory` 从 `res/drawable` 加载图片资源
- 迁移飞机、子弹、道具、工厂、策略、观察者等核心逻辑代码
- 默认加载简单模式 `EasyGame`

## 运行方式

1. 使用 Android Studio 打开本项目
2. 等待 Gradle Sync 完成
3. 选择一个 Android 模拟器或真机
4. 点击 Run 运行应用

启动后即可看到游戏界面，并通过拖动控制英雄机。

## 主要代码位置

- 入口 Activity：`app/src/main/java/edu/hitsz/MainActivity.java`
- SurfaceView 游戏页：`app/src/main/java/edu/hitsz/application/BaseGame.java`
- 简单模式入口：`app/src/main/java/edu/hitsz/application/EasyGame.java`
- 图片资源管理：`app/src/main/java/edu/hitsz/application/ImageManager.java`

## 构建验证

已通过命令行构建验证：

```bash
./gradlew assembleDebug
```

## 与课程实验安排的对应关系

根据课程 PDF 当前阶段与总体安排：

- 实验二重点是代码迁移，并在 Android Studio 模拟器中运行展示游戏界面
- 本仓库当前已完成实验二所需的核心迁移内容
- `排行榜页面`、`难度选择页面`、`音效/背景音乐` 等内容在实验材料中属于后续待完善或后续实验阶段内容

## 结合课程安排的后续方向

- 实验三：完善界面相关功能
- 实验四：补充音效与背景音乐迁移
- 实验五：实现排行榜功能
- 实验六：继续开发网络功能
