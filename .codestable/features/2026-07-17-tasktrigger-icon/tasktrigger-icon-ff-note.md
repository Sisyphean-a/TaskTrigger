---
doc_type: feature-ff-note
feature: tasktrigger-icon
date: 2026-07-17
requirement:
tags: [android, branding, icon]
---

## 做了什么

为 TaskTrigger 增加了自有 Android 应用图标。图标沿用应用橙黑配色，以终端提示符和触发执行竖条表达“定时执行 Shell 命令”。

## 改了哪些

- `app/src/main/AndroidManifest.xml` — 配置普通与圆形启动器图标入口。
- `app/src/main/res/values/colors.xml` — 增加图标背景色。
- `app/src/main/res/drawable/ic_launcher_foreground.xml` — 增加矢量前景符号。
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` — 增加自适应图标。
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml` — 增加圆形自适应图标。

## 怎么验证的

`gradlew :app:assembleDebug` 和 `git diff --check` 通过。当前没有在线 Android 设备或已配置模拟器，未进行启动器截图验证。

## 顺手发现（可选，不阻塞）

- 无。
