ALTER TABLE notifications
    ADD COLUMN business_key VARCHAR(500);

WITH ranked_notifications AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY source, notification_type, user_id, reference_id
            ORDER BY created_at, id
        ) AS occurrence
    FROM notifications
)
UPDATE notifications notification
SET business_key = notification.source
        || '|'
        || notification.notification_type
        || '|'
        || notification.user_id
        || '|'
        || COALESCE(notification.reference_id, 'EVENT:' || notification.event_id)
FROM ranked_notifications ranked
WHERE notification.id = ranked.id
  AND ranked.occurrence = 1;

CREATE UNIQUE INDEX uk_notifications_business_key
    ON notifications(business_key)
    WHERE business_key IS NOT NULL;
