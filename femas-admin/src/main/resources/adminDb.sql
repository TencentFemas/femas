CREATE DATABASE IF NOT EXISTS adminDb;
use adminDb;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for auth_rule
-- ----------------------------
DROP TABLE IF EXISTS `auth_rule`;
CREATE TABLE `auth_rule` (
  `rule_id` varchar(50) NOT NULL COMMENT '鉴权规则id',
  `namespace_id` varchar(20) DEFAULT NULL COMMENT '命名空间id',
  `service_name` varchar(128) DEFAULT NULL COMMENT '服务名',
  `rule_name` varchar(20) DEFAULT NULL COMMENT '规则名',
  `is_enable` varchar(20) DEFAULT NULL COMMENT '生效状态 1开启 0 关闭',
  `rule_type` varchar(20) DEFAULT NULL COMMENT '规则类型 关闭状态：CLOSE,白名单：WHITE,黑名单：BLACK',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `available_time` bigint(20) DEFAULT NULL COMMENT '生效时间',
  `tags` text COMMENT '标签列表',
  `tag_program` varchar(20) DEFAULT NULL COMMENT '标签计算规则',
  `target` varchar(20) DEFAULT NULL COMMENT '生效对象 所有接口：ALL 指定接口 PART',
  `desc` varchar(200) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for circuit_breaker_rule
-- ----------------------------
DROP TABLE IF EXISTS `circuit_breaker_rule`;
CREATE TABLE `circuit_breaker_rule` (
  `rule_id` varchar(50) NOT NULL COMMENT '规则id',
  `namespace_id` varchar(20) DEFAULT NULL COMMENT '命名空间id',
  `service_name` varchar(128) DEFAULT NULL COMMENT '服务名',
  `target_namespace_id` varchar(20) DEFAULT NULL COMMENT '下游命名空间',
  `target_service_name` varchar(128) DEFAULT NULL COMMENT '下游服务名',
  `rule_name` varchar(20) DEFAULT NULL COMMENT '规则名',
  `isolation_level` varchar(20) DEFAULT NULL COMMENT '隔离级别',
  `strategy` text COMMENT '熔断策略',
  `is_enable` varchar(20) DEFAULT NULL COMMENT '"是否开启 ：1开启；0：关闭"',
  `update_time` bigint(20) DEFAULT NULL COMMENT '生效时间',
  `desc` varchar(200) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for config
-- ----------------------------
DROP TABLE IF EXISTS `config`;
CREATE TABLE `config` (
  `config_key` varchar(200) NOT NULL COMMENT '配置key',
  `config_value` varchar(2000) DEFAULT NULL COMMENT '配置值',
  PRIMARY KEY (`config_key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for dcfg_config
-- ----------------------------
DROP TABLE IF EXISTS `dcfg_config`;
CREATE TABLE `dcfg_config` (
  `config_id` varchar(50) CHARACTER SET utf8 NOT NULL,
  `config_name` varchar(200) CHARACTER SET utf8 DEFAULT NULL,
  `namespace_id` varchar(50) CHARACTER SET utf8 DEFAULT NULL,
  `service_name` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
  `system_tag` varchar(20) CHARACTER SET utf8 DEFAULT NULL COMMENT '系统标签',
  `config_desc` varchar(200) CHARACTER SET utf8 DEFAULT NULL,
  `config_type` varchar(20) CHARACTER SET utf8 DEFAULT NULL,
  `create_time` bigint(20) DEFAULT NULL,
  `current_release_version_id` varchar(50) CHARACTER SET utf8 DEFAULT NULL,
  `last_release_version_id` varchar(50) CHARACTER SET utf8 DEFAULT NULL,
  `release_time` bigint(20) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for dcfg_config_version
-- ----------------------------
DROP TABLE IF EXISTS `dcfg_config_version`;
CREATE TABLE `dcfg_config_version` (
  `config_version_id` varchar(50) CHARACTER SET utf8 DEFAULT NULL,
  `config_id` varchar(50) CHARACTER SET utf8 DEFAULT NULL,
  `config_value` varchar(2000) CHARACTER SET utf8 DEFAULT NULL,
  `create_time` bigint(20) DEFAULT NULL,
  `release_time` bigint(20) NOT NULL DEFAULT '0',
  `release_status` varchar(4) CHARACTER SET utf8 DEFAULT NULL COMMENT 'U: 未发布； S：发布成功；F：发布失败；RS：回滚成功；RF：回滚失败；DS：删除成功；DF：删除失败；',
  `config_version` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for namespace
-- ----------------------------
DROP TABLE IF EXISTS `namespace`;
CREATE TABLE `namespace` (
  `namespace_id` varchar(50) NOT NULL COMMENT '命名空间id',
  `registry_id` varchar(50) NOT NULL COMMENT '组册中心id',
  `name` varchar(20) DEFAULT NULL COMMENT '名称',
  `desc` varchar(200) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`namespace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for rate_limit_rule
-- ----------------------------
DROP TABLE IF EXISTS `rate_limit_rule`;
CREATE TABLE `rate_limit_rule` (
  `rule_id` varchar(50) NOT NULL COMMENT '限流规则id',
  `namespace_id` varchar(50) DEFAULT NULL COMMENT '命名空间id',
  `service_name` varchar(128) DEFAULT NULL COMMENT '服务名',
  `rule_name` varchar(500) DEFAULT NULL COMMENT '规则名',
  `type` varchar(50) DEFAULT NULL COMMENT '限流粒度：GLOBAL, PART',
  `tags` text COMMENT '流量来源规则',
  `duration` int(20) DEFAULT NULL COMMENT '单位时间（s）',
  `total_quota` int(20) DEFAULT NULL COMMENT '请求数',
  `status` int(5) DEFAULT NULL COMMENT '生效状态 1：开启 0：关闭',
  `update_time` bigint(20) DEFAULT NULL COMMENT '生效时间',
  `desc` varchar(200) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for record
-- ----------------------------
DROP TABLE IF EXISTS `record`;
CREATE TABLE `record` (
  `log_id` varchar(50) NOT NULL COMMENT '日志id',
  `user` varchar(20) DEFAULT NULL,
  `status` tinyint(11) DEFAULT NULL COMMENT '1成功0失败',
  `detail` varchar(2000) DEFAULT NULL COMMENT '操作详情',
  `type` varchar(2000) DEFAULT NULL COMMENT '操作类型',
  `module` varchar(20) DEFAULT NULL COMMENT '模块',
  `time` bigint(20) DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for registry_config
-- ----------------------------
DROP TABLE IF EXISTS `registry_config`;
CREATE TABLE `registry_config` (
  `registry_id` varchar(50) NOT NULL COMMENT '注册中心id',
  `registry_cluster` varchar(200) DEFAULT NULL COMMENT '注册中心集群地址ex：ip:port,ip:port...',
  `registry_name` varchar(20) DEFAULT NULL COMMENT '注册中心名称',
  `registry_type` varchar(20) DEFAULT NULL COMMENT '注册中心类型',
  `certificate_type` varchar(20) DEFAULT NULL COMMENT 'k8s验证类型',
  `kube_config` varchar(2000) DEFAULT NULL COMMENT 'k8s配置',
  `secret` varchar(200) DEFAULT NULL COMMENT 'k8s秘钥',
  `user_name` varchar(16) DEFAULT NULL COMMENT 'nacos账号',
  `password` varchar(32) DEFAULT NULL COMMENT 'nacos密码',
  `status` int(20) DEFAULT NULL COMMENT '1正常 2 异常',
  `instance_count` int(20) DEFAULT NULL COMMENT '实例数',
  PRIMARY KEY (`registry_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for route_rule
-- ----------------------------
DROP TABLE IF EXISTS `route_rule`;
CREATE TABLE `route_rule` (
  `rule_id` varchar(50) NOT NULL COMMENT '路由规则id',
  `namespace_id` varchar(20) DEFAULT NULL COMMENT '命名空间id',
  `service_name` varchar(128) DEFAULT NULL COMMENT '服务名',
  `rule_name` varchar(20) DEFAULT NULL COMMENT '规则名',
  `status` varchar(20) DEFAULT NULL COMMENT '生效状态 1开启 0 关闭',
  `route_tag` text COMMENT '路由标签',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '生效时间',
  `desc` varchar(200) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for service_api
-- ----------------------------
DROP TABLE IF EXISTS `service_api`;
CREATE TABLE `service_api` (
  `api_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '接口id',
  `namespace_id` varchar(50) DEFAULT NULL COMMENT '命名空间',
  `service_name` varchar(128) DEFAULT NULL COMMENT '服务名',
  `path` varchar(50) DEFAULT NULL COMMENT '接口路径',
  `status` tinyint(4) DEFAULT NULL COMMENT '健康状态1健康 0异常',
  `service_version` varchar(10) DEFAULT NULL COMMENT '服务端版本',
  `method` varchar(10) DEFAULT NULL COMMENT '方法类型 ex:post,get',
  PRIMARY KEY (`api_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for service_event
-- ----------------------------
DROP TABLE IF EXISTS `service_event`;
CREATE TABLE `service_event` (
  `event_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '事件id',
  `namespace_id` varchar(50) DEFAULT NULL COMMENT '命名空间',
  `service_name` varchar(128) DEFAULT NULL COMMENT '服务名',
  `event_type` varchar(20) DEFAULT NULL COMMENT '事件类型',
  `occur_time` bigint(20) DEFAULT NULL COMMENT '时间发生时间(admin产生)',
  `upstream` varchar(128) DEFAULT NULL COMMENT '上游服务',
  `downstream` varchar(128) DEFAULT NULL COMMENT '下游服务',
  `instance_id` varchar(150) DEFAULT NULL COMMENT '实例id',
  `additional_msg` varchar(500) DEFAULT NULL COMMENT '额外信息',
  PRIMARY KEY (`event_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tolerant
-- ----------------------------
DROP TABLE IF EXISTS `tolerant`;
CREATE TABLE `tolerant` (
  `namespace_id` varchar(50) NOT NULL COMMENT '命名空间',
  `service_name` varchar(128) NOT NULL COMMENT '服务名',
  `is_tolerant` tinyint(4) DEFAULT NULL COMMENT '容错保护开关1开启 0关闭',
  PRIMARY KEY (`namespace_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for lane_info
-- ----------------------------
DROP TABLE IF EXISTS `lane_info`;
CREATE TABLE `lane_info` (
  `lane_id` varchar(50) NOT NULL COMMENT '泳道ID',
  `lane_name` varchar(255) DEFAULT '' COMMENT '泳道名称',
  `remark` varchar(2000) DEFAULT '' COMMENT '备注',
  `create_time` bigint(20) DEFAULT NULL COMMENT '规则创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '规则更新时间',
  `lane_service_list` varchar(5000) DEFAULT NULL COMMENT '泳道服务列表',
 PRIMARY KEY (`lane_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for lane_rule
-- ----------------------------
DROP TABLE IF EXISTS `lane_rule`;
CREATE TABLE `lane_rule` (
 `rule_id` varchar(50) NOT NULL COMMENT '泳道规则id',
 `rule_name` varchar(2000) DEFAULT '' COMMENT '泳道规则名称',
 `remark` varchar(5000) DEFAULT '' COMMENT '备注',
 `rule_tag_list` text COMMENT '规则tag列表',
 `rule_tag_relationship` text COMMENT 'tag聚会关系',
 `relative_lane` varchar(5000) DEFAULT NULL COMMENT '关联泳道id',
 `enable` int(5) DEFAULT NULL COMMENT '1：开启 0:关闭',
 `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
 `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
 `gray_type` varchar(255) DEFAULT NULL COMMENT '灰度类型 tag，canary',
 `priority` int(11) NOT NULL COMMENT '规则优先级',
 PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
