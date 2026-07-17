# task-actions-countdown-logs Goal Plan

## 输入

- Feature: `2026-07-16-task-actions-countdown-logs`
- Design: `.codestable/features/2026-07-16-task-actions-countdown-logs/task-actions-countdown-logs-design.md`
- Checklist: `.codestable/features/2026-07-16-task-actions-countdown-logs/task-actions-countdown-logs-checklist.yaml`
- Design review: `.codestable/features/2026-07-16-task-actions-countdown-logs/task-actions-countdown-logs-design-review.md`
- Owner approval: 2026-07-16，用户明确要求实现当前未提交方案。

## 执行策略

按 checklist 的 8 个步骤顺序推进。涉及代码行为的步骤默认执行 RED -> GREEN -> VERIFY
微循环；无法先写失败测试时，必须记录 `TDD exception` 和可复现的替代证据。每完成一步，
立即更新 checklist 和 `goal-state.yaml` ledger。

## 必跑验证

- `gradlew.bat testDebugUnitTest`
- `gradlew.bat connectedDebugAndroidTest`
- `gradlew.bat assembleDebug`

核心验收覆盖：首页长按复制/删除、固定时间兼容、倒计时状态转移、Room v1 到 v2 无损迁移、
失败与跳过日志、命令超时和长输出、重复投递原子领取、日志失败时的状态顺序。

## Gate 与交接

实现 gate 通过后依次进入独立代码审查、QA 和 acceptance。spec 合规与代码质量必须分别通过。
只有需要修改已批准设计/范围/公开契约、独立审查不可用、同一失败三轮仍未解决、或缺少外部环境
导致核心行为无法判断时，才写入 handoff；其余失败在 goal loop 内修复并重验。
