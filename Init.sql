CREATE DATABASE FoxMeet;

USE FoxMeet;

CREATE TABLE Users
(
	userID int NOT NULL PRIMARY KEY,
	emailID varchar(255) NOT NULL UNIQUE
);

CREATE TABLE Events
(
	eventID int NOT NULL PRIMARY KEY ,
	location varchar(240) ,
	start int,
	end int,
	event_name varchar(240) NOT NULL,
	date varchar(50) NOT NULL
);

CREATE TABLE Event_Att
(
	user_ID int NOT NULL,
	event_ID int NOT NULL,
	voted BOOLEAN DEFAULT FALSE ,
	FOREIGN KEY (user_ID) REFERENCES Users(userID),
	FOREIGN KEY (event_ID) REFERENCES Events(eventID)
);
