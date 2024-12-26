-- Create punishments table

CREATE TABLE IF NOT EXISTS punishments
(
    id TEXT PRIMARY KEY,
    moderator TEXT NOT NULL,
    player TEXT NOT NULL,
    reason TEXT NOT NULL,
    type INTEGER NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    duration INTEGER NOT NULL,
    active INTEGER NOT NULL,
    notes TEXT,
    reverted_by TEXT,
    reverted_at INTEGER,
    reverted_reason TEXT
);