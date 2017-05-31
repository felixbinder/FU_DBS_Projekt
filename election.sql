CREATE TABLE Tweet (
	ID 		SERIAL PRIMARY KEY NOT NULL,
	handle 		VARCHAR(50) NOT NULL,
	text 		VARCHAR(300) NOT NULL,
	time 		TIMESTAMP NOT NULL,
	is_retweet 	BOOLEAN NOT NULL,
	is_quote_status BOOLEAN NOT NULL,
	retweet_count 	INT,
	favorite_count 	INT
);

CREATE TABLE Hashtag (
	Name		VARCHAR(50) PRIMARY KEY NOT NULL,
	Anzahl_global	INT
);

CREATE TABLE T_enth_H(
	Tweet_ID 	INT 		REFERENCES Tweet (ID),
	H_Name 		VARCHAR(50) 	REFERENCES Hashtag(Name),
	wie_oft 	INT,
	PRIMARY KEY (Tweet_ID, H_Name)
);

CREATE TABLE Hashtag_Paare (
	ID	SERIAL PRIMARY KEY NOT NULL,
	name1	VARCHAR(50)	REFERENCES Hashtag(Name),
	name2	VARCHAR(50) 	REFERENCES Hashtag(Name),
	Anzahl_global INT,
	CHECK (lower(name1) < lower(name2))
);

CREATE TABLE Hashtags_bilden_HP(
	Hash_Name	VARCHAR(50)	REFERENCES Hashtag(Name),
	HP_ID		INT		REFERENCES Hashtag_Paare(ID),
	PRIMARY KEY(Hash_Name, HP_ID)
);

CREATE TABLE T_enth_HP (
	Tweet_ID	INT		REFERENCES Tweet (ID),
	Hashpaar_ID	INT		REFERENCES Hashtag_Paare (ID),
	wie_oft		INT NOT NULL,
	PRIMARY KEY (Tweet_ID, Hashpaar_ID)
);
