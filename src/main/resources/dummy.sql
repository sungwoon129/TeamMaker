/* database 생성 */
create database wauction;

/* database 전환 */
use wauction;

/* # table 생성 */
create table Message (Message_id bigint not null auto_increment, created_date datetime(6), deleted bit not null, deleted_date datetime(6), last_modified_date datetime(6), content varchar(255), channel_id bigint, member_id bigint, primary key (Message_id)) engine=InnoDB;
create table channel (channel_id bigint not null auto_increment, created_date datetime(6), deleted bit not null, deleted_date datetime(6), last_modified_date datetime(6), name varchar(255), primary key (channel_id)) engine=InnoDB;
create table channel_member (channel_member_id bigint not null auto_increment, created_date datetime(6), deleted bit not null, deleted_date datetime(6), last_modified_date datetime(6), channel_id bigint, member_id bigint, primary key (channel_member_id)) engine=InnoDB;
create table member (member_id bigint not null auto_increment, created_date datetime(6), deleted bit not null, deleted_date datetime(6), last_modified_date datetime(6), name varchar(255), primary key (member_id)) engine=InnoDB;

/* member 삽입 */
INSERT INTO `wauction`.`member` (`created_date`, `last_modified_date`, `deleted`, `name`) VALUES (now(), now(), 0, '김가나');
INSERT INTO `wauction`.`member` (`created_date`, `last_modified_date`, `deleted`, `name`) VALUES (now(), now(), 0, '이다라');
INSERT INTO `wauction`.`member` (`created_date`, `last_modified_date`, `deleted`, `name`) VALUES (now(), now(), 0, '박마바');
INSERT INTO `wauction`.`member` (`created_date`, `last_modified_date`, `deleted`, `name`) VALUES (now(), now(), 0, '신사아');
INSERT INTO `wauction`.`member` (`created_date`, `last_modified_date`, `deleted`, `name`) VALUES (now(), now(), 0, '정자차');
INSERT INTO `wauction`.`member` (`created_date`, `last_modified_date`, `deleted`, `name`) VALUES (now(), now(), 0, '윤카타');
INSERT INTO `wauction`.`member` (`created_date`, `last_modified_date`, `deleted`, `name`) VALUES (now(), now(), 0, '연파하');

/* channel 삽입 */
INSERT INTO `wauction`.`channel` (`created_date`, `last_modified_date`, `deleted`, `name`) VALUES (now(), now(), 0, '저녁식사모임');
INSERT INTO `wauction`.`channel` (`created_date`, `last_modified_date`, `deleted`, `name`) VALUES (now(), now(), 0, '스프링초고수단');
INSERT INTO `wauction`.`channel` (`created_date`, `last_modified_date`, `deleted`, `name`) VALUES (now(), now(), 0, '개발자들');

/* channel_member 삽입 */
INSERT INTO `wauction`.`channel_member` (`created_date`, `last_modified_date`, `deleted`, `member_id`, `channel_id`) VALUES (now(), now(), 0, 1, 1);
INSERT INTO `wauction`.`channel_member` (`created_date`, `last_modified_date`, `deleted`, `member_id`, `channel_id`) VALUES (now(), now(), 0, 1, 2);
INSERT INTO `wauction`.`channel_member` (`created_date`, `last_modified_date`, `deleted`, `member_id`, `channel_id`) VALUES (now(), now(), 0, 1, 3);
INSERT INTO `wauction`.`channel_member` (`created_date`, `last_modified_date`, `deleted`, `member_id`, `channel_id`) VALUES (now(), now(), 0, 2, 1);
INSERT INTO `wauction`.`channel_member` (`created_date`, `last_modified_date`, `deleted`, `member_id`, `channel_id`) VALUES (now(), now(), 0, 2, 2);
INSERT INTO `wauction`.`channel_member` (`created_date`, `last_modified_date`, `deleted`, `member_id`, `channel_id`) VALUES (now(), now(), 0, 2, 3);
INSERT INTO `wauction`.`channel_member` (`created_date`, `last_modified_date`, `deleted`, `member_id`, `channel_id`) VALUES (now(), now(), 0, 3, 1);
INSERT INTO `wauction`.`channel_member` (`created_date`, `last_modified_date`, `deleted`, `member_id`, `channel_id`) VALUES (now(), now(), 0, 3, 3);
INSERT INTO `wauction`.`channel_member` (`created_date`, `last_modified_date`, `deleted`, `member_id`, `channel_id`) VALUES (now(), now(), 0, 4, 1);

/* Message 삽입 */
INSERT INTO `wauction`.`Message` (`created_date`, `last_modified_date`, `deleted`, `content`, `member_id`, `channel_id`) VALUES (now(), now(), 0, '50 포인트 입찰', 1, 1);
INSERT INTO `wauction`.`Message` (`created_date`, `last_modified_date`, `deleted`, `content`, `member_id`, `channel_id`) VALUES (now(), now(), 0, '100 포인트 입찰', 2, 1);
INSERT INTO `wauction`.`Message` (`created_date`, `last_modified_date`, `deleted`, `content`, `member_id`, `channel_id`) VALUES (now(), now(), 0, '450 포인트 입찰', 1, 1);
INSERT INTO `wauction`.`Message` (`created_date`, `last_modified_date`, `deleted`, `content`, `member_id`, `channel_id`) VALUES (now(), now(), 0, '455 포인트 입찰', 3, 1);
INSERT INTO `wauction`.`Message` (`created_date`, `last_modified_date`, `deleted`, `content`, `member_id`, `channel_id`) VALUES (now(), now(), 0, '유찰되었습니다.', 2, 1);
INSERT INTO `wauction`.`Message` (`created_date`, `last_modified_date`, `deleted`, `content`, `member_id`, `channel_id`) VALUES (now(), now(), 0, '낙찰되었습니다.', 4, 1);
INSERT INTO `wauction`.`Message` (`created_date`, `last_modified_date`, `deleted`, `content`, `member_id`, `channel_id`) VALUES (now(), now(), 0, '경매를 시작합니다.', 1, 2);
INSERT INTO `wauction`.`Message` (`created_date`, `last_modified_date`, `deleted`, `content`, `member_id`, `channel_id`) VALUES (now(), now(), 0, '경매가 종료되었습니다.', 2, 2);
