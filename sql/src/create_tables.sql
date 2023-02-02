DROP TABLE WORK_EXPR;
DROP TABLE EDUCATIONAL_DETAILS;
DROP TABLE MESSAGE;
DROP TABLE CONNECTION_USR;
DROP TABLE USR;

--- Foreign keys were added as well as changing the character length to 50

CREATE TABLE USR(
	userId varchar(50) UNIQUE NOT NULL,
	password varchar(50) NOT NULL,
	email text NOT NULL,
	name char(50),
	dateOfBirth date,
	PRIMARY KEY(userId)
);

CREATE TABLE WORK_EXPR(
	userId char(50) NOT NULL,
	company char(50) NOT NULL,
	role char(50) NOT NULL,
	location char(50),
	startDate date,
	endDate date,
	PRIMARY KEY(userId,company,role,startDate),
	FOREIGN KEY (userId) REFERENCES USR ON DELETE CASCADE
);

CREATE TABLE EDUCATIONAL_DETAILS(
	userId char(50) NOT NULL,
	instituitionName char(50) NOT NULL,
	major char(50) NOT NULL,
	degree char(50) NOT NULL,
	startdate date,
	enddate date,
	PRIMARY KEY(userId,major,degree),
	FOREIGN KEY (userId) REFERENCES USR ON DELETE CASCADE
);

CREATE SEQUENCE msg_id_seq;

CREATE TABLE MESSAGE(
	msgId integer UNIQUE NOT NULL DEFAULT nextval('msg_id_seq'),
	senderId char(50) NOT NULL,
	receiverId char(50) NOT NULL,
	contents char(500) NOT NULL,
	sendTime timestamp,
	deleteStatus integer,
	status char(30) NOT NULL,
	PRIMARY KEY(msgId),
	FOREIGN KEY (senderId) REFERENCES USR ON DELETE CASCADE,
	FOREIGN KEY (receiverId) REFERENCES USR ON DELETE CASCADE
);

--- Alter sequence to start with an ID +1 more than current from `data_MESSAGE.csv`
ALTER SEQUENCE msg_id_seq RESTART WITH 27812;

CREATE TABLE CONNECTION_USR(
	userId char(50) NOT NULL,
	connectionId char(50) NOT NULL,
	status char(30) NOT NULL,
	PRIMARY KEY(userId,connectionId),
	FOREIGN KEY(connectionId) REFERENCES USR ON DELETE CASCADE,
	FOREIGN KEY(userId) REFERENCES USR ON DELETE CASCADE
);
