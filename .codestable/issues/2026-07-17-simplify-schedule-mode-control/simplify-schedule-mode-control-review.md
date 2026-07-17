---
doc_type: issue-review
issue: 2026-07-17-simplify-schedule-mode-control
status: passed
reviewer: subagent+ocr
reviewed: 2026-07-17
round: 2
---

# 时间模式控件代码审查报告

## 1. Scope And Inputs

- Fix note: `simplify-schedule-mode-control-fix-note.md`
- Implementation evidence: 本轮对话、单元测试和 Lint 结果
- Diff basis: `TaskScheduleFields.kt` 与本 issue 记录
- Baseline dirty files: none

### Independent Review

- Detection: 原生独立审查代理与 OCR CLI 均可用
- 环节 A 独立隔离 Task agent: native-agent + completed
- 环节 B OCR CLI: completed
- Merge policy: 两个环节结果均已核验后合并
- Gate effect: none

## 2. Diff Summary

- 新增：本 issue 修复记录和审查报告
- 修改：`TaskScheduleFields.kt`
- 删除：none
- 未跟踪 / staged：issue 记录未跟踪；无 staged 文件
- 风险热点：用户可见 UI、触控与无障碍语义

## 3. Adversarial Pass

- 假设的生产 bug：视觉变紧凑后点击区和无障碍语义退化
- 主动攻击过的反例：48dp 触控目标、单选组语义、大字体换行、窄屏和状态切换
- 结果：发现三项 important

## 4. Findings

### blocking

none

### important

- [x] REV-001 `TaskScheduleFields.kt:59` 可点击区域仅 36dp 高，低于 48dp 触控目标。视觉层保持 36dp，点击层已扩至 48dp。
- [x] REV-002 `TaskScheduleFields.kt:59` 父容器缺少 `selectableGroup` 单选组语义。已补齐。
- [x] REV-003 `TaskScheduleFields.kt:81` 文案未显式锁定单行，大字体下可能换行或裁切。已锁定单行并调整为 13sp。

### nit

- import 顺序可整理，不影响行为。

### suggestion

none

### learning

- 紧凑视觉高度和可访问点击高度应分层处理。

### praise

- 状态切换与 `repeatDays` 清空逻辑保持不变，主要视觉方案已经落地。

## 5. Test And QA Focus

- QA 必须重点复核：触控范围、单选组语义、大字体单行、切换行为
- Evidence pack residual risks / gate warnings：固定态与倒计时态实机截图已通过
- 建议新增或加强的测试：none
- 不能靠 review 完全确认的点：真实设备点击手感与字体缩放效果

## 6. Residual Risk

- 当前设备实机验收通过；极端字体缩放和超窄屏幕仍未覆盖。

## 7. Verdict

- Status: passed
- Next: issue 修复收尾
