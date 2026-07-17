---
doc_type: issue-fix
issue: 2026-07-17-simplify-schedule-mode-control
status: fixed
path: fast-track
fix_date: 2026-07-17
tags: [ui, compose]
---

# 时间模式控件风格不一致修复记录

## 1. 问题描述

编辑任务界面的时间模式控件使用大圆角、紫色选中态和换行文案，与页面现有橙黑视觉体系不一致。

## 2. 根因

该区域直接使用 Material 3 默认分段按钮，没有套用项目自有的颜色、尺寸和圆角规范。

## 3. 修复方案

替换为 164×36dp 的项目内双段选择器，使用 4dp 圆角、深灰边框和橙色选中态文字；文案简化为“固定 / 倒计时”，删除勾选图标。

## 4. 改动文件清单

- `app/src/main/java/com/tasktrigger/ui/TaskScheduleFields.kt`：重做时间模式选择器外观，保留原有状态逻辑和单选语义。

## 5. 验证结果

- `gradlew :app:testDebugUnitTest` 通过。
- `gradlew :app:lintDebug` 通过。
- `git diff --check` 通过。
- 已在设备 `24122RKC7C` 上覆盖安装 Debug APK，固定态与倒计时态截图复核通过。
- 点击两个选项均能切换，倒计时字段按状态正确显示；语义节点点击高度约为 48dp。

## 6. 遗留事项

- 极端字体缩放和超窄屏幕未覆盖。
