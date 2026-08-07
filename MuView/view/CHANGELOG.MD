# ChangeLog | 更新目录

## LunaLight V0

### V0.1

- 将 ElementPlus UI 更换为 Shadcn/vue 以支持自定义度更高的 UI 控件

### V0.1.2

#### 重要
- 添加 MuDashboard 控件以解决 Shadcn/Vue 中没有类似控件的问题
    > 灵感来源：https://tailwindcss.com/docs/mask-image#adding-a-conic-mask

#### 常规
- 调整了一些样式
- 调整了 AboutView 中应用标题的布局
- 将侧边栏菜单项列表移动至路由文件

### V0.1.3

#### 重要
- MuDashboard：现在仪表盘可以通过 “as” 属性指定卡片外观或非卡片外观
- MuSidebar：添加了一个控制暗黑模式的菜单项，且这是强制性的菜单项

#### 常规
- HomeView：现在控制内容样式的 div 区块移动至上一级，现在 HomeView 为默认 flex-nowrap 状态的容器

### V0.1.4

#### 重要
- MuSidebar：添加了 Tooltip（提示气泡），其可在侧边栏为收起状态时通过光标覆盖提示该菜单项的标题
- AboutView：修复了分隔线在暗黑模式下颜色显示错误的问题
- 由于暂时没有用途，删除了顶边栏菜单控件（navigation-menu）

#### 常规
- MuSidebar：修改了主颜色
