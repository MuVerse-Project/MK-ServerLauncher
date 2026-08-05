<h3 style="font-size: 24px" align="center">系列 III · 第四幕</h3>
<h1 style="font-size: 32px; font-weight: bold" align="center">MuView</h1>

<p align="center">The Front-end Implementation for MK-Series Management based on Vue & TypeScript</p>
<p align="center">基于 Vue3 & TypeScript 的 MK-Series 管理类前端实现</p>

<div align="center">
<img alt="Static Badge" src="https://img.shields.io/badge/Vue-V3-gray?style=flat&logo=vue.js&logoColor=white&labelColor=darkgreen" />
<img alt="Static Badge" src="https://img.shields.io/badge/TailwindCSS-V4-gray?style=flat&logo=tailwindcss&logoColor=white&labelColor=blue" />
<img alt="Static Badge" src="https://img.shields.io/badge/Shadcn Vue-V2-gray?style=flat&logoColor=white&labelColor=black" />
</div><br/>

---

# Role | 项目定位

*就当前阶段而言，MuView 是一个主要基于 Vue3 + TypeScript 的“轻量”SPA 实现。*

*在此基础之上，它是一个为暮空系列（MK-Series）管理类产品或其他特定产品提供管理类前端实现的项目。*

<h1><a href="/CHANGELOG.MD">ChangeLog | 更新目录</a></h1>

# Framework | 框架

- Vite
- Vue
- VueRouter
- VueUse
- Axios
- Vue I18n
- OverlayScorollBar
- TailwindCSS
- Shadcn/UI Vue
- Reka UI
- Lucide Vue
- Vue Sonner

# Structure | 总体架构

## MuAPI

- MuPacketAPI4TS
  The message protocol using in communicate between MuView and MuCore based on JSON
- MuCoreConnector
  Used to connect MuCore & Backend RESTful API endpoints.
- UserManagement
  User Management API Provider
- Authorization
  User Authorization API Provider

## MuView

- App / MainView with RouterView
- Customized Views by Developer
