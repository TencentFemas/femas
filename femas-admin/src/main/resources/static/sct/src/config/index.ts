import resolvePath from "@src/common/util/resolvePath";

const baseUrl = window["FEMAS_BASE_PATH"];

export const Menu = {
  guide: {
    title: "操作指引",
    icon: [`${baseUrl}/icon/guide.svg`, `${baseUrl}/icon/guide-hover.svg`],
  },
  registry: {
    title: "注册中心",
    icon: [
      `${baseUrl}/icon/registry.svg`,
      `${baseUrl}/icon/registry-hover.svg`,
    ],
  },
  namespace: {
    title: "命名空间",
    icon: [
      `${baseUrl}/icon/namespace.svg`,
      `${baseUrl}/icon/namespace-hover.svg`,
    ],
  },
  service: {
    title: "服务治理",
    icon: [`${baseUrl}/icon/service.svg`, `${baseUrl}/icon/service-hover.svg`],
  },
  config: {
    title: "配置管理",
    icon: [`${baseUrl}/icon/config.svg`, `${baseUrl}/icon/config-hover.svg`],
  },
  monitor: {
    title: "服务可观测",
    isSubMenu: true,
    icon: [`${baseUrl}/icon/monitor.svg`, `${baseUrl}/icon/monitor-hover.svg`],

    submenus: [
      {
        title: "服务依赖拓扑",
        route: "topology",
      },
      { title: "调用链查询", route: "trace" },
      // { title: "服务监控", route: "monitor" },
    ],
  },
  "operation-log": {
    title: "变更记录",
    icon: [`${baseUrl}/icon/log.svg`, `${baseUrl}/icon/log-hover.svg`],
  },
  publish: {
    title: "全链路灰度发布",
    icon: [`${baseUrl}/icon/publish.svg`, `${baseUrl}/icon/publish-hover.svg`],
  },
};

export const defaultMenu = "guide";

export const apiEndpoint = resolvePath(window["FEMAS_BASE_PATH"], "atom/v1");

export const AuthenticationKey = "atom_login_token";
