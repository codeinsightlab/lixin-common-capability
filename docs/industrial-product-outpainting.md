# 工业产品图 Outpainting Pipeline

本文档沉淀制造业数字展厅产品图的宽屏素材生产流程。

当前阶段只沉淀经验、规则和流程，不实现自动化 AI pipeline，不封装 SDK，不抽前端组件。

核心结论：

```text
AI 不可信主体。
AI 只负责环境扩展。
最终产品主体必须来自原图，并通过工程化方式覆盖回去。
```

## 1. 问题背景

在工业大屏和数字展厅页面中，产品中心模块通常是横向宽屏区域，而产品素材经常来自偏竖构图或接近普通横图比例的渲染图。

以 `dashboard/data-panel` 的产品中心为例：

- 页面产品中心区域偏宽屏。
- 原始产品图比例偏竖。
- `object-fit: contain` 可以保留产品完整，但左右会留黑或留空。
- `object-fit: cover` 可以填满容器，但会裁切产品、边框或展台。
- `scale` 放大可以减少空白，但会让产品贴边、裁切或破坏主体尺度。
- 拉伸图片会直接破坏产品比例和机械结构可信度。

因此，普通前端适配不够。前端只能决定怎么展示已有素材，不能把不匹配的素材变成合格的宽屏展厅图。

正确边界是：

```text
素材层负责比例修复。
前端层负责稳定展示。
```

## 2. 错误方案

### AI 重生成宽图

错误方向：

```text
generate a new wide showroom image
```

问题：

- 产品会被 AI 改写。
- 金属结构、螺栓、孔位、装配关系可能变化。
- 产品比例和排列可能漂移。
- 展台、边框和灯光风格可能被重建。
- 工业质感可能下降，产品可信度被破坏。

工业产品图不是普通装饰图。产品主体是资产本身，不能交给 AI 自由重画。

### AI 重设计展厅

错误方向：

```text
redesign the showroom
```

问题：

- 原有蓝色工业数字展厅语言会漂移。
- 光向、地面反射、空间透视可能断裂。
- 新增 HUD、霓虹、文字、复杂 UI 后会抢产品主体。
- 最终像新图，不像原图自然变宽。

### object-fit: cover

问题：

- 会裁切产品主体。
- 会裁切发光边框、展台或机械边缘。
- 产品中心会从“产品 Spotlight”变成“背景图铺满”。

### scale 放大

问题：

- 放大会制造新的裁切风险。
- 不同素材主体大小不一致时，统一 scale 无法解决根因。
- 产品容易贴边或显得“怼脸”。

### 拉伸图片

问题：

- 直接破坏产品比例。
- 金属件、轴孔、螺栓会变形。
- 工业产品的真实性和严肃感会明显下降。

## 3. 正确 Pipeline

### Step 1: 原图保持不变

把原图作为 locked content。

要求：

- 原图不缩放。
- 原图不裁切。
- 原图不拉伸。
- 原图不重绘。
- 产品主体、展台、灯光、边框、地面反射都保持原样。

### Step 2: AI 只扩展左右环境

AI 只用于生成左右延展环境，不允许接管产品主体。

正确方向：

```text
continue existing environment
```

错误方向：

```text
generate new showroom
```

AI 应延续：

- 蓝色工业数字展厅背景。
- 科技地面。
- 能量纹理。
- 发光轨道。
- 工业空间纵深。
- 环境雾气。
- 数字光流。
- 既有边框语言。
- 原有光照方向和反射逻辑。

### Step 3: 生成宽图参考

使用 AI outpainting 生成宽屏环境参考图。

这一步的产物不能直接作为最终产品图验收，因为模型可能仍然重画中心主体。

验收重点：

- 左右环境是否自然。
- 地面和光流是否连续。
- 边框延展是否可信。
- 中心主体是否被改写。

如果中心主体被改写，不能直接使用该图。

### Step 4: 原图按原尺寸居中覆盖回去

这是 pipeline 的关键步骤。

最终合成时：

1. 使用 AI 生成图作为宽屏环境底图。
2. 将原图按原始像素尺寸居中覆盖回宽屏画布。
3. 原图覆盖区域作为最终中心主体。
4. AI 只保留左右扩展环境贡献。

这样可以保证：

- 产品主体来自原图。
- 产品比例不变。
- 产品位置不变。
- 产品清晰度不变。
- 金属结构不被 AI 重画。
- 展台、边框、中心地面和灯光不漂移。

### Step 5: 输出统一宽屏素材

输出宽屏素材后，前端只切换素材目录，不再通过 `cover`、`scale` 或拉伸修复比例。

推荐命名：

```text
<source-name>-outpaint-2280x1000.png
```

要求：

- 保持文件命名可追溯。
- 不覆盖原图。
- 保留原图作为 source of truth。
- 宽屏图作为页面展示素材。

## 4. Prompt 规范

Prompt 的作用是约束 AI 只扩环境，但不能替代工程化主体保护。

### 正向 Prompt

```text
Preserve the original image exactly as-is.
Treat the uploaded image as locked content.
The original image must remain visually identical in the center region.

Only extend the canvas horizontally.

Do not redesign the scene.
Do not reinterpret the products.
Do not generate a new showroom.

Keep all products, metal parts, stage structures, lighting, reflections, digital floor, and border design unchanged.

Only continue the existing blue industrial digital showroom environment naturally on the left and right sides.

Continue the existing background, floor reflections, atmospheric depth, border glow, lighting direction, and blue energy environment seamlessly.

The result should look like the original image was always a wider image, not a newly generated composition.

Industrial digital showroom, premium smart factory atmosphere, clean futuristic blue environment, subtle digital lighting, cinematic industrial presentation, seamless environment extension, preserve original composition, preserve original geometry, preserve original lighting.

Ultra high quality, sharp metal details, realistic industrial rendering, clean blue technology environment, no product distortion, no redesign.
```

### 负向 Prompt

```text
Do not redraw products.
Do not redesign the showroom.
Do not change product positions.
Do not change product scale.
Do not stretch products.
Do not crop products.
Do not replace products.
Do not create a new composition.
Do not generate a new center scene.
Do not alter the original stage.
Do not alter the original border.
Do not alter the original lighting.

No new products.
No people.
No text.
No logos.
No HUD.
No cyberpunk clutter.
No colorful redesign.
No fantasy effects.
No blurry metal.
No duplicated products.
No warped borders.
No environment replacement.
```

### 操作口径

```text
Canvas size: 2280 x 1000
Original image: centered
Resize original: no
Crop original: no
Outpaint: left and right only
Top/bottom: preserve original, minimal continuation only
Main subject lock: strict
```

## 5. 输出尺寸规范

当前沉淀的统一输出尺寸：

```text
2280 x 1000
```

选择原因：

- 接近 `2.28:1`，适合工业大屏产品中心的横向宽屏区域。
- 比普通 16:9 更扁，能保留产品 Spotlight 的展厅感。
- 比过宽比例更稳，不会让产品主体显得过小。
- 适合产品中心、大屏轮播、数字展厅横幅类模块复用。

构图建议：

- 产品主体占最终宽屏图约 `60%` 到 `75%` 的视觉宽度。
- 左右保留完整工业数字展厅空间。
- 产品不要贴边。
- 环境不要抢主体。
- 地面光环、反射和边框需要连续。

安全区域：

- 中心原图区域应完整保留。
- 左右扩展区只承载环境，不承载新产品。
- 重要产品边缘和金属件不能落在裁切风险区。

## 6. 工业主体保护规则

工业产品图的核心不是背景，而是产品主体。

必须锁定：

- 产品比例。
- 产品位置。
- 产品排列。
- 金属结构。
- 螺栓、孔位、轴、装配关系。
- 展台结构。
- 数字边框。
- 中心地面。
- 灯光方向。
- 反射关系。
- 主体清晰度。

硬规则：

```text
产品主体不能交给 AI。
原图必须最终覆盖回去。
AI 只能扩环境。
```

如果 AI 结果看起来更漂亮，但产品被改写，也必须判定失败。

推荐验收方式：

- 对比原图中心区域。
- 检查产品数量是否一致。
- 检查产品位置是否一致。
- 检查金属件轮廓和孔位是否一致。
- 检查边框、地面、光环是否在原图区域保持原样。
- 检查最终画布是否为 `2280 x 1000`。

## 7. 前端协作规则

前端不负责素材修复。

前端职责：

- 切换到已完成宽屏适配的素材目录。
- 稳定展示宽屏素材。
- 保持轮播逻辑。
- 保持产品中心结构。

前端不应再做：

- 用 `object-fit: cover` 强行填充。
- 用大幅 `scale` 修复比例。
- 拉伸图片。
- 为单张图写特殊裁切规则。
- 在页面中重新设计产品展示构图。

推荐展示策略：

```text
素材已经是目标比例 -> 前端只负责展示
```

如果素材仍然不协调，应回到素材 pipeline 修复，而不是继续堆前端样式补丁。

## 8. 适用场景

本 pipeline 适用于：

- MES 大屏。
- 工业数字展厅。
- 产品中心轮播。
- 设备展示屏。
- 智能工厂数字空间。
- 工业宣传屏。
- 制造业产品 Spotlight。
- 企业展厅大屏素材生产。

不适用于：

- 需要 AI 重新设计产品外观的创意图。
- 产品主体不要求真实一致的营销概念图。
- 需要完全重建 3D 场景的设计稿。
- 有严格 CAD / 工程图尺寸约束的技术图纸。

## 9. 目录结构建议

当前建议沉淀结构：

```text
common-capability/
  docs/
    industrial-product-outpainting.md
  examples/
    showroom-before-after/
```

说明：

- `docs/` 记录流程、边界、反例和验收标准。
- `examples/showroom-before-after/` 后续可以放最小示例，不要复制业务项目正式素材。
- 当前阶段不建立自动化 AI pipeline，不封装命令行工具，不引入图像 SDK。

## 10. 最小交付清单

每次执行工业产品图 outpainting 时，至少交付：

- 原图目录说明。
- 输出目录说明。
- 输出尺寸说明。
- 使用 prompt 说明。
- 主体保护方式说明。
- 是否经过原图覆盖回合成说明。
- 前端是否只切换素材目录说明。
- 验收结果说明。

如果没有执行“原图覆盖回去”，不能声明满足严格保真。
