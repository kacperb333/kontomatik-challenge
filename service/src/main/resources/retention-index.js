db.sessions.createIndex(
    {"persistedAt": 1},
    {expireAfterSeconds: 86400}
)
