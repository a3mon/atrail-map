COPY at_user (id, email, password, role, registration_date, authentication_type) FROM stdin;
1	showcase@example.com	password	user.payed	2017-02-27	email
\.

SELECT pg_catalog.setval('at_user_id_seq', 1, true);

COPY at_preferences (id, "user", direction, start_end, show_whole_trail) FROM stdin;
1	1	nobo	[2017-03-17,2017-09-11)	t
\.

SELECT pg_catalog.setval('at_preferencs_id_seq', 1, true);

COPY at_timeline (id, at_user, at_poi, comment, date) FROM stdin;
1	1	789	\N	2017-03-18
2	1	786	\N	2017-03-19
3	1	501	\N	2017-03-20
\.

SELECT pg_catalog.setval('at_timeline_id_seq', 3, true);