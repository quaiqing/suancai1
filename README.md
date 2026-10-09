# 坐标辅助线

Android 悬浮辅助线，所有参数和服务操作均在 App 内完成，不使用 Root、Shell、广播控制器或其他外部控制程序。

## 功能

- 设置起点和终点坐标
- 设置颜色、粗细和透明度
- 保存并显示、应用设置、隐藏和停止悬浮
- 使用不可触摸悬浮层，尽量不影响游戏操作

## 使用 GitHub Actions 打包

1. 在 GitHub 新建一个仓库。
2. 将本目录中的全部文件和隐藏的 `.github` 目录上传到仓库根目录。
3. 打开仓库的 **Actions** 页面。
4. 选择 **Build Android APK**，点击 **Run workflow**。
5. 等待工作流变成绿色成功状态。
6. 打开本次运行页面，在 **Artifacts** 中下载 `CoordLine-debug-apk`，解压后即可得到 `app-debug.apk`。

也可以把项目推送到 `main` 或 `master` 分支，工作流会自动构建。

## 本地编译

使用兼容 Android Gradle Plugin 8.7.3 的 Gradle 8.9 和 JDK 17，安装 Android SDK Platform 35，然后在本目录执行：

```bash
gradle assembleDebug
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 首次使用

安装后打开 App，授权“显示在其他应用上层”，输入参数并点击“保存并显示”。不需要 Root。

## 注意

坐标单位为屏幕像素，原点位于左上角。状态栏、导航栏、游戏画面区域可能影响线条相对位置。Android 12 及以上对触摸穿透有系统限制，部分系统或游戏可能不允许悬浮层触摸穿透。
