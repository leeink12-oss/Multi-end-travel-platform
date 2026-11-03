-- Travel Platform database schema
-- Generated from the project's local Navicat export.
-- Runtime/user data and demo records were intentionally removed before publishing.
-- The application can create/update tables via JPA; import this file when an explicit schema is preferred.

/*
 Navicat Premium Dump SQL

 Source Server         : 127.0.0.1_3306
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : 127.0.0.1:3306
 Source Schema         : nadouqu

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 24/05/2026 00:01:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app_messages
-- ----------------------------
DROP TABLE IF EXISTS `app_messages`;
CREATE TABLE `app_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `read_flag` bit(1) NOT NULL,
  `related_merchant_id` bigint NULL DEFAULT NULL,
  `related_order_id` bigint NULL DEFAULT NULL,
  `title` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `related_target_id` bigint NULL DEFAULT NULL,
  `related_target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKds1fihfkia1fmffg8b6qjdet5`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FKds1fihfkia1fmffg8b6qjdet5` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for cities
-- ----------------------------
DROP TABLE IF EXISTS `cities`;
CREATE TABLE `cities`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `enabled` bit(1) NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `score` int NULL DEFAULT NULL,
  `target_id` bigint NOT NULL,
  `target_type` enum('FLIGHT','GROUPON','HOTEL','RESTAURANT','SCENIC','TRAIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `visible` bit(1) NOT NULL,
  `user_id` bigint NOT NULL,
  `reply_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `reply_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK8omq0tc18jd43bu5tjh6jvraq`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK8omq0tc18jd43bu5tjh6jvraq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for coupons
-- ----------------------------
DROP TABLE IF EXISTS `coupons`;
CREATE TABLE `coupons`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `discount_rate` decimal(5, 4) NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for dining_reservations
-- ----------------------------
DROP TABLE IF EXISTS `dining_reservations`;
CREATE TABLE `dining_reservations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `contact_phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `people_count` int NULL DEFAULT NULL,
  `reserve_time` datetime(6) NULL DEFAULT NULL,
  `status` enum('CANCELLED','CONFIRMED','PENDING','REFUNDED','UNUSED','USED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `restaurant_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK1w9ugq8q34a0oqsiaenaxm5u6`(`restaurant_id` ASC) USING BTREE,
  INDEX `FK2og0ahu823cqoupbk1jufk8b6`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK1w9ugq8q34a0oqsiaenaxm5u6` FOREIGN KEY (`restaurant_id`) REFERENCES `restaurants` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK2og0ahu823cqoupbk1jufk8b6` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `target_id` bigint NOT NULL,
  `target_type` enum('FLIGHT','GROUPON','HOTEL','RESTAURANT','SCENIC','TRAIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK781pmjx7kf279uopjhmbwnqhl`(`user_id` ASC, `target_id` ASC, `target_type` ASC) USING BTREE,
  CONSTRAINT `FKk7du8b8ewipawnnpg76d55fus` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for flights
-- ----------------------------
DROP TABLE IF EXISTS `flights`;
CREATE TABLE `flights`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `arrive_time` datetime(6) NOT NULL,
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `depart_time` datetime(6) NOT NULL,
  `flight_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `from_city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `price` decimal(10, 2) NOT NULL,
  `status` enum('DRAFT','OFFLINE','ONLINE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `to_city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `merchant_id` bigint NOT NULL,
  `business_class_price` decimal(10, 2) NULL DEFAULT NULL,
  `economy_class_price` decimal(10, 2) NULL DEFAULT NULL,
  `first_class_price` decimal(10, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK4b2h8v5h7npottonkggbigxm0`(`merchant_id` ASC) USING BTREE,
  CONSTRAINT `FK4b2h8v5h7npottonkggbigxm0` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 376 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for groupon_orders
-- ----------------------------
DROP TABLE IF EXISTS `groupon_orders`;
CREATE TABLE `groupon_orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `quantity` int NULL DEFAULT NULL,
  `status` enum('CANCELLED','CONFIRMED','PENDING','REFUNDED','UNUSED','USED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `total_price` decimal(38, 2) NULL DEFAULT NULL,
  `groupon_package_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `verify_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKkw9gw6n41mrulb7ta2yp9g46f`(`groupon_package_id` ASC) USING BTREE,
  INDEX `FKn50ldxadm65gri1igit4fagh9`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FKkw9gw6n41mrulb7ta2yp9g46f` FOREIGN KEY (`groupon_package_id`) REFERENCES `groupon_packages` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKn50ldxadm65gri1igit4fagh9` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for groupon_packages
-- ----------------------------
DROP TABLE IF EXISTS `groupon_packages`;
CREATE TABLE `groupon_packages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `group_price` decimal(38, 2) NOT NULL,
  `image_url` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `original_price` decimal(38, 2) NOT NULL,
  `stock` int NULL DEFAULT NULL,
  `title` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `restaurant_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKpupvhk4i3kn0ckdvplnac5cy9`(`restaurant_id` ASC) USING BTREE,
  CONSTRAINT `FKpupvhk4i3kn0ckdvplnac5cy9` FOREIGN KEY (`restaurant_id`) REFERENCES `restaurants` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for hotel_room_prices
-- ----------------------------
DROP TABLE IF EXISTS `hotel_room_prices`;
CREATE TABLE `hotel_room_prices`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `price_date` date NOT NULL,
  `price` decimal(38, 2) NOT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKlph28asm5509799ywij9vb9ge`(`room_id` ASC, `price_date` ASC) USING BTREE,
  CONSTRAINT `FKd1it8e6se5icctvrs4ksbuchi` FOREIGN KEY (`room_id`) REFERENCES `hotel_rooms` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 613 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for hotel_rooms
-- ----------------------------
DROP TABLE IF EXISTS `hotel_rooms`;
CREATE TABLE `hotel_rooms`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `facilities` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `floor` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `max_price` decimal(38, 2) NULL DEFAULT NULL,
  `min_price` decimal(38, 2) NULL DEFAULT NULL,
  `room_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `size` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `hotel_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKigrfhojvvvsq1hore24uf5sst`(`hotel_id` ASC) USING BTREE,
  CONSTRAINT `FKigrfhojvvvsq1hore24uf5sst` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for hotels
-- ----------------------------
DROP TABLE IF EXISTS `hotels`;
CREATE TABLE `hotels`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `price` decimal(38, 2) NULL DEFAULT NULL,
  `status` enum('DRAFT','OFFLINE','ONLINE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `city_id` bigint NOT NULL,
  `merchant_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKs4hjpubog1wqi4qktjpoejarg`(`city_id` ASC) USING BTREE,
  INDEX `FK8sgql22ghk81jmdtctebbmo4t`(`merchant_id` ASC) USING BTREE,
  CONSTRAINT `FK8sgql22ghk81jmdtctebbmo4t` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKs4hjpubog1wqi4qktjpoejarg` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for merchants
-- ----------------------------
DROP TABLE IF EXISTS `merchants`;
CREATE TABLE `merchants`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` enum('APPROVED','DISABLED','PENDING') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `type` enum('CATERING','TRANSPORT','TRAVEL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKh9v6fyuu5mnk7p3qtlpta2947`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FKa759srj6ts95j9qh089b6gbei` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cancel_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cancel_requested` bit(1) NULL DEFAULT NULL,
  `contact_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `contact_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `quantity` int NOT NULL,
  `status` enum('CANCELLED','CANCEL_REQUESTED','COMPLETED','CREATED','PAID') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `target_id` bigint NOT NULL,
  `target_type` enum('FLIGHT','GROUPON','HOTEL','RESTAURANT','SCENIC','TRAIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `total_price` decimal(38, 2) NOT NULL,
  `use_date` date NULL DEFAULT NULL,
  `merchant_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `check_in_date` date NULL DEFAULT NULL,
  `check_out_date` date NULL DEFAULT NULL,
  `room_id` bigint NULL DEFAULT NULL,
  `started` bit(1) NULL DEFAULT NULL,
  `ticket_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `coupon_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `price_after_coupon` decimal(38, 2) NULL DEFAULT NULL,
  `price_before_coupon` decimal(38, 2) NULL DEFAULT NULL,
  `transport_seat_class` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKtqvpn5gy0ajx8fip3deoidfxd`(`merchant_id` ASC) USING BTREE,
  INDEX `FK32ql8ubntj5uh44ph9659tiih`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKtqvpn5gy0ajx8fip3deoidfxd` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for recommended_dish_likes
-- ----------------------------
DROP TABLE IF EXISTS `recommended_dish_likes`;
CREATE TABLE `recommended_dish_likes`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dish_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK9pktir5jfi4ec5lahibie7d35`(`dish_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `FKan18s9fqsdymevj8ik052ep8s`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FKan18s9fqsdymevj8ik052ep8s` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKflu5t6pc5qrm1xow4xcmbodt7` FOREIGN KEY (`dish_id`) REFERENCES `recommended_dishes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for recommended_dishes
-- ----------------------------
DROP TABLE IF EXISTS `recommended_dishes`;
CREATE TABLE `recommended_dishes`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `image_url` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `like_count` int NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `restaurant_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKsl9a3l73180se71yau4g3a94g`(`restaurant_id` ASC) USING BTREE,
  CONSTRAINT `FKsl9a3l73180se71yau4g3a94g` FOREIGN KEY (`restaurant_id`) REFERENCES `restaurants` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for restaurants
-- ----------------------------
DROP TABLE IF EXISTS `restaurants`;
CREATE TABLE `restaurants`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avg_price` decimal(38, 2) NULL DEFAULT NULL,
  `cover_image` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `open_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` enum('DRAFT','OFFLINE','ONLINE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `city_id` bigint NOT NULL,
  `merchant_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKqxinx5shi9vp70a0huwi3bhqv`(`city_id` ASC) USING BTREE,
  INDEX `FKipv2k23iabgggck1wm4cr5t7q`(`merchant_id` ASC) USING BTREE,
  CONSTRAINT `FKipv2k23iabgggck1wm4cr5t7q` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKqxinx5shi9vp70a0huwi3bhqv` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for scenic_spots
-- ----------------------------
DROP TABLE IF EXISTS `scenic_spots`;
CREATE TABLE `scenic_spots`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `price` decimal(38, 2) NULL DEFAULT NULL,
  `status` enum('DRAFT','OFFLINE','ONLINE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `city_id` bigint NOT NULL,
  `merchant_id` bigint NOT NULL,
  `adult_price` decimal(38, 2) NULL DEFAULT NULL,
  `child_price` decimal(38, 2) NULL DEFAULT NULL,
  `free` bit(1) NULL DEFAULT NULL,
  `student_price` decimal(38, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKke24nlh6gp7870skqve2ooe3r`(`city_id` ASC) USING BTREE,
  INDEX `FK9fnwvfhaq9h1a0vi5kggbi2fu`(`merchant_id` ASC) USING BTREE,
  CONSTRAINT `FK9fnwvfhaq9h1a0vi5kggbi2fu` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKke24nlh6gp7870skqve2ooe3r` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for trains
-- ----------------------------
DROP TABLE IF EXISTS `trains`;
CREATE TABLE `trains`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `arrive_time` datetime(6) NOT NULL,
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `depart_time` datetime(6) NOT NULL,
  `from_city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `price` decimal(10, 2) NOT NULL,
  `status` enum('DRAFT','OFFLINE','ONLINE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `to_city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `train_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `merchant_id` bigint NOT NULL,
  `emu_first_class_seat_price` decimal(10, 2) NULL DEFAULT NULL,
  `emu_first_seat_price` decimal(10, 2) NULL DEFAULT NULL,
  `emu_no_seat_price` decimal(10, 2) NULL DEFAULT NULL,
  `emu_second_seat_price` decimal(10, 2) NULL DEFAULT NULL,
  `train_hard_seat_price` decimal(10, 2) NULL DEFAULT NULL,
  `train_hard_sleeper_price` decimal(10, 2) NULL DEFAULT NULL,
  `train_soft_sleeper_price` decimal(10, 2) NULL DEFAULT NULL,
  `train_type` enum('EMU','TRAIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK3y37bk611qwu5b2p21sklgfac`(`merchant_id` ASC) USING BTREE,
  CONSTRAINT `FK3y37bk611qwu5b2p21sklgfac` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 375 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for user_coupons
-- ----------------------------
DROP TABLE IF EXISTS `user_coupons`;
CREATE TABLE `user_coupons`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `used` bit(1) NOT NULL,
  `used_at` datetime(6) NULL DEFAULT NULL,
  `coupon_id` bigint NOT NULL,
  `order_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK9oi3p5xyfe4j32xs54nn7mi20`(`coupon_id` ASC) USING BTREE,
  INDEX `FK75uvyldhruqeeman5b0l35hnu`(`order_id` ASC) USING BTREE,
  INDEX `FK654lvm2qu8l08pyg310mbd74h`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK654lvm2qu8l08pyg310mbd74h` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK75uvyldhruqeeman5b0l35hnu` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK9oi3p5xyfe4j32xs54nn7mi20` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` enum('ADMIN','MERCHANT','USER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKr43af9ap4edm43mmtq01oddj6`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Demo seed data intentionally omitted
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
