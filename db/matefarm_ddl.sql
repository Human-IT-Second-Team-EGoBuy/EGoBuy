-- 1) 새로운  project 계정 만들기
CREATE USER 'matefarm'@'%' IDENTIFIED BY  'matefarm';

-- 2) 데이터베이스 생성 후 계정에 권한 부여

-- 데이터베이스(스키마) 생성
CREATE DATABASE matefarm;

-- 데이터베이스 확인
SHOW databases;

-- matefarm 계정에 권한 부여
GRANT ALL PRIVILEGES ON matefarm.* TO 'matefarm'@'%';

-- matefarm 계정의 바뀐 권한 확인하기
SHOW GRANTS FOR 'matefarm'@'%';

-- ddl 구문 삽입 전 DB 사용 명시
USE matefarm;


------------------------------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS recomment;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS community_post;
DROP TABLE IF EXISTS post_like;
DROP TABLE IF EXISTS post_report;
DROP TABLE IF EXISTS user_report;
DROP TABLE IF EXISTS answer;
DROP TABLE IF EXISTS inquiry;
DROP TABLE IF EXISTS conversations_messages;
DROP TABLE IF EXISTS conversations;
DROP TABLE IF EXISTS penalty;
DROP TABLE IF EXISTS blacklist;
DROP TABLE IF EXISTS farm_log;
DROP TABLE IF EXISTS files;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS soil_analysis;
DROP TABLE IF EXISTS habitat_records;
DROP TABLE IF EXISTS insect_detail;
DROP TABLE IF EXISTS insect;
DROP TABLE IF EXISTS disease_detail;
DROP TABLE IF EXISTS disease;
DROP TABLE IF EXISTS crop_cultivation;
DROP TABLE IF EXISTS variety;
DROP TABLE IF EXISTS variety_group;
DROP TABLE IF EXISTS crop;
DROP TABLE IF EXISTS crop_categories;
DROP TABLE IF EXISTS land_transactions;
DROP TABLE IF EXISTS wildlife_species;
DROP TABLE IF EXISTS regional_retail_prices;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 전체 테이블 DDL (AUTO_INCREMENT 및 FK 제약조건 포함)
-- ============================================

-- 1. users 테이블
CREATE TABLE `users` (
	`user_id` BIGINT NOT NULL AUTO_INCREMENT,
	`user_name` VARCHAR(20) NOT NULL,
	`user_auth_id` VARCHAR(255) NOT NULL,
	`user_password` VARCHAR(255) NOT NULL,
	`nickname` VARCHAR(20) NOT NULL,
	`email` VARCHAR(255) NOT NULL,
	`phone_number` VARCHAR(255) NULL,
	`road_name_address` VARCHAR(255) NULL,
	`detailed_address` VARCHAR(255) NULL,
	`postcode` VARCHAR(20) NULL,
	`user_role` ENUM('USER', 'ADMIN') NOT NULL,
	`user_status` ENUM('ACTIVE', 'INACTIVE', 'BLACKED','DELETED') NOT NULL,
	`signup_path` ENUM('NORMAL', 'KAKAO', 'NAVER', 'GOOGLE') NOT NULL,
	`user_identifier` VARCHAR(20) NOT NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
	`deleted_at` TIMESTAMP NULL,
	`privacy_agreement_yn` ENUM('Y', 'N') NOT NULL,
	`marketing_email_agreement_yn` ENUM('Y', 'N') NOT NULL,
	PRIMARY KEY (`user_id`)
) ENGINE=InnoDB;

-- 2. crop_categories 테이블
CREATE TABLE `crop_categories` (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `category_name` varchar(100) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_crop_categories_name` (`category_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1157 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. crop 테이블
CREATE TABLE `crop` (
  `crop_id` int NOT NULL AUTO_INCREMENT,
  `category_id` int DEFAULT NULL,
  `crop_name` varchar(100) NOT NULL,
  `crop_code` varchar(30) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`crop_id`),
  UNIQUE KEY `uk_crop_name` (`crop_name`),
  UNIQUE KEY `uk_crop_code` (`crop_code`),
  KEY `idx_crop_category` (`category_id`),
  CONSTRAINT `fk_crop_category` FOREIGN KEY (`category_id`) REFERENCES `crop_categories` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1161 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. variety_group 테이블
CREATE TABLE `variety_group` (
  `variety_group_id` int NOT NULL AUTO_INCREMENT,
  `crop_id` int NOT NULL,
  `group_name` varchar(255) NOT NULL,
  `status` int NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`variety_group_id`),
  UNIQUE KEY `uk_vg_crop_group` (`crop_id`,`group_name`),
  KEY `idx_vg_crop_id` (`crop_id`),
  CONSTRAINT `fk_vg_crop` FOREIGN KEY (`crop_id`) REFERENCES `crop` (`crop_id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. variety 테이블
CREATE TABLE `variety` (
  `variety_id` int NOT NULL AUTO_INCREMENT,
  `variety_group_id` int NOT NULL,
  `variety_name` varchar(255) NOT NULL,
  `usage` varchar(255) DEFAULT NULL,
  `lineage_name` varchar(255) DEFAULT NULL,
  `mother` varchar(255) DEFAULT NULL,
  `father` varchar(255) DEFAULT NULL,
  `adapt_region` varchar(255) DEFAULT NULL,
  `main_features` text,
  PRIMARY KEY (`variety_id`),
  UNIQUE KEY `uk_v_vg_name` (`variety_group_id`,`variety_name`),
  KEY `idx_v_variety_group_id` (`variety_group_id`),
  CONSTRAINT `fk_v_vg` FOREIGN KEY (`variety_group_id`) REFERENCES `variety_group` (`variety_group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1438 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. insect 테이블
CREATE TABLE `insect` (
  `insect_id` int NOT NULL AUTO_INCREMENT,
  `crop_id` int NOT NULL,
  `ncpms_insect_key` varchar(20) DEFAULT NULL,
  `insect_species_kor` varchar(150) DEFAULT NULL,
  `insect_species` varchar(150) DEFAULT NULL,
  `insect_species_code` varchar(50) DEFAULT NULL,
  `tgt_vrmn_name` varchar(150) DEFAULT NULL,
  `insect_order` varchar(100) DEFAULT NULL,
  `insect_family` varchar(100) DEFAULT NULL,
  `insect_genus` varchar(100) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`insect_id`),
  KEY `idx_insect_crop` (`crop_id`),
  CONSTRAINT `fk_insect_crop` FOREIGN KEY (`crop_id`) REFERENCES `crop` (`crop_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1477 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. insect_detail 테이블
CREATE TABLE `insect_detail` (
  `insect_id` int NOT NULL,
  `distrb_info` text,
  `stle_info` text,
  `ecology_info` text,
  `damage_info` text,
  `qrant_info` text,
  `prevent_method` text,
  `biology_prvnbe_mth` text,
  `chemical_prvnbe_mth` text,
  `etc` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`insect_id`),
  CONSTRAINT `fk_insect_detail` FOREIGN KEY (`insect_id`) REFERENCES `insect` (`insect_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. disease 테이블
CREATE TABLE `disease` (
  `disease_id` int NOT NULL AUTO_INCREMENT,
  `crop_id` int NOT NULL,
  `ncpms_sick_key` varchar(20) DEFAULT NULL,
  `sick_name_kor` varchar(150) NOT NULL,
  `sick_name_eng` varchar(150) DEFAULT NULL,
  `sick_name_chn` varchar(150) DEFAULT NULL,
  `sort_order2` int DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`disease_id`),
  KEY `idx_disease_crop` (`crop_id`),
  CONSTRAINT `fk_disease_crop` FOREIGN KEY (`crop_id`) REFERENCES `crop` (`crop_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2273 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. disease_detail 테이블
CREATE TABLE `disease_detail` (
  `disease_id` int NOT NULL,
  `infection_route` varchar(255) DEFAULT NULL,
  `development_condition` text,
  `symptoms` text,
  `prevention_method` text,
  `biology_prvnbe_mth` text,
  `chemical_prvnbe_mth` text,
  `virus_name` varchar(255) DEFAULT NULL,
  `etc` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`disease_id`),
  CONSTRAINT `fk_disease_detail` FOREIGN KEY (`disease_id`) REFERENCES `disease` (`disease_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. crop_cultivation 테이블
CREATE TABLE `crop_cultivation` (
  `crop_cultivation_id` int NOT NULL AUTO_INCREMENT,
  `crop_id` int NOT NULL,
  `cultivation_method` text,
  `harvest_season` text,
  `notes` text,
  `standard_ability` text,
  PRIMARY KEY (`crop_cultivation_id`),
  UNIQUE KEY `uk_crop_cultivation_crop` (`crop_id`),
  KEY `idx_crop_cultivation_crop_id` (`crop_id`),
  CONSTRAINT `fk_crop_cultivation_crop_id` FOREIGN KEY (`crop_id`) REFERENCES `crop` (`crop_id`)
) ENGINE=InnoDB AUTO_INCREMENT=185 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. wildlife_species 테이블
CREATE TABLE `wildlife_species` (
	`ID` BIGINT NOT NULL AUTO_INCREMENT,
	`common_name` VARCHAR(255) NOT NULL,
	`scientific_name` VARCHAR(255) NOT NULL,
	`image_url` VARCHAR(255) NOT NULL,
	PRIMARY KEY (`ID`)
) ENGINE=InnoDB;

-- 12. habitat_records 테이블
CREATE TABLE `habitat_records` (
	`ID` BIGINT NOT NULL AUTO_INCREMENT,
	`species_id` BIGINT NOT NULL,
	`latitude` DECIMAL(10, 8) NOT NULL,
	`longitude` DECIMAL(11, 8) NOT NULL,
	`geometry_layer` TEXT NOT NULL,
	`trace_evidence` TEXT NOT NULL,
	`habitat_description` TEXT NOT NULL,
	`legal_dong_code` VARCHAR(255) NOT NULL,
	`ID2` BIGINT NOT NULL,
	PRIMARY KEY (`ID`),
	CONSTRAINT `FK_wildlife_species_TO_habitat_records` FOREIGN KEY (`species_id`) 
		REFERENCES `wildlife_species` (`ID`)
) ENGINE=InnoDB;

-- 13. regional_retail_prices 테이블
CREATE TABLE `regional_retail_prices` (
	`ID` INT NOT NULL AUTO_INCREMENT,
	`region_name` VARCHAR(255) NOT NULL,
	`item_name` VARCHAR(255) NOT NULL,
	`variey_name` VARCHAR(255) NOT NULL,
	`unit` VARCHAR(255) NOT NULL,
	`current_price` INT NOT NULL,
	`price_1d_ago` INT NOT NULL,
	`price_1w_ago` INT NOT NULL,
	`price_1m_ago` INT NOT NULL,
	`price_1y_ago` INT NOT NULL,
	`price_avg_year` INT NOT NULL,
	`price_normal_day` INT NOT NULL,
	`inquiry_date` INT NOT NULL,
	PRIMARY KEY (`ID`)
) ENGINE=InnoDB;

-- 14. land_transactions 테이블
CREATE TABLE `land_transactions` (
	`land_transactions_id` BIGINT NOT NULL AUTO_INCREMENT,
	`region_code` INT NOT NULL,
	`land_category` DECIMAL(10, 8) NOT NULL,
	`sigungu` DECIMAL(11, 8) NOT NULL,
	`legal_dong` TEXT NOT NULL,
	`lot_number` TEXT NOT NULL,
	`transaction_area` TEXT NOT NULL,
	`transaction_amount` VARCHAR(255) NOT NULL,
	`transaction_date` TIMESTAMP NULL,
	PRIMARY KEY (`land_transactions_id`)
) ENGINE=InnoDB;

-- 15. soil_analysis 테이블
CREATE TABLE `soil_analysis` (
	`soil_analysis_id` BIGINT NOT NULL AUTO_INCREMENT,
	`land_id` INT NOT NULL,
	`ph` DECIMAL(10, 8) NOT NULL,
	`organic_matter` DECIMAL(11, 8) NOT NULL,
	`available_phosphorus` TEXT NOT NULL,
	`potassium` TEXT NOT NULL,
	`calcium` TEXT NOT NULL,
	`magnesium` VARCHAR(255) NOT NULL,
	`available_silicate` INT NULL,
	`analysis_date` TIMESTAMP NULL,
	`ID2` BIGINT NOT NULL,
	PRIMARY KEY (`soil_analysis_id`),
	CONSTRAINT `FK_land_transactions_TO_soil_analysis` FOREIGN KEY (`ID2`) 
		REFERENCES `land_transactions` (`land_transactions_id`)
) ENGINE=InnoDB;


-- 16. conversations 테이블
CREATE TABLE `conversations` (
	`conversation_id` BIGINT NOT NULL AUTO_INCREMENT,
	`title` VARCHAR(255) NOT NULL,
	`status` INT NOT NULL,
	`last_message_at` TIMESTAMP NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`user_id` BIGINT NOT NULL,
	PRIMARY KEY (`conversation_id`),
	CONSTRAINT `FK_users_TO_conversations` FOREIGN KEY (`user_id`) 
		REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

-- 17. conversations_messages 테이블
CREATE TABLE `conversations_messages` (
	`conversations_messages_id` BIGINT NOT NULL AUTO_INCREMENT,
	`role` ENUM('USER','SYSTEM','ASSISTANT') NOT NULL,
	`content` LONGTEXT NOT NULL,
	`metadata` LONGTEXT NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`status` INT NULL,
	`conversation_id` BIGINT NOT NULL,
	PRIMARY KEY (`conversations_messages_id`),
	CONSTRAINT `FK_conversations_TO_messages` FOREIGN KEY (`conversation_id`) 
		REFERENCES `conversations` (`conversation_id`)
) ENGINE=InnoDB;

-- 18. message_pattachments 테이블, 메시지 첨부 안함
-- CREATE TABLE `message_pattachments` (
-- 	`message_attachment_id` BIGINT NOT NULL AUTO_INCREMENT,
-- 	`sort_order` INT NOT NULL,
-- 	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
-- 	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
-- 	PRIMARY KEY (`message_attachment_id`)
-- ) ENGINE=InnoDB;

-- 19. community_post 테이블
CREATE TABLE `community_post` (
	`community_post_id` BIGINT NOT NULL AUTO_INCREMENT,
	`community_post_title` VARCHAR(50) NOT NULL,
	`community_post_content` TEXT NOT NULL,
	`post_type` ENUM('COMMUNITY') NOT NULL,
	`liked_count` INT NOT NULL DEFAULT 0,
	`reported_count` INT NOT NULL DEFAULT 0,
	`view_count` INT NOT NULL DEFAULT 0,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
	`deleted_at` TIMESTAMP NULL,
	`files_tf` ENUM('Y', 'N') NOT NULL,
	`writer_id` BIGINT NOT NULL,
	PRIMARY KEY (`community_post_id`),
	CONSTRAINT `FK_users_TO_community_post` FOREIGN KEY (`writer_id`) 
		REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

-- 20. comment 테이블
CREATE TABLE `comment` (
	`comment_id` BIGINT NOT NULL AUTO_INCREMENT,
	`comment_content` VARCHAR(255) NULL,
	`created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
	`delete_yn` ENUM('Y', 'N') NULL,
	`writer_id` BIGINT NOT NULL,
	`post_id` BIGINT NOT NULL,
	`parent_id` BIGINT NULL,
	PRIMARY KEY (`comment_id`),
	CONSTRAINT `FK_users_TO_comment` FOREIGN KEY (`writer_id`) 
		REFERENCES `users` (`user_id`),
	CONSTRAINT `FK_community_post_TO_comment` FOREIGN KEY (`post_id`) 
		REFERENCES `community_post` (`community_post_id`),
	CONSTRAINT `FK_comment_TO_comment` FOREIGN KEY (`parent_id`) 
		REFERENCES `comment` (`comment_id`)
) ENGINE=InnoDB;

-- 21. recomment 테이블 삭제. self join으로 comment table에 병합. ( N + 1 문제 방지 위해. )
-- CREATE TABLE `recomment` (
-- 	`recomment_id` BIGINT NOT NULL AUTO_INCREMENT,
-- 	`recomment_content` TEXT NULL,
-- 	`created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
-- 	`updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
-- 	`delete_yn` ENUM('Y', 'N') NULL,
-- 	`comment_id` BIGINT NOT NULL,
-- 	`recomment_writer_id` BIGINT NOT NULL,
-- 	PRIMARY KEY (`recomment_id`),
-- 	CONSTRAINT `FK_comment_TO_recomment` FOREIGN KEY (`comment_id`) 
-- 		REFERENCES `comment` (`comment_id`),
-- 	CONSTRAINT `FK_users_TO_recomment` FOREIGN KEY (`recomment_writer_id`) 
-- 		REFERENCES `users` (`user_id`)
-- ) ENGINE=InnoDB;

-- 22. post_like 테이블
CREATE TABLE `post_like` (
	`post_like_id` BIGINT NOT NULL AUTO_INCREMENT,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`post_id` BIGINT NOT NULL,
	`user_id` BIGINT NOT NULL,
	PRIMARY KEY (`post_like_id`),
	CONSTRAINT `FK_community_post_TO_post_like` FOREIGN KEY (`post_id`) 
		REFERENCES `community_post` (`community_post_id`),
	CONSTRAINT `FK_users_TO_post_like` FOREIGN KEY (`user_id`) 
		REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

-- 23. post_report 테이블
CREATE TABLE `post_report` (
	`post_report_id` BIGINT NOT NULL AUTO_INCREMENT,
	`report_type` ENUM('SPAM', 'INAPPROPRIATE_CONTENT', 'ABUSIVE_LANGUAGE', 'POLITICAL_ISSUE', 'OTHER') NULL,
	`report_title` VARCHAR(50) NULL,
	`report_content` TEXT NULL,
	`reported_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`report_user_id` BIGINT NOT NULL,
	`target_post_id` BIGINT NOT NULL,
	PRIMARY KEY (`post_report_id`),
	CONSTRAINT `FK_users_TO_post_report` FOREIGN KEY (`report_user_id`) 
		REFERENCES `users` (`user_id`),
	CONSTRAINT `FK_community_post_TO_post_report` FOREIGN KEY (`target_post_id`) 
		REFERENCES `community_post` (`community_post_id`)
) ENGINE=InnoDB;

-- 24. notice 테이블
CREATE TABLE `notice` (
	`notice_id` BIGINT NOT NULL AUTO_INCREMENT,
	`notice_title` VARCHAR(50) NOT NULL,
	`notice_content` TEXT NOT NULL,
	`files_tf` ENUM('Y', 'N') NOT NULL,
	`writer_id` BIGINT NOT NULL,
	PRIMARY KEY (`notice_id`),
	CONSTRAINT `FK_users_TO_notice` FOREIGN KEY (`writer_id`) 
		REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

-- 25. inquiry 테이블
CREATE TABLE `inquiry` (
	`inquiry_id` BIGINT NOT NULL AUTO_INCREMENT,
	`inquiry_title` VARCHAR(50) NOT NULL,
	`inquiry_content` TEXT NOT NULL,
	`inquiry_type` ENUM('ACCOUNT', 'TECHNICAL_ISSUE', 'OTHER') NULL,
	`inquiry_status` ENUM('PENDING', 'PROCESSING', 'COMPLETED') NOT NULL,
	`writer_id` BIGINT NOT NULL,
	PRIMARY KEY (`inquiry_id`),
	CONSTRAINT `FK_users_TO_inquiry` FOREIGN KEY (`writer_id`) 
		REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

-- 26. answer 테이블
CREATE TABLE `answer` (
	`answer_id` BIGINT NOT NULL AUTO_INCREMENT,
	`answer_content` TEXT NOT NULL,
	`inquiry_id` BIGINT NOT NULL,
	`answer_user_id` BIGINT NOT NULL,
	PRIMARY KEY (`answer_id`),
	CONSTRAINT `FK_inquiry_TO_answer` FOREIGN KEY (`inquiry_id`) 
		REFERENCES `inquiry` (`inquiry_id`),
	CONSTRAINT `FK_users_TO_answer` FOREIGN KEY (`answer_user_id`) 
		REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

-- 27. farm_log 테이블
CREATE TABLE `farm_log` (
	`farm_log_id` BIGINT NOT NULL AUTO_INCREMENT,
	`field` VARCHAR(255) NULL,
	`crop_variety` VARCHAR(255) NULL,
	`crop_type` VARCHAR(255) NULL,
	`work_content` VARCHAR(255) NULL,
	`work_stage` VARCHAR(255) NULL,
	`temperature` INT NOT NULL,
	`precipitation` INT NOT NULL,
	`humidity` INT NOT NULL,
	`start_at` TIMESTAMP NOT NULL,
	`end_at` TIMESTAMP NOT NULL,
	`files_tf` ENUM('Y', 'N') NOT NULL,
	`user_id` BIGINT NOT NULL,
	PRIMARY KEY (`farm_log_id`),
	CONSTRAINT `FK_users_TO_farm_log` FOREIGN KEY (`user_id`) 
		REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

-- 28. files 테이블
CREATE TABLE `files` (
	`file_id` BIGINT NOT NULL AUTO_INCREMENT,
	`bucket_name` VARCHAR(255) NOT NULL,
	`object_key` VARCHAR(255) NOT NULL,
	`original_file_name` VARCHAR(255) NOT NULL,
	`content_type` VARCHAR(255) NOT NULL,
	`owner_type` ENUM('COMMUNITY_POST','NOTICE','FARM_LOG') NOT NULL,
	`owner_id` BIGINT NOT NULL,
	`file_size` BIGINT NOT NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (`file_id`)
) ENGINE=InnoDB;

-- 29. user_report 테이블
CREATE TABLE `user_report` (
	`report_id` BIGINT NOT NULL AUTO_INCREMENT,
	`report_type` ENUM('SPAM','INAPPROPRIATE_CONTENT','ABUSIVE_LANGUAGE','POLITICAL_ISSUE','OTHER') NOT NULL,
	`reported_content` TEXT NOT NULL,
	`evidence_pk_id` BIGINT NOT NULL,
	`reported_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`report_user_id` BIGINT NOT NULL,
	`reported_user_id` BIGINT NOT NULL,
	PRIMARY KEY (`report_id`),
	CONSTRAINT `FK_users_TO_user_report_reporter` FOREIGN KEY (`report_user_id`) 
		REFERENCES `users` (`user_id`),
	CONSTRAINT `FK_users_TO_user_report_reported` FOREIGN KEY (`reported_user_id`) 
		REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

-- 30. penalty 테이블
CREATE TABLE `penalty` (
	`penalty_id` BIGINT NOT NULL AUTO_INCREMENT,
	`penalty_reason` TEXT NULL,
	`penalty_type` ENUM('WARNING','TEMP_BAN','PERM_BAN') NOT NULL,
	`penalized_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`penalized_user_id` BIGINT NOT NULL,
	`admin_id` BIGINT NOT NULL,
	PRIMARY KEY (`penalty_id`),
	CONSTRAINT `FK_users_TO_penalty_penalized` FOREIGN KEY (`penalized_user_id`) 
		REFERENCES `users` (`user_id`),
	CONSTRAINT `FK_users_TO_penalty_admin` FOREIGN KEY (`admin_id`) 
		REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

-- 31. blacklist 테이블
CREATE TABLE `blacklist` (
	`black_id` BIGINT NOT NULL AUTO_INCREMENT,
	`blacked_user_id` BIGINT NOT NULL,
	`admin_id` BIGINT NOT NULL,
	`create_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (`black_id`),
	CONSTRAINT `FK_users_TO_blacklist_blacked` FOREIGN KEY (`blacked_user_id`) 
		REFERENCES `users` (`user_id`),
	CONSTRAINT `FK_users_TO_blacklist_admin` FOREIGN KEY (`admin_id`) 
		REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;


 
