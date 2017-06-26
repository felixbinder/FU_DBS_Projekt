CREATE TABLE Hashtags_Aehnlichkeit(
    name1 VARCHAR(50) NOT NULL REFERENCES Hashtag(Name),
    name2 VARCHAR(50) NOT NULL REFERENCES Hashtag(Name),
    aehnlichkeit NUMERIC NOT NULL,
    PRIMARY KEY (name1,name2),
    CHECK (lower(name1) < lower(name2))
);


#CHECK (distanz <= 1 AND distanz >= -1)
