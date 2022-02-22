# 贡献Femas

**[`English`](./CONTRIBUTING.md) | `简体中文`**

欢迎加入我们！ 本文档是关于如何为 Femas 做出贡献的指南。

如果您有好的意见或建议，欢迎创建[`Issues`](https://github.com/Tencent/Femas/issues/new) 或[`Pull Requests`](https://github.com/Tencent/Femas/pulls) ，为Femas开源社区做贡献。 Femas 持续招募贡献者，即使是在 issue 中回答问题，或者做一些简单的 bug 修复，都会对我们有很大的帮助。

**[腾讯开源激励计划](https://opensource.tencent.com/contribution)鼓励开发者参与贡献，期待您的加入。**

-------------------


## 行为准则
> 请务必阅读并遵守我们的 **[行为准则](./Code-of-Conduct.md)**。

## 报告 Bugs/提交issue
* 搜索 [issues](https://github.com/Tencent/Femas/issues) 确保该错误未被提出，以免重复提交。
* 通过社区微信群或邮件与社区取得联系，确认bug或者新的feature（可选）。
*  [新建一个issue](https://github.com/Tencent/Femas/issues/new)，包含**标题和详细描述、代码示例、截图或短视频**以帮助我们定位问题。
## 开始贡献
Femas社区贡献者主要有四种角色类型：
- 用户（在企业环境调研或正在落地Femas的社区用户）
- 贡献者（给项目带来价值或者合并过PR的人）
- 提交者（PR达到一定数量，由社区评委会评定提升为提交者角色，成为社区骨干成员）
- PMC（项目管理员）

我们鼓励新人积极参与Femas 项目，我们也非常欢迎企业用户使用Femas，给社区提供落地案例，社区也会全力支持企业用户落地，并将精彩的案例分享给大家，互联网的精神就是分享，Femas社区也将持续将腾讯内部微服务最佳实践方法论和业界共享。

### Pull Request指南
> 首先从[issues列表](https://github.com/Tencent/Femas/issues) 中选择一个想要修复的问题，任何PR都必须关联一个有效的issue，或者先和社区成员联系，确认该issue无人认领，如果有多人同时认领同一个issue，社区将会经过评审，将issue分配给方案最合理的维护者。

Femas以 **`develop`** 分支作为开发基准分支，下面是具体操作流程：
1. 从 **`develop`分支** fork代码到你自己GitHub的仓库。
2. 创建一个新分支，分支名称可以issue号命名例如(fix_issue#007)。
3. 提交更改，提交之前请确认你的分支和源分支代码同步。
4. 将你的提交推送到你的fork仓库。
5. 向 **`develop`分支** 创建拉取请求。

提交PR注意事项：
- 请务必参照[拉取请求模板](./PULL_REQUEST_TEMPLATE.md)。
- 请确认PR关联有效issue，否则PR将被拒绝。
- 请在新创建的文件中添加许可和版权声明。
- 确保一致的代码风格，做充分的测试。
- 如果PR 包含大量更改，例如组件重构或新组件，请详细说明其设计和用法。
-  确保提交信息尽量清晰简洁。

### 代码审查
所有合并到官方的代码都会经过社区review评审，一般这些工作会分配给Committer或者PMC，当然我们也会通过自动化的方式，规避一些凌乱的代码风格或者潜在的漏洞，我们必须保持整个代码仓库高的质量标准：
> - 可读性 ：重要的代码应该有详细的注释和文档记录， API 应该有 Javadoc。
> - 优雅：通用组件和函数要有合理的设计，具有很好的可扩展性，标准化、模块化的代码设计是Femas的内核。
> - 可维护性 ：确保一致的代码风格，符合我们的 [代码规范](style/codeStyle.md)。
> - 可测试性 ：核心feature的相关代码应该被单元测试用例覆盖。  
    
## 关于源代码的问题
#### [常见问题]()
#### 加入我们的社区交流群

![image](https://user-images.githubusercontent.com/22976760/153165352-361517e8-0712-4194-938f-2ea712ddf0a6.png)


