# task-actions-countdown-logs Goal Protocol

1. 读取 design、checklist、goal-plan 和 goal-state，以仓库事实恢复阶段。
2. 进入 `cs-feat` implementation，按 checklist 完成实现。代码行为默认执行
   RED -> GREEN -> VERIFY；例外必须记录 `TDD exception` 和替代证据。
3. 每完成一个步骤，立即更新 checklist 状态和 goal-state ledger。
4. 运行 implementation gates，生成 evidence pack、gate result 和 DoD 结果；通过后写
   `stage: review`、`status: ready`。
5. 使用独立 Task agent 执行 `cs-code-review`。若有 blocking，写 `review/fixing`，修复并重审。
6. review passed 后写 `qa/ready` 并执行 QA；失败则写 `qa/fixing`，修复后重新跑 review 与 QA。
7. QA passed 后写 `acceptance/ready`，逐项反查代码、数据库、界面和 checklist checks。
8. acceptance passed 且无 handoff 时，先写 `stage: complete`、`status: passed`，再输出
   `CS_FEATURE_GOAL_COMPLETE`。

Goal 模式接管普通阶段 checkpoint；只有命中下列条件才停止：

- 需要修改已批准设计、feature 范围或公开契约。
- 独立 Task agent reviewer pending/failed/blocked，且用户未批准降级。
- 同一失败项连续三轮修复仍不通过。
- 外部凭证或环境缺失导致核心行为无法判断。
- 用户要求暂停、改方向或终止。

handoff 前必须先写 `stage: handoff`、`status: blocked`、`handoff_reason` 和
`handoff_next`，再输出：

```text
CS_FEATURE_GOAL_HANDOFF
Reason: <具体阻塞>
Next: <建议动作>
```

不得绕过 TDD policy。缺少 RED/GREEN/VERIFY 证据且没有 `TDD exception` 时，实现 gate
不通过。阶段状态变化必须立即写回；续跑以 ledger、产物和 git 历史为准，不重复已完成步骤。
