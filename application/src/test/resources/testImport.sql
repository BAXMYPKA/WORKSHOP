-- HIBERNATE SPECIAL IMPORT AFTER ALL THE SCHEMAS HAVE BEEN SET FROM 'schema.sql' AND THE TABLES HAVE BEEN CREATED AUTOMATICALLY
-- SUPPORTS ONLY INLINE STRINGS!
-- TO ALLOW HIBERNATE READ MULTILINES SQL STATEMENTS ADD TO THE SPRING BOOT application.properties:
-- spring.jpa.properties.hibernate.hbm2ddl.import_files_sql_extractor=org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor
CREATE SCHEMA IF NOT EXISTS INTERNAL;
CREATE SCHEMA IF NOT EXISTS EXTERNAL;
;
INSERT INTO INTERNAL.DEPARTMENTS (ID, NAME, CREATED)
VALUES (2001, 'THE TEST SITE MANAGEMENT', CURRENT_TIMESTAMP());
INSERT INTO INTERNAL.DEPARTMENTS (ID, NAME, CREATED)
VALUES (2002, 'THE TEST HR', CURRENT_TIMESTAMP());
INSERT INTO INTERNAL.DEPARTMENTS (ID, NAME, CREATED)
VALUES (2003, 'THE TEST MANAGEMENT', CURRENT_TIMESTAMP());
INSERT INTO INTERNAL.DEPARTMENTS (ID, NAME, CREATED)
VALUES (2004, 'THE TEST TECHNICAL DEPARTMENT', CURRENT_TIMESTAMP());
;
INSERT INTO INTERNAL.POSITIONS (ID, NAME, DESCRIPTION, CREATED, DEPARTMENT_ID)
VALUES (1011, 'Test Administrator', 'Администратор сайта с доступом ко всем разделам.', CURRENT_TIMESTAMP(), 2001);
INSERT INTO INTERNAL.POSITIONS (ID, NAME, DESCRIPTION, CREATED, DEPARTMENT_ID)
VALUES (1012, 'Test Senior personnel manager', 'Старший работник отдела кадров', CURRENT_TIMESTAMP(), 2002);
INSERT INTO INTERNAL.POSITIONS (ID, NAME, DESCRIPTION, CREATED, DEPARTMENT_ID)
VALUES (1013, 'Test Personnel manager', 'Работник отдела кадров', CURRENT_TIMESTAMP(), 2002);
INSERT INTO INTERNAL.POSITIONS (ID, NAME, DESCRIPTION, CREATED, DEPARTMENT_ID)
VALUES (1014, 'Test Senior manager', 'Старший менеджер по управлению заказами', CURRENT_TIMESTAMP(), 2003);
INSERT INTO INTERNAL.POSITIONS (ID, NAME, DESCRIPTION, CREATED, DEPARTMENT_ID)
VALUES (1015, 'Test Manager', 'Менеджер по управлению заказами', CURRENT_TIMESTAMP(), 2003);
INSERT INTO INTERNAL.POSITIONS (ID, NAME, DESCRIPTION, CREATED, DEPARTMENT_ID)
VALUES (1016, 'Test Senior technician', 'Старший техник', CURRENT_TIMESTAMP(), 2004);
INSERT INTO INTERNAL.POSITIONS (ID, NAME, DESCRIPTION, CREATED, DEPARTMENT_ID)
VALUES (1017, 'Test Technician', 'Техник', CURRENT_TIMESTAMP(), 2004);;
/*
-- ALL PASSWORDS ARE THE SAME AS BCryptPasswordEncoder.encode STRING "12345" FOR THE SIMPLICITY
*/
INSERT INTO INTERNAL.EMPLOYEES (ID, BIRTHDAY, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD, EMPLOYED, POSITION_ID)
VALUES (1021, DATE '1970-02-14', 'testadmin@workshop.pro', 'Feofan', 'Mudovski',
		'$2a$10$3ZP75a6hiK0jjPgIYSqDPeAHy954ynYFDK6OibhJg4Wc4F4JfVOqa', CURRENT_TIMESTAMP(), 1011);
INSERT INTO INTERNAL.EMPLOYEES (ID, BIRTHDAY, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD, EMPLOYED, POSITION_ID)
VALUES (1022, DATE '1985-11-18', 'testneotlozhka@workshop.pro', 'Ambulatoriya', 'Neotlozhnaya',
		'$2a$10$IOQse1xvam3Wqm6MC5fc4.IxZNTnNLQXiRR4CRkOQ8ynkJakzHqqu', CURRENT_TIMESTAMP(), 1012);
INSERT INTO INTERNAL.EMPLOYEES (ID, BIRTHDAY, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD, EMPLOYED, POSITION_ID)
VALUES (1023, DATE '1999-01-05', 'testbozhena@workshop.pro', 'Skolota', 'Bozhenova',
		'$2a$10$xT0XO4QAEwkdBQpExuYOXOq0uzSkxBvFiwPKXapsziUoGFQW0B4xS', CURRENT_TIMESTAMP(), 1013);
INSERT INTO INTERNAL.EMPLOYEES (ID, BIRTHDAY, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD, EMPLOYED, POSITION_ID)
VALUES (1024, DATE '1991-10-09', 'testpetro@workshop.pro', 'Oktavian', 'Petrov',
		'$2a$10$zvHjaq7bDWCD' || '.mAm/RPgDOK1F6Mp7RK5OvJPVyZpl96bbForV/LcG', CURRENT_TIMESTAMP(), 1013);
INSERT INTO INTERNAL.EMPLOYEES (ID, BIRTHDAY, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD, EMPLOYED, POSITION_ID)
VALUES (1025, DATE '1973-03-21', 'testsobolev@workshop.pro', 'Kirill', 'Sobolev',
		'$2a$10$EgyGzvUTWH' || '.uOqTT6LkzgeixVYug7ixwZ85TbqXvtyoudCIlSP1v.', CURRENT_TIMESTAMP(), 1014);
INSERT INTO INTERNAL.EMPLOYEES (ID, BIRTHDAY, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD, EMPLOYED, POSITION_ID)
VALUES (1026, DATE '1983-07-25', 'testkukushkin@workshop.pro', 'Leonid', 'Kukishkind',
		'$2a$10$5pGjiQHPqtinyM0PaIwjx' || '.NdNfK6vbBlzIk.6sLW9Z3UEmbj5Rz12', CURRENT_TIMESTAMP(), 1015);
INSERT INTO INTERNAL.EMPLOYEES (ID, BIRTHDAY, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD, EMPLOYED, POSITION_ID)
VALUES (1027, DATE '1979-08-20', 'testpuzdoy@workshop.pro', 'Semion', 'Puzdoy',
		'$2a$10$9wFRh0Sr4bu2JmWWX/3pjetFJOPmJf4kj2EyU65IxJUiN8TDqILH.', CURRENT_TIMESTAMP(), 1016);
;
;
INSERT INTO INTERNAL.PHONES (ID, NAME, PHONE, EMPLOYEE_ID, CREATED)
VALUES (1031, 'Mobile', '1-234-56-351', 1021, CURRENT_TIMESTAMP());
INSERT INTO INTERNAL.PHONES (ID, NAME, PHONE, EMPLOYEE_ID, CREATED)
VALUES (1032, 'Mobile', '1-863-45-231', 1022, CURRENT_TIMESTAMP());
INSERT INTO INTERNAL.PHONES (ID, NAME, PHONE, EMPLOYEE_ID, CREATED)
VALUES (1033, 'Mobile', '1-843-42-581', 1022, CURRENT_TIMESTAMP());
INSERT INTO INTERNAL.PHONES (ID, NAME, PHONE, EMPLOYEE_ID, CREATED)
VALUES (1034, 'Mobile', '1-356-35-381', 1023, CURRENT_TIMESTAMP());
INSERT INTO INTERNAL.PHONES (ID, NAME, PHONE, EMPLOYEE_ID, CREATED)
VALUES (1035, 'Mobile', '1-864-34-341', 1024, CURRENT_TIMESTAMP());
INSERT INTO INTERNAL.PHONES (ID, NAME, PHONE, EMPLOYEE_ID, CREATED)
VALUES (1036, 'Mobile', '1-883-25-531', 1024, CURRENT_TIMESTAMP());
INSERT INTO INTERNAL.PHONES (ID, NAME, PHONE, EMPLOYEE_ID, CREATED)
VALUES (1037, 'Mobile', '1-267-46-531', 1025, CURRENT_TIMESTAMP());
INSERT INTO INTERNAL.PHONES (ID, NAME, PHONE, EMPLOYEE_ID, CREATED)
VALUES (1038, 'Mobile', '1-927-93-821', 1025, CURRENT_TIMESTAMP());
INSERT INTO INTERNAL.PHONES (ID, NAME, PHONE, EMPLOYEE_ID, CREATED)
VALUES (1039, 'Mobile', '1-294-72-991', 1026, CURRENT_TIMESTAMP());
INSERT INTO INTERNAL.PHONES (ID, NAME, PHONE, EMPLOYEE_ID, CREATED)
VALUES (1040, 'Mobile', '1-924-77-421', 1027, CURRENT_TIMESTAMP());
INSERT INTO INTERNAL.PHONES (ID, NAME, PHONE, EMPLOYEE_ID, CREATED)
VALUES (1041, 'Mobile', '1-864-72-621', 1027, CURRENT_TIMESTAMP());
;
;
INSERT INTO EXTERNAL.USERS (ID, BIRTHDAY, CREATED, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD)
VALUES (1091, CURRENT_DATE, CURRENT_TIMESTAMP(), 'testuser1One@mail.pro', 'UserOneFirst', 'UserOneLast',
		'$2a$10$xT0XO4QAEwkdBQpExuYOXOq0uzSkxBvFiwPKXapsziUoGFQW0B4xS');
INSERT INTO EXTERNAL.USERS (ID, BIRTHDAY, CREATED, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD)
VALUES (1092, CURRENT_DATE, CURRENT_TIMESTAMP(), 'testuser2Two@meail.pro', 'UserTwoFirst', 'UserTwoLast',
		'$2a$10$xT0XO4QAEwkdBQpExuYOXOq0uzSkxBvFiwPKXapsziUoGFQW0B4xS');
INSERT INTO EXTERNAL.USERS (ID, BIRTHDAY, CREATED, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD)
VALUES (1093, CURRENT_DATE, CURRENT_TIMESTAMP(), 'user3Three@mailto.ru', 'UserThreeFirst', 'UserThreeLast',
		'$2a$10$xT0XO4QAEwkdBQpExuYOXOq0uzSkxBvFiwPKXapsziUoGFQW0B4xS');
INSERT INTO EXTERNAL.USERS (ID, BIRTHDAY, CREATED, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD)
VALUES (1094, CURRENT_DATE, CURRENT_TIMESTAMP(), 'testuser4Four@mail.eu', 'UserFourFirst', 'UserFourLast',
		'$2a$10$xT0XO4QAEwkdBQpExuYOXOq0uzSkxBvFiwPKXapsziUoGFQW0B4xS');
INSERT INTO EXTERNAL.USERS (ID, BIRTHDAY, CREATED, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD)
VALUES (1095, CURRENT_DATE, CURRENT_TIMESTAMP(), 'testuser5Five@mailing.org', 'UserFiveFirst', 'UserFiveLast',
		'$2a$10$xT0XO4QAEwkdBQpExuYOXOq0uzSkxBvFiwPKXapsziUoGFQW0B4xS');
INSERT INTO EXTERNAL.USERS (ID, BIRTHDAY, CREATED, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD)
VALUES (1096, CURRENT_DATE, CURRENT_TIMESTAMP(), 'testuser6Six@mail.prod', 'UserSixFirst', 'UserSixLast',
		'$2a$10$xT0XO4QAEwkdBQpExuYOXOq0uzSkxBvFiwPKXapsziUoGFQW0B4xS');
INSERT INTO EXTERNAL.USERS (ID, BIRTHDAY, CREATED, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD)
VALUES (1097, CURRENT_DATE, CURRENT_TIMESTAMP(), 'testuser7Seven@email.lv', 'UserSevenFirst', 'UserSevenLast',
		'$2a$10$xT0XO4QAEwkdBQpExuYOXOq0uzSkxBvFiwPKXapsziUoGFQW0B4xS');
INSERT INTO EXTERNAL.USERS (ID, BIRTHDAY, CREATED, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD)
VALUES (1098, CURRENT_DATE, CURRENT_TIMESTAMP(), 'testuser8Eight@email.alb', 'UserEightFirst', 'UserEightLast',
		'$2a$10$xT0XO4QAEwkdBQpExuYOXOq0uzSkxBvFiwPKXapsziUoGFQW0B4xS');
INSERT INTO EXTERNAL.USERS (ID, BIRTHDAY, CREATED, EMAIL, FIRST_NAME, LAST_NAME, PASSWORD)
VALUES (1099, CURRENT_DATE, CURRENT_TIMESTAMP(), 'testuser9Nine@pro.mail', 'UserNineFirst', 'UserNineLast',
		'$2a$10$xT0XO4QAEwkdBQpExuYOXOq0uzSkxBvFiwPKXapsziUoGFQW0B4xS');
;
;
INSERT INTO INTERNAL.ORDERS (ID, CREATED, DESCRIPTION, CREATED_BY, MODIFIED_BY, CREATED_FOR_ID)
VALUES (1051, CURRENT_TIMESTAMP(), 'Test Order one', 1024, 1025, 1091);
INSERT INTO INTERNAL.ORDERS (ID, CREATED, DESCRIPTION, OVERALL_PRICE, CREATED_BY, MODIFIED_BY, CREATED_FOR_ID)
VALUES (1052, CURRENT_TIMESTAMP(), 'Test Order two', 78.56, 1027, 1025, 1092);
INSERT INTO INTERNAL.ORDERS (ID, CREATED, DESCRIPTION, OVERALL_PRICE, CREATED_BY, CREATED_FOR_ID)
VALUES (1053, CURRENT_TIMESTAMP(), 'Test Order three', 25.50, 1023, 1093);
INSERT INTO INTERNAL.ORDERS (ID, CREATED, DESCRIPTION, OVERALL_PRICE, CREATED_BY, MODIFIED_BY, CREATED_FOR_ID)
VALUES ( 10501, CURRENT_TIMESTAMP(), 'Order three', 25.50, 1024, 1022, 1093 );
INSERT INTO INTERNAL.ORDERS (ID, CREATED, FINISHED, DEADLINE, DESCRIPTION, OVERALL_PRICE, CREATED_BY, MODIFIED_BY, CREATED_FOR_ID)
VALUES ( 10502, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 'Order three', 25.50, 1023, 1024, 1093 );
INSERT INTO INTERNAL.ORDERS (ID, CREATED, FINISHED, DEADLINE, DESCRIPTION, OVERALL_PRICE, CREATED_BY, MODIFIED_BY, CREATED_FOR_ID)
VALUES ( 10503, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 'Order three', 25.50, 1025, 1027, 1093 );
-- --
INSERT INTO INTERNAL.TASKS (ID, CREATED, NAME, PRICE, CREATED_BY, APPOINTED_TO, ORDER_ID)
VALUES ( 10511, CURRENT_TIMESTAMP(), 'The Task One', 50.05, 1022, 1023, 10501 );