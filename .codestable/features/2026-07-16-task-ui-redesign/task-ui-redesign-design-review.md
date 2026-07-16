---
doc_type: feature-design-review
feature: 2026-07-16-task-ui-redesign
status: changes-requested
reviewed: 2026-07-16
round: 1
---

# task-ui-redesign feature design 审查报告

## 1. Scope And Inputs

- Design: `.codestable/features/2026-07-16-task-ui-redesign/task-ui-redesign-design.md`
- Checklist: `.codestable/features/2026-07-16-task-ui-redesign/task-ui-redesign-checklist.yaml`
- Intent / brainstorm: none
- Roadmap: none
- Related docs: `需求文档.md`；无 requirements、architecture 或 compound 文档。
- Code facts checked: `ui/MainActivity.kt`、`ui/TaskViewModel.kt`、`data/TaskEntity.kt`、`domain/TaskSchedule.kt`。

### Independent Review

- Status: completed
- Detection: native-agent
- Provider / agent: `/root/design_reviewer`
- Raw output: 独立审查提出 3 个 blocking、3 个 important 与 1 个 nit；主 agent 已核验其与 design、checklist、现有 UI/执行入口一致。
- Merge policy: 已逐条核验；本报告不替用户批准 design。
- Gate effect: 用户必须明确未保存任务的测试执行语义，修订后必须再次独立审查。

## 2. Design Summary

- Goal: 将 Android 任务管理界面复刻为确认的深色命令工具体验，同时保留现有操作链路。
- Key contracts: 不改领域数据和执行链路；装配层持有 ViewModel；视觉对照以 `references/home.png`、`references/editor.png` 为准。
- Steps: 6 步；风险热点是未保存任务测试执行、小屏输入和环境缺 Java。
- Checks: 5 项，均映射到设计章节。
- Baseline / validation: Java/JAVA_HOME 缺失使单测和构建基线无法执行；恢复后必须跑单测和 Debug 构建。

## 3. Findings

### blocking

- [ ] FDR-002 `task-ui-redesign-design.md#2.2` 未保存任务的“立即执行”没有稳定行为契约。
  - Evidence: 现有 `TaskViewModel.executeNow(task)` 会执行并记录任务日志；未保存 `TaskEntity` 的 id 为 0。新参考图显示测试按钮，但需求文档只定义“每个任务”的立即执行。
  - Impact: 不能判断测试是否允许、是否写日志或是否需要先保存，因而不能实现且不违背“不伪造执行结果”。
  - Expected fix scope: 用户在“仅保存后可执行”和“保存前也可测试、但不写任务日志”中选择其一；选择后补齐按钮状态、结果反馈、日志去向与验收。

### important

- [ ] FDR-006 `task-ui-redesign-design.md#1` 当前环境没有 Java/JAVA_HOME；恢复环境后需执行两条核心命令并保存输出。
  - Evidence: `gradlew.bat testDebugUnitTest` 返回 “JAVA_HOME is not set and no 'java' command could be found”。
  - Impact: 目前无法取得构建和单测证据。

### nit

- [ ] none

### suggestion

- [ ] 日志页在实现前可补一张同风格参考图，以便同尺寸截图比对。

### learning

- `statusMessage` 只报告调度失败；立即执行的结果应从日志事实呈现，不能假定现有状态流会自动显示完成状态。

### praise

- 不改 `TaskEntity`、数据库、调度和 Shell/Root 逻辑的范围控制与现有实现一致。

## 4. User Review Focus

- 用户需要重点拍板：新建任务在首次保存前点击“立即执行”的行为。
- implement 需要重点遵守：视觉只以两张归档参考资产对照；所有领域操作继续通过既有 ViewModel。
- code review / QA / acceptance 需要重点复核：固定设备截图、软键盘、Root/权限、空/成功/失败日志，以及 Java 环境恢复后的构建证据。

## 5. Evidence Confidence Ledger

| Check | Verdict | Evidence Class | Basis | Follow-up |
| --- | --- | --- | --- | --- |
| Acceptance Coverage Matrix | warn | E | 执行测试语义未定 | 用户选择后补齐 |
| DoD Contract | pass | E | 设计与 checklist 都有 DoD 和命令 | 恢复 Java 后执行 |
| Steps and checks traceability | pass | E | 清单条目均指向设计章节 | 二审复核 |
| Roadmap contract compliance | n/a | E | 非 roadmap feature | none |
| Module interface design | pass | C | UI 仅重组，未新增跨模块接口 | 代码审查复核 |
| Validation and artifacts | warn | E | Java 环境阻塞，日志参考图尚未补齐 | 实现前补图/环境 |

Summary: E=5, C=1, H=0, H-only core checks=none。

## 6. Residual Risk

- Java 环境缺失，当前不能完成构建/单测基线复验。
- 真机 Root 授权、精确定时授权返回和软键盘行为须在设备/模拟器阶段验证。

## 7. Verdict

- Status: changes-requested
- Next: 等用户明确未保存任务的测试执行语义；修订 design/checklist 后重跑 `cs-feat` design-review 阶段。
