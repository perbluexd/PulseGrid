INSERT INTO dashboard_users (id, email, password_hash, role, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'percy@gmail.com',
    '$argon2id$v=19$m=16384,t=2,p=1$dB9styw8vjacZFrwD+UvFw$xrrjS1wbok9tg/MfsSy5Lv/kreuhzg2KCcPq24YCMNA',
    'ADMIN',
    true,
    now(),
    now()
);
