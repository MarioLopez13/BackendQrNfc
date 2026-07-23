CREATE USER smartpayut_identity WITH PASSWORD 'identity_dev_password';
CREATE USER smartpayut_wallet WITH PASSWORD 'wallet_dev_password';
CREATE USER smartpayut_payment WITH PASSWORD 'payment_dev_password';
CREATE USER smartpayut_transaction WITH PASSWORD 'transaction_dev_password';
CREATE USER smartpayut_notification WITH PASSWORD 'notification_dev_password';
CREATE USER smartpayut_keycloak WITH PASSWORD 'keycloak_dev_password';

CREATE DATABASE smartpayut_identity OWNER smartpayut_identity;
CREATE DATABASE smartpayut_wallet OWNER smartpayut_wallet;
CREATE DATABASE smartpayut_payment OWNER smartpayut_payment;
CREATE DATABASE smartpayut_transaction OWNER smartpayut_transaction;
CREATE DATABASE smartpayut_notification OWNER smartpayut_notification;
CREATE DATABASE smartpayut_keycloak OWNER smartpayut_keycloak;
