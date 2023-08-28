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

 Date: 26/08/2023 16:36:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for raffle_records
-- ----------------------------
DROP TABLE IF EXISTS `raffle_records`;
CREATE TABLE `raffle_records`  (
  `raffle_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `prize_id` int(0) NOT NULL,
  `prize_time` datetime(0) NOT NULL,
  PRIMARY KEY (`raffle_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of raffle_records
-- ----------------------------
INSERT INTO `raffle_records` VALUES ('111', '1', 1, '2023-08-26 16:34:26');
INSERT INTO `raffle_records` VALUES ('222', '2', 3, '2023-08-25 12:35:46');

SET FOREIGN_KEY_CHECKS = 1;
