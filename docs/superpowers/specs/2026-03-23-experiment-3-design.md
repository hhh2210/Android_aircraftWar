# 实验三设计说明

## 目标

- 为 Android 版飞机大战增加一个难度选择页面。
- 用户点击难度按钮后，通过 `Intent` 跳转到统一的游戏页面。
- 在跳转时传递难度参数，并由目标页面按参数加载对应难度的游戏场景。

## 现状

- `MainActivity` 当前直接 `setContentView(new EasyGame(this))`，启动后立即进入简单模式。
- `AndroidManifest.xml` 目前只注册了 `MainActivity`。
- `app/src/main/res/layout/activity_main.xml` 还是模板布局，尚未承担实验三页面职责。
- 游戏模式层已经有 `EasyMode`、`NormalMode`、`HardMode`，但页面入口还没有把这三种模式接入 Android 页面跳转流程。

## 方案选择

### 方案 A（采用）

- `MainActivity` 作为难度选择页。
- 新增一个统一的 `GameActivity`。
- `MainActivity` 通过 `Intent.putExtra()` 传递难度参数给 `GameActivity`。
- `GameActivity` 读取参数后，创建对应难度的游戏 View。

采用原因：

- 最贴合实验三对页面开发、事件监听、`Intent` 页面跳转、数据传递的要求。
- 比为每个难度单独建一个 Activity 更简洁，避免重复代码。
- 不需要改动 `BaseGame` 主循环，风险小，便于在现有工程上平滑演进。

### 备选方案 B（未采用）

- 继续只保留一个 `MainActivity`，点击按钮后在当前 Activity 内直接切换不同游戏 View。

未采用原因：

- 无法清晰体现“页面跳转”的实验重点。
- `Intent` 数据传递会被弱化，不适合作为实验三的标准实现。

### 备选方案 C（未采用）

- 为简单、普通、困难分别创建三个独立的 Activity。

未采用原因：

- 能完成功能，但会引入重复的 Activity 启动和分发逻辑。
- 相比统一的 `GameActivity`，可维护性更差，后续扩展不划算。

## 页面与类拆分

### `MainActivity`

- 改为加载 `activity_main.xml`。
- 负责显示实验三入口页面。
- 通过按钮监听响应用户选择。

页面内容保持正式实验作业风格：

- 页面标题，例如“Aircraft War”。
- 副标题或说明文字，例如“请选择游戏难度”。
- 三个难度按钮：Easy、Normal、Hard。
- 布局采用 XML 静态声明，突出实验三中的页面开发方式。
- 按钮文案和提示文字优先放入 `strings.xml`，保持 Android 资源组织方式一致。

### `GameActivity`

- 新增统一游戏页。
- 继承 `AppCompatActivity`，与当前 `MainActivity` 保持一致。
- 在 `onCreate()` 中读取 `Intent` 传入的难度参数。
- 根据难度参数创建对应的游戏 View，并通过 `setContentView()` 展示。
- `GameActivity` 无需新增 XML 布局，继续沿用当前游戏页面以程序化方式挂载 `BaseGame`/`SurfaceView` 的模式。

### 游戏包装类

- 保留现有 `EasyGame`。
- 新增 `NormalGame`、`HardGame` 两个轻量包装类。
- 三个类都只负责把对应的游戏模式对象传给 `BaseGame`，保持现有结构一致。

### Manifest

- 在 `AndroidManifest.xml` 中新增 `GameActivity` 注册。

## 数据流设计

### 难度常量

- 在 `GameActivity` 中集中定义：
  - `EXTRA_DIFFICULTY`
  - `DIFFICULTY_EASY = "easy"`
  - `DIFFICULTY_NORMAL = "normal"`
  - `DIFFICULTY_HARD = "hard"`

这样可以避免字符串字面量散落在多个类里，减少拼写错误风险。

### 跳转流程

1. 用户启动应用，进入 `MainActivity`。
2. 用户点击某个难度按钮。
3. `MainActivity` 创建 `Intent` 指向 `GameActivity`。
4. `MainActivity` 通过 `putExtra(EXTRA_DIFFICULTY, ...)` 写入难度值。
5. 调用 `startActivity(intent)` 完成页面跳转。
6. `GameActivity` 在 `onCreate()` 中读取 extra。
7. `GameActivity` 根据难度值创建对应游戏 View。
8. `GameActivity` 调用 `setContentView()` 显示游戏场景。

## 监听与分发逻辑

### `MainActivity` 的监听职责

- 通过 `findViewById()` 获取三个按钮。
- 每个按钮绑定点击监听。
- 监听内部只做一件事：触发跳转并传入难度值。

为了避免重复代码，推荐提取一个私有方法，例如：

- `launchGame(String difficulty)`

该方法负责统一构造 `Intent`、写入参数、启动页面。

### `GameActivity` 的分发职责

- 提供一个小型分发方法，例如：
  - `createGameViewByDifficulty(String difficulty)`
- 根据参数返回：
  - `EasyGame`
  - `NormalGame`
  - `HardGame`

这样可以把 Activity 生命周期逻辑与难度分发逻辑分开，让代码更清晰。

## 异常与兜底处理

- 如果 `Intent` 中没有传难度参数，默认回退到 `EasyGame`。
- 如果参数值不合法，也默认回退到 `EasyGame`。

采用默认回退而不是直接抛错，原因是：

- 实验演示场景下更稳，不会因参数问题直接崩溃。
- 与当前工程“可启动、可运行”的目标更一致。

## 返回行为

- `GameActivity` 不额外定制返回逻辑。
- 用户按系统返回键时，回到 `MainActivity` 的难度选择页面。

这样实现简单、稳定，也符合实验场景中“选择难度 -> 进入游戏 -> 返回重新选择”的自然流程。

## 测试与验收标准

### 功能验收

- 启动应用后，首先看到难度选择页面，而不是直接进入游戏。
- 点击 Easy 按钮，进入简单模式游戏场景。
- 点击 Normal 按钮，进入普通模式游戏场景。
- 点击 Hard 按钮，进入困难模式游戏场景。
- 从游戏页返回后，能够重新看到难度选择页面。

### 实验目标验收

- 难度选择页使用 XML 静态布局实现。
- 使用按钮点击监听处理用户事件。
- 使用 `Intent` 完成页面跳转。
- 使用 `putExtra()` 和目标页读取 extra 展示数据传递。
- 游戏页继续按现有 `SurfaceView` 方案动态加载对应游戏 View，不要求改造成 XML 页面。

### 工程验证

- 至少执行 `./gradlew assembleDebug`，确保工程可以成功构建。
- 如有设备或模拟器，补一次手工点击冒烟测试，验证三种难度跳转路径。

## 预期改动文件

- 修改 `app/src/main/java/edu/hitsz/MainActivity.java`
- 新增 `app/src/main/java/edu/hitsz/GameActivity.java`
- 新增 `app/src/main/java/edu/hitsz/application/NormalGame.java`
- 新增 `app/src/main/java/edu/hitsz/application/HardGame.java`
- 修改 `app/src/main/res/layout/activity_main.xml`
- 视需要新增字符串资源到 `app/src/main/res/values/strings.xml`
- 修改 `app/src/main/AndroidManifest.xml`

## 不在本次实验范围内

- 不重构 `BaseGame`、`HeroAircraft` 等核心运行逻辑。
- 不改造为多 Activity 多套游戏主循环。
- 不引入新的框架或大型依赖。
- 不额外扩展排行榜、暂停菜单、设置页等功能。
