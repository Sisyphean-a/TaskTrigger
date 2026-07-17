---
doc_type: issue-fix
issue: 2026-07-17-command-editor-line-alignment
status: fixed
path: fast-track
fix_date: 2026-07-17
tags: [ui, compose]
---

# 命令编辑区行号错位修复记录

## 1. 问题描述

新建任务界面的命令编辑区中，前两行文字与左侧行号没有对齐。

## 2. 根因

行号使用 11sp 默认行高，占位文字使用 13sp 默认行高，实际输入文字使用 13sp、20sp 行高，三者排版参数不一致。

## 3. 修复方案

行号、占位文字和实际输入文字共用 13sp、20sp 的等宽字体样式，使每一行使用相同的高度和基线。

## 4. 改动文件清单

- `app/src/main/java/com/tasktrigger/ui/TaskEditorFields.kt`：统一命令编辑区文字样式。

## 5. 验证结果

- `git diff --check` 通过。
- `gradlew :app:compileDebugKotlin` 通过。
- 当前没有在线 Android 设备或已配置模拟器，未进行实机截图复核。

## 6. 遗留事项

- 需在 Android 设备上打开新建任务界面，最终确认不同系统字体缩放下的视觉效果。
