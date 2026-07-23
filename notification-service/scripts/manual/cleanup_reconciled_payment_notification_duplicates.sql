-- Manual cleanup only. Review the preview before executing the DELETE statements.
-- These event IDs were created by the 2026-07-23 Payment reconciliation.

BEGIN;

CREATE TEMPORARY TABLE cleanup_notification_events (
    event_id VARCHAR(150) PRIMARY KEY,
    reference_id VARCHAR(150) NOT NULL
) ON COMMIT DROP;

INSERT INTO cleanup_notification_events(event_id, reference_id)
VALUES
    ('82be811a-ad14-3420-a015-1dcf057ec50b', '0aed135a-e6e8-422f-9d80-84066f4adc2e'),
    ('10c887e6-388d-3971-b55f-3478f8de4298', '6d86651d-e6e8-4884-8d13-c35254e88369');

SELECT notification.id,
       notification.event_id,
       notification.reference_id,
       notification.notification_type,
       notification.created_at
FROM notifications notification
JOIN cleanup_notification_events target
  ON target.event_id = notification.event_id
 AND target.reference_id = notification.reference_id
WHERE notification.source = 'PAYMENT'
  AND notification.notification_type = 'PAYMENT_COMPLETED'
  AND EXISTS (
      SELECT 1
      FROM notifications original
      WHERE original.id <> notification.id
        AND original.source = notification.source
        AND original.notification_type = notification.notification_type
        AND original.user_id = notification.user_id
        AND original.reference_id = notification.reference_id
  );

-- Remove the leading comment markers and replace the final ROLLBACK with COMMIT
-- only after validating that the preview returns exactly the two
-- reconciliation-created duplicates listed above.
--
-- DELETE FROM notifications notification
-- USING cleanup_notification_events target
-- WHERE notification.event_id = target.event_id
--   AND notification.reference_id = target.reference_id
--   AND notification.source = 'PAYMENT'
--   AND notification.notification_type = 'PAYMENT_COMPLETED';
--
-- DELETE FROM processed_events processed
-- USING cleanup_notification_events target
-- WHERE processed.event_id = target.event_id;

ROLLBACK;
