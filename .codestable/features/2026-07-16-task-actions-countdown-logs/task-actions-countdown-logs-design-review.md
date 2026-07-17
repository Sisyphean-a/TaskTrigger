---
doc_type: feature-design-review
feature: 2026-07-16-task-actions-countdown-logs
status: passed
reviewed: 2026-07-16
round: 3
---

# task-actions-countdown-logs feature design 审查报告

## 1. Scope And Inputs

- Design: `.codestable/features/2026-07-16-task-actions-countdown-logs/task-actions-countdown-logs-design.md`
- Checklist: `.codestable/features/2026-07-16-task-actions-countdown-logs/task-actions-countdown-logs-checklist.yaml`
- Intent / brainstorm: none
- Roadmap: none
- Related docs: `需求文档.md`；无 CONTEXT、ADR 或 compound 文档。
- Code facts checked: 任务实体与 Room、调度计算与 AlarmManager、ViewModel、定时/开机接收器、命令执行、首页/编辑/日志 Compose 页面、Gradle 配置。

### Independent Review

- Status: completed
- Detection: native-agent
- Provider / agent: `/root/design_reviewer`、`/root/design_reviewer_round2`、`/root/design_reviewer_round3`
- Raw output: 第一轮发现日志迁移真实性、Room 迁移、一次性终态和可注入编排 4 个阻塞项；第二轮发现原子领取与错误顺序 1 个阻塞项；修订后第三轮无 blocking/important，建议 passed。
- Merge policy: 每轮 finding 均按当前 design、checklist 与代码事实核验；阻塞项已写回设计契约并由下一轮独立复审。
- Gate effect: design-review 通过，但不替用户批准 design。

## 2. Design Summary

- Goal: 增加任务长按复制/删除、固定时间与一次性倒计时，并把应用可观测的调度、派发和命令失败写入丰富日志。
- Key contracts: 副本保存后默认关闭；倒计时仅启用时开始；一次性任务原子领取后执行并自动关闭；调度失败回退关闭；旧数据无损迁移且不伪造未知历史字段。
- Steps: 8 步，按统一编排、领域契约、迁移、调度、日志、首页、编辑、日志 UI/回归推进。
- Checks: 5 项，覆盖范围守护、名词、编排、挂载点和验收场景。
- Baseline / validation: 当前缺 Java/JAVA_HOME，单测与构建在 Gradle 启动前失败；迁移测试、单测、Debug 构建仍是完成前不可豁免证据。

## 3. Findings

### blocking

none

### important

none

### nit

- [ ] FDR-301 Acceptance Matrix 使用 S2～S8 按顺序引用 checklist，步骤没有显式 id；当前对应清楚，后续调整顺序时需同步矩阵。
- [ ] FDR-302 旧失败日志的 `reasonCode` 默认值需在实现时统一并由迁移测试锁定；不得伪造具体失败原因。

### suggestion

none

### learning

- 开机时发现一次性任务已经过期，是应用当下真实观察到的“恢复跳过”，可以记录；它不等同于声称 Android 曾投递或漏投闹钟。
- 一次性任务必须用数据库条件更新或等价事务原子领取，不能退化成先查再改。

### praise

- 复制只生成关闭态草稿，避免隐式创建重复闹钟。
- 对系统未唤醒应用的不可观测边界表述诚实，没有用推断日志冒充事实。
- 关停、删除和调度回退优先于日志写入，日志失败不会破坏安全状态。

## 4. User Review Focus

- 用户需要重点拍板：倒计时继续使用现有墙上时间语义，手动修改系统时间会改变剩余时长。
- implement 需要重点遵守：副本默认关闭；一次性任务原子领取；关闭态倒计时 `triggerAt = 0`；调度失败统一回退关闭；旧日志未知字段保持为空。
- code review / QA / acceptance 需要重点复核：并发重复投递、日志写入失败顺序、Room v1→v2 孤立日志迁移、超大输出与超时部分输出、真机接收器时限。

## 5. Evidence Confidence Ledger

| Check | Verdict | Evidence Class | Basis | Follow-up |
| --- | --- | --- | --- | --- |
| Acceptance Coverage Matrix | pass | E | 14 个场景均映射到步骤与证据 | 调整步骤顺序时同步编号 |
| DoD Contract | pass | E | 五阶段 DoD、三条核心命令和产物齐全 | 实现后补命令证据 |
| Steps and checks traceability | pass | E | 8 个 steps 与 5 个 checks 均可追溯 | none |
| Roadmap contract compliance | n/a | E | 非 roadmap feature | none |
| Module interface design | pass | C | TaskStore/Scheduler/CommandRunner/Clock/TaskOperations 边界及错误顺序明确 | 实现阶段核对原子领取 |
| Validation and artifacts | pass | E | 单测、迁移测试、构建、截图均已定义 | 当前 Java 环境需恢复 |

Summary: E=5, C=1, H=0, H-only core checks=none。

## 6. Residual Risk

- 倒计时使用 `RTC_WAKEUP` 墙上时间，用户修改系统时间或时区会改变剩余时长。
- 当前接收器命令预算为 8 秒，真机仍需验证临界超时下日志落盘和周期重排。
- 超时后需要等待输出读取任务收尾，避免丢失最后一段输出。
- 当前环境缺 Java/JAVA_HOME，尚无构建、单测和迁移测试运行证据。

## 7. Verdict

- Status: passed
- Next: 交给用户整体 review；用户明确放行后将 design 标记为 approved，再进入 goal package 与实现。
