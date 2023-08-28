/*
 Navicat MySQL Data Transfer

 Source Server         : FL_local
 Source Server Type    : MySQL
 Source Server Version : 80019
 Source Host           : localhost:3306
 Source Schema         : cib24

 Target Server Type    : MySQL
 Target Server Version : 80019
 File Encoding         : 65001

 Date: 26/08/2023 16:35:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for prize_inventory
-- ----------------------------
DROP TABLE IF EXISTS `prize_inventory`;
CREATE TABLE `prize_inventory`  (
  `prize_id` int(0) NOT NULL,
  `prize_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `prize_num` int(0) NOT NULL,
  `prize_rank` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`prize_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of prize_inventory
-- ----------------------------
INSERT INTO `prize_inventory` VALUES (1, '兴业银行offer', 5, '一等奖');
INSERT INTO `prize_inventory` VALUES (2, '法拉利', 10, '二等奖');
INSERT INTO `prize_inventory` VALUES (3, '索尼相机', 20, '三等奖');
INSERT INTO `prize_inventory` VALUES (4, '蓝牙耳机', 50, '四等奖');
INSERT INTO `prize_inventory` VALUES (5, '兴业公仔', 100, '五等奖');
INSERT INTO `prize_inventory` VALUES (6, '积分150', 10000, '谢谢参与');
INSERT INTO `prize_inventory` VALUES (7, '积分100', 10000, '谢谢参与');
INSERT INTO `prize_inventory` VALUES (8, '积分50', 10000, '谢谢参与');

SET FOREIGN_KEY_CHECKS = 1;
