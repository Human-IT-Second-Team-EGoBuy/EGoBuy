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
	`category_id` BIGINT NOT NULL AUTO_INCREMENT,
	`category_name` VARCHAR(255) NOT NULL,
	`status` INT NOT NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`category_id`)
) ENGINE=InnoDB;

-- 3. crop 테이블
CREATE TABLE `crop` (
	`crop_id` BIGINT NOT NULL AUTO_INCREMENT,
	`crop_name` VARCHAR(255) NOT NULL,
	`crop_code` VARCHAR(255) NOT NULL,
	`status` INT NOT NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`category_id` BIGINT NOT NULL,
	PRIMARY KEY (`crop_id`),
	CONSTRAINT `FK_crop_categories_TO_crop` FOREIGN KEY (`category_id`) 
		REFERENCES `crop_categories` (`category_id`)
) ENGINE=InnoDB;

-- 4. variety_group 테이블
CREATE TABLE `variety_group` (
	`variety_group_id` BIGINT NOT NULL AUTO_INCREMENT,
	`group_name` VARCHAR(255) NOT NULL,
	`status` INT NOT NULL DEFAULT 1,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`crop_id` BIGINT NOT NULL,
	PRIMARY KEY (`variety_group_id`),
	CONSTRAINT `FK_crop_TO_variety_group` FOREIGN KEY (`crop_id`) 
		REFERENCES `crop` (`crop_id`)
) ENGINE=InnoDB;

-- 5. variety 테이블
CREATE TABLE `variety` (
	`variety_id` BIGINT NOT NULL AUTO_INCREMENT,
	`variety_name` VARCHAR(255) NOT NULL,
	`usage` VARCHAR(255) NULL,
	`lineage_name` VARCHAR(255) NULL,
	`mother` VARCHAR(255) NULL,
	`father` VARCHAR(255) NULL,
	`adapt_region` VARCHAR(255) NULL,
	`main_features` TEXT NULL,
	`variety_group_id` BIGINT NOT NULL,
	PRIMARY KEY (`variety_id`),
	CONSTRAINT `FK_variety_group_TO_variety` FOREIGN KEY (`variety_group_id`) 
		REFERENCES `variety_group` (`variety_group_id`)
) ENGINE=InnoDB;

-- 6. insect 테이블
CREATE TABLE `insect` (
	`insect_key` BIGINT NOT NULL AUTO_INCREMENT,
	`ncpms_insect_key` VARCHAR(255) NULL,
	`insect_species_code` VARCHAR(255) NULL,
	`insect_species_kor` VARCHAR(255) NULL,
	`insect_species` VARCHAR(255) NULL,
	`field` VARCHAR(255) NULL,
	`insect_family` VARCHAR(255) NULL,
	`insect_order` VARCHAR(255) NULL,
	`insect_genus` VARCHAR(255) NULL,
	`tgt_vrmn_name` VARCHAR(255) NULL,
	`status` INT NOT NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`crop_id` BIGINT NOT NULL,
	PRIMARY KEY (`insect_key`),
	CONSTRAINT `FK_crop_TO_insect` FOREIGN KEY (`crop_id`) 
		REFERENCES `crop` (`crop_id`)
) ENGINE=InnoDB;

-- 7. insect_detail 테이블
CREATE TABLE `insect_detail` (
	`insect_key` BIGINT NOT NULL AUTO_INCREMENT,
	`distrb_info` TEXT NULL,
	`stle_info` TEXT NULL,
	`ecology_info` TEXT NULL,
	`damage_info` TEXT NULL,
	`qrant_info` TEXT NULL,
	`prevent_method` TEXT NULL,
	`biology_prvnbe_mth` TEXT NULL,
	`chemical_prvnbe_mth` TEXT NULL,
	`insect_subspecies` VARCHAR(255) NULL,
	`insect_subgenus` VARCHAR(255) NULL,
	`etc` TEXT NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`insect_key`),
	CONSTRAINT `FK_insect_TO_insect_detail` FOREIGN KEY (`insect_key`) 
		REFERENCES `insect` (`insect_key`)
) ENGINE=InnoDB;

-- 8. disease 테이블
CREATE TABLE `disease` (
	`disease_id` BIGINT NOT NULL AUTO_INCREMENT,
	`ncpms_sick_key` VARCHAR(255) NULL,
	`sick_name_kor` VARCHAR(255) NOT NULL,
	`sick_name_eng` VARCHAR(255) NULL,
	`sick_name_chn` VARCHAR(255) NULL,
	`status` INT NOT NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`sort_order2` INT NULL,
	`crop_id` BIGINT NOT NULL,
	PRIMARY KEY (`disease_id`),
	CONSTRAINT `FK_crop_TO_disease` FOREIGN KEY (`crop_id`) 
		REFERENCES `crop` (`crop_id`)
) ENGINE=InnoDB;

-- 9. disease_detail 테이블
CREATE TABLE `disease_detail` (
	`disease_id` BIGINT NOT NULL AUTO_INCREMENT,
	`infection_route` VARCHAR(255) NULL,
	`development_condition` TEXT NULL,
	`symptoms` TEXT NULL,
	`prevention_method` TEXT NULL,
	`biology_prvnbe_mth` TEXT NULL,
	`chemical_prvnbe_mth` TEXT NULL,
	`virus_name` VARCHAR(255) NULL,
	`etc` TEXT NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`disease_id`),
	CONSTRAINT `FK_disease_TO_disease_detail` FOREIGN KEY (`disease_id`) 
		REFERENCES `disease` (`disease_id`)
) ENGINE=InnoDB;

-- 10. crop_cultivation 테이블
CREATE TABLE `crop_cultivation` (
	`crop_cultivation_id` BIGINT NOT NULL AUTO_INCREMENT,
	`cultivation_method` TEXT NULL,
	`harvest_season` TEXT NULL,
	`notes` TEXT NULL,
	`standard_ability` TEXT NULL,
	`crop_id` BIGINT NOT NULL,
	PRIMARY KEY (`crop_cultivation_id`),
	CONSTRAINT `FK_crop_TO_crop_cultivation` FOREIGN KEY (`crop_id`) 
		REFERENCES `crop` (`crop_id`)
) ENGINE=InnoDB;

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
	`content` TEXT NOT NULL,
	`metadata` JSON NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`status` INT NULL,
	`conversation_id` BIGINT NOT NULL,
	PRIMARY KEY (`conversations_messages_id`),
	CONSTRAINT `FK_conversations_TO_messages` FOREIGN KEY (`conversation_id`) 
		REFERENCES `conversations` (`conversation_id`)
) ENGINE=InnoDB;

-- 18. message_pattachments 테이블
CREATE TABLE `message_pattachments` (
	`message_attachment_id` BIGINT NOT NULL AUTO_INCREMENT,
	`sort_order` INT NOT NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`message_attachment_id`)
) ENGINE=InnoDB;

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
		REFERENCES `users` (`user_id`),
		CONSTRAINT `UK_POST_USER` UNIQUE (`post_id`, `user_id`)
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
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP NOT NULL,
	`updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP NULL,
	PRIMARY KEY (`notice_id`),
	CONSTRAINT `FK_users_TO_notice` FOREIGN KEY (`writer_id`) 
		REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

-- 25. inquiry 테이블
CREATE TABLE `inquiry` (
	`inquiry_id` BIGINT NOT NULL AUTO_INCREMENT,
	`inquiry_title` VARCHAR(50) NOT NULL,
	`inquiry_content` TEXT NOT NULL,
	`inquiry_type` ENUM('ACCOUNT', 'TECHNICAL_ISSUE', 'OTHER') NOT NULL,
	`inquiry_status` ENUM('PENDING', 'PROCESSING', 'COMPLETED') NOT NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP NOT NULL,
	`answered_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
	`writer_id` BIGINT NOT NULL,
	PRIMARY KEY (`inquiry_id`),
	CONSTRAINT `FK_users_TO_inquiry` FOREIGN KEY (`writer_id`) 
		REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

-- 26. answer 테이블
CREATE TABLE `answer` (
	`answer_id` BIGINT NOT NULL AUTO_INCREMENT,
	`answer_content` TEXT NOT NULL,
	`is_deleted` ENUM('Y', 'N') NOT NULL,
	`inquiry_id` BIGINT NOT NULL,
	`answer_user_id` BIGINT NOT NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
	`deleted_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
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


 
