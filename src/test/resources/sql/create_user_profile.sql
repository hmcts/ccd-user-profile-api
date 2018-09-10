INSERT INTO jurisdiction(id) VALUES ('TEST1');
INSERT INTO jurisdiction(id) VALUES ('TEST2');
INSERT INTO jurisdiction(id) VALUES ('TEST3');

INSERT INTO user_profile(id, work_basket_default_jurisdiction, work_basket_default_case_type, work_basket_default_state)
VALUES ('user1', 'TEST2', 'case', 'state');

INSERT INTO user_profile(id, work_basket_default_jurisdiction, work_basket_default_case_type, work_basket_default_state)
VALUES ('user5', 'TEST2', 'case', 'state');

INSERT INTO user_profile(id) VALUES ('user2');
INSERT INTO user_profile(id) VALUES ('user4');

INSERT INTO user_profile_jurisdiction(user_profile_id, jurisdiction_id) VALUES ('user1', 'TEST1');
INSERT INTO user_profile_jurisdiction(user_profile_id, jurisdiction_id) VALUES ('user1', 'TEST2');
INSERT INTO user_profile_jurisdiction(user_profile_id, jurisdiction_id) VALUES ('user1', 'TEST3');
INSERT INTO user_profile_jurisdiction(user_profile_id, jurisdiction_id) VALUES ('user4', 'TEST1');
INSERT INTO user_profile_jurisdiction(user_profile_id, jurisdiction_id) VALUES ('user5', 'TEST2');
