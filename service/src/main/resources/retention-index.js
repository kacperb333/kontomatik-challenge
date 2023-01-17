db.finished_session.createIndex(
    {"createdAt": 1},
    {expireAfterSeconds: 86400}
)
