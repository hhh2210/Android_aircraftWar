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

## 后续可扩展方向

- 增加普通模式、困难模式入口切换
- 补充排行榜页面与难度选择页面
- 迁移音效与背景音乐
- 适配更多 Android 界面交互与资源管理能力
