CREATE TABLE users
(
    id INTEGER PRIMARY KEY UNIQUE NOT NULL,
    name VARCHAR,
    time_zone INTEGER DEFAULT 0,
    on_off BOOLEAN DEFAULT true
);

CREATE TABLE user_events
(
    user_id INTEGER,
    time timestamp,
    description VARCHAR,
    event_id SERIAL,
    event_freq VARCHAR DEFAULT 'TIME',
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);


CREATE TABLE event_cash
(
    time timestamp,
    description VARCHAR,
    user_id INTEGER,
    id SERIAL
);