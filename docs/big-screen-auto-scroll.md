# 大屏自动滚动与虚拟列表能力沉淀

本文档沉淀来自两个已验证大屏页面的运行时经验：

- `DataPanel.vue`：复杂大屏 runtime，包含大数据量滚动、虚拟窗口、circular player、active/standby buffer、统一 `requestAnimationFrame` 调度、`translate3d`、hover/visibility pause、无感刷新。
- `InspectionBoard.vue`：轻量大屏 runtime，包含双列任务卡片流、Hero 与列表分离、双份内容无缝循环、placeholder 补位、`translate3d` offset、自动慢滚、hover pause、数据刷新无感。

当前阶段是能力沉淀阶段，不抽 UI 组件，不改业务页面，不建立万能大屏组件。

## 目录结构建议

```text
common-capability/
  docs/
    big-screen-auto-scroll.md
  runtime/
    big-screen-runtime.mjs
  examples/
```

说明：

- `docs/` 优先沉淀设计规则、验收标准、反例和边界。
- `runtime/` 只放 headless helper，不依赖 Vue、DOM 结构、业务字段和样式。
- `examples/` 后续只放最小使用示例，不复制业务页面。

## 当前建议沉淀的文件

| 文件 | 作用 | 来源页面 | 是否立即实现 |
| --- | --- | --- | --- |
| `docs/big-screen-auto-scroll.md` | 大屏自动滚动、虚拟列表、双列卡片流、刷新策略设计规则 | `DataPanel.vue`、`InspectionBoard.vue` | 是 |
| `runtime/big-screen-runtime.mjs` | 最小 headless runtime helper：rAF loop、loop offset、固定高度虚拟窗口、行切分、placeholder 补位 | `DataPanel.vue`、`InspectionBoard.vue` | 是 |
| `examples/auto-scroll-basic.md` | 轻量接入示例，只展示 offset/transform/cleanup | 后续从 runtime 反推 | 否 |
| `examples/fixed-virtual-window.md` | 固定高度虚拟窗口示例 | 后续从 runtime 反推 | 否 |

## 当前不应该抽的东西

以下内容属于业务页面或 UI 层，不应进入 common-capability：

- `BigScreenAutoScrollList.vue`、`VirtualAutoScrollList.vue`、大屏卡片组件、表格组件。
- DataPanel 的 KPI、生产线、交付预测、采购风险、在制品业务字段。
- InspectionBoard 的报检状态、等待时长、任务卡视觉样式、Hero 展示逻辑。
- 页面接口调用、接口 DTO、排序口径、状态颜色、业务编号生成规则。
- 业务侧刷新频率、权限、路由、菜单、页面布局。
- 任何带具体业务字段名的 mapper、formatter、normalizer。

common-capability 只沉淀运行时原则和通用计算工具。

## 大屏自动滚动设计原则

大屏自动滚动的目标不是模拟用户滚动，而是稳定地驱动视觉位移。

推荐模型：

1. 用 `requestAnimationFrame` 驱动 runtime offset。
2. 用 `translate3d(0, -offset, 0)` 应用视觉位移。
3. 用业务页面或渲染层决定 offset 如何绑定到具体 DOM。
4. hover、页面隐藏、销毁时停止 runtime。
5. 数据刷新时优先保留 offset，并在安全时机切换数据。

不推荐把滚动能力绑定到具体组件。自动滚动 runtime 只应该关心：

- 当前 offset。
- 每毫秒速度。
- 每帧 delta。
- 是否暂停。
- 是否循环。
- 循环高度。
- 销毁清理。

## 为什么优先 translate3d

`translate3d` 更适合大屏挂屏滚动：

- 只改变合成层 transform，减少布局和重排压力。
- 更容易把 runtime offset 与虚拟窗口拆开。
- 可以让滚动条、DOM pool、双份内容循环分别独立控制。
- 对长时间挂屏更稳定，不依赖浏览器原生滚动位置的细碎副作用。

使用规则：

- runtime 维护数值 offset。
- 渲染层把 offset 映射为 `transform: translate3d(0, -offsetpx, 0)`。
- 不在每帧读写布局属性。
- 测量高度只在初始化、数据变更、resize 后进行。

## 为什么避免高频 scrollTop

不建议高频写 `scrollTop`：

- `scrollTop` 更容易触发布局相关计算。
- 用户滚动、浏览器滚动恢复、滚动条状态与自动滚动会互相抢控制权。
- 高频写入会让虚拟窗口边界和 DOM pool 回收更难稳定。
- 页面隐藏后恢复时，`scrollTop` 状态容易和逻辑 offset 脱节。
- 多区域同时滚动时，多个 `scrollTop` 写入会放大主线程压力。

`scrollTop` 可以用于用户手动滚动的读取或兼容入口，但不要作为大屏自动播放的主时钟。

## rAF delta clamp

每帧 delta 必须 clamp，例如上限 `32ms`。

原因：

- 页面切到后台、系统卡顿、DevTools 暂停后，下一帧 timestamp 可能跳很大。
- 不 clamp 会导致 offset 瞬移，直接跨过回收点、循环点或数据切换点。
- 虚拟窗口和 circular player 通常依赖逐步推进，突跳会造成空白、错位或重复行。

规则：

- `delta = min(timestamp - lastTimestamp, 32)`。
- 首帧只记录 timestamp，不推进或只推进 `0`。
- 恢复自动滚动时清空 `lastTimestamp`，避免把暂停时间算进位移。

## hover / visibility / destroy 规则

hover pause：

- 鼠标进入可读区域时暂停。
- 鼠标离开后延迟恢复。
- hover 恢复延迟可以短，例如 `500ms`。

manual pause：

- wheel、touch、pointer 操作应视为用户接管。
- 恢复延迟应长于 hover，例如 `1500ms`。
- 恢复前要同步 runtime offset，避免视觉跳动。

visibility pause：

- `document.hidden` 时暂停全部 runtime。
- 页面恢复可见后短延迟恢复，例如 `500ms`。
- 恢复时重置 `lastTimestamp`，不补算隐藏期间 delta。

destroy cleanup：

- 取消所有 `requestAnimationFrame`。
- 清理 resume timer。
- 移除 `visibilitychange`、resize、pointer/wheel 等监听。
- 清空 active runtime 集合。
- 不在销毁后继续写 transform。

## 双份内容循环规则

轻量列表可以使用双份内容循环：

1. 渲染两份相同内容。
2. 只测量第一份内容高度作为 `loopHeight`。
3. runtime offset 在 `[0, loopHeight)` 内循环。
4. transform 始终使用 `-offset`。

注意：

- 只有内容高度大于视口高度时才滚动。
- 如果数据不足一屏，不启动循环，并把 offset 归零。
- 数据变化后先归一化 offset：`offset % loopHeight`。
- loopHeight 必须来自单份内容，不是双份总高度。

## 虚拟列表 Runtime

虚拟列表只负责控制 DOM 数量，不负责自动滚动。

固定高度虚拟窗口规则：

- 必须有稳定 `itemHeight`。
- `totalHeight = totalItems * itemHeight`。
- `startIndex = floor(offset / itemHeight) - bufferCount`。
- `endIndex = startIndex + visibleCount + bufferCount * 2`。
- `offsetY = startIndex * itemHeight`。
- 渲染层使用 spacer 撑总高度，再使用 content transform 对齐窗口。

虚拟列表应输出：

- `totalItems`
- `totalHeight`
- `visibleItems`
- `startIndex`
- `endIndex`
- `offsetY`
- `virtualOffset`
- `maxOffset`

不要在虚拟列表里处理：

- 自动滚动速度。
- hover pause。
- 接口刷新。
- DOM 样式。
- 业务字段转换。

## 自动滚动与虚拟列表必须分离

自动滚动关心时间和 offset；虚拟列表关心 offset 下应该渲染哪一段数据。

正确组合方式：

```text
rAF runtime -> offset -> getFixedVirtualWindow -> render visibleItems + transform
```

错误方式：

```text
virtual list 内部自己启动定时器
virtual list 内部直接写 DOM
virtual list 内部决定接口刷新和业务切换
```

分离后可以：

- 复用同一个 rAF scheduler 驱动多个区域。
- 在每帧只更新 runtime 数值。
- 限制虚拟窗口更新频率，避免每个区域每帧都重算。
- 数据刷新时保留 offset。

## Circular Player / DOM Pool

复杂大屏大数据量滚动可以使用 circular player：

- DOM pool 固定大小。
- 每次跨过一个 item height，只回收一个槽位。
- `cursor` 表示数据游标。
- `positionCursor` 表示视觉位置游标。
- `visualOffset` 表示当前 item 内的细粒度位移。
- 回收时把剩余 offset 带到下一轮。

适用场景：

- 数据量大。
- 多列表同时滚动。
- 固定高度可靠。
- 希望每帧不重建完整 visible window。

不适用场景：

- 卡片高度不稳定。
- 数据很少。
- 只是双列慢速循环。
- 业务页面还未稳定。

## 多列卡片流规则

多列卡片流适合轻量大屏：

1. 先按列数切行：`chunkRows(rows, columns)`。
2. 最后一行不足列数时补 placeholder。
3. placeholder 必须占位但不可见。
4. 双份循环时复制的是行集合，不是随意复制单卡。
5. 编号展示根据原始数据下标回绕，不让第二份内容继续累加。
6. grid 行高、列数、间距必须稳定。

placeholder 规则：

- placeholder 只用于布局补位。
- 不参与业务统计。
- 不参与点击和状态展示。
- 可用 `visibility: hidden`，但仍保留尺寸。

## 数据刷新策略

大屏刷新要避免闪烁和粗暴 reset。

推荐 active/standby buffer：

- `activeBuffer`：当前正在展示的数据。
- `standbyBuffer`：新拉取的数据。
- `pendingSwitch`：等待切换标记。

切换规则：

- 初次加载可以先渲染少量数据，再延迟切入完整数据。
- 定时刷新先写入 standby。
- 如果当前列表不可循环且用户未交互，可以立即切换。
- 如果正在循环滚动，优先等到循环边界再切换。
- 切换后保留 offset，或在循环边界归零。
- 不要在任意刷新完成时清空列表再填充。

轻量列表策略：

- 新旧数据相同时不替换数组。
- 数据变更后同步 loop offset：`offset % loopHeight`。
- 不把刷新成功与滚动 reset 绑定。

## 验收标准

文档和 runtime 能力验收：

- 文档明确区分自动滚动、虚拟列表、数据刷新、多列卡片流。
- 文档写明不能抽 UI 组件和不能混入业务字段。
- runtime 不依赖 Vue。
- runtime 不直接读写 DOM。
- runtime 不包含业务字段。
- runtime helper 能被页面按需组合，而不是强制接管页面。

页面应用验收：

- 自动滚动长时间运行不闪烁、不空白、不突然跳大段。
- hover 后暂停，离开后恢复。
- 页面隐藏后暂停，恢复后不补算隐藏期间位移。
- 数据刷新不清屏，不粗暴归零。
- 双份循环到边界无明显断点。
- 虚拟列表 DOM 数量受控。
- 小屏或远距离观看时文字和卡片高度稳定。

## 常见 bug

- 页面后台恢复后瞬间跳过多行：没有 clamp delta 或恢复时没有重置 timestamp。
- 循环边界闪一下空白：loopHeight 取了双份总高度或取值时机不对。
- 数据刷新时闪烁：直接清空 active rows 后再填充。
- hover 后继续滚：只暂停了 CSS animation，没有暂停 runtime。
- 多个滚动区卡顿：每个区域单独 rAF，并且每帧写响应式状态。
- 虚拟列表错位：itemHeight 不稳定，或 content transform 没扣掉 virtualOffset。
- 最后一行单卡拉伸：双列卡片流没有 placeholder 补位。
- 列表越滚越快：暂停期间的 timestamp 被计入下一帧 delta。
- 销毁后报错：rAF/timer/listener 没清理。

## 常见错误方案

- 抽一个万能 Vue 大屏组件。
- 把业务字段、接口刷新、状态颜色放进 runtime。
- 用 `setInterval` 做主滚动时钟。
- 高频写 `scrollTop` 驱动自动滚动。
- 每帧读 DOM 高度再写 transform。
- 刷新数据时直接 reset offset。
- 把自动滚动和虚拟列表做成一个不可拆的黑盒。
- 为了兼容不固定高度，牺牲虚拟列表稳定性。
- 在 common-capability 中复制 DataPanel 或 InspectionBoard 的大段业务代码。

## 性能注意事项

- 一个页面优先共享一个 rAF scheduler。
- 每帧只推进 runtime 数值和必要 transform。
- DOM 测量集中在 mount、resize、数据切换后的安全时机。
- 大数据量列表优先固定 item height。
- 对多个虚拟区域进行更新节流，例如一帧只消费一个重窗口更新。
- 热路径不要写 Vue 响应式状态。
- 数据预处理放在刷新完成或切换前，不放在每帧。
- transform 使用 `translate3d`，避免 layout 相关属性。

## 后续演进路线

阶段 1：文档沉淀。

- 固化设计原则、边界、验收标准、常见 bug。
- 明确哪些能力可以进入 common-capability，哪些留在业务页面。

阶段 2：runtime helper。

- `createRafLoop`
- `normalizeLoopOffset`
- `getFixedVirtualWindow`
- `chunkRows`
- `padRowsWithPlaceholder`

阶段 3：headless runtime。

- `createAutoScrollRuntime`
- circular player 状态机 helper。
- active/standby buffer 切换策略 helper。

阶段 4：renderless component。

- 只有当多个业务页面重复接入、API 已稳定、运行时规则被反复验证后再考虑。
- renderless 仍不包含 UI、样式、业务字段和接口调用。

