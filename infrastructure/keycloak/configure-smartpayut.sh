#!/bin/bash
set -euo pipefail

KCADM=/opt/keycloak/bin/kcadm.sh
SERVER_URL="${KEYCLOAK_SERVER_URL:-http://keycloak:8080}"
REALM="${KEYCLOAK_REALM:-smartpayut}"

until "$KCADM" config credentials \
    --server "$SERVER_URL" \
    --realm master \
    --user "$KEYCLOAK_ADMIN" \
    --password "$KEYCLOAK_ADMIN_PASSWORD"; do
    sleep 2
done

identity_client_id="$("$KCADM" get clients \
    -r "$REALM" \
    -q clientId=smartpayut-identity \
    --fields id \
    --format csv \
    --noquotes)"

identity_service_account_id="$("$KCADM" get "clients/$identity_client_id/service-account-user" \
    -r "$REALM" \
    --fields id \
    --format csv \
    --noquotes)"

"$KCADM" add-roles \
    -r "$REALM" \
    --uid "$identity_service_account_id" \
    --cclientid realm-management \
    --rolename manage-users \
    --rolename view-realm

"$KCADM" update users/profile \
    -r "$REALM" \
    -s unmanagedAttributePolicy=ADMIN_EDIT

user_id_mapper_id=""
while IFS=, read -r mapper_id mapper_name; do
    if [ "$mapper_name" = "user-id" ]; then
        user_id_mapper_id="$mapper_id"
        break
    fi
done < <("$KCADM" get "clients/$identity_client_id/protocol-mappers/models" \
    -r "$REALM" \
    --fields id,name \
    --format csv \
    --noquotes)

if [ -n "$user_id_mapper_id" ]; then
    "$KCADM" delete "clients/$identity_client_id/protocol-mappers/models/$user_id_mapper_id" \
        -r "$REALM"
fi

"$KCADM" create "clients/$identity_client_id/protocol-mappers/models" \
    -r "$REALM" \
    -f /opt/smartpayut/user-id-mapper.json

if ! "$KCADM" get roles/SERVICE -r "$REALM" >/dev/null 2>&1; then
    "$KCADM" create roles -r "$REALM" -s name=SERVICE
fi

payment_client_id="$("$KCADM" get clients \
    -r "$REALM" \
    -q clientId=smartpayut-payment \
    --fields id \
    --format csv \
    --noquotes)"

if [ -z "$payment_client_id" ]; then
    "$KCADM" create clients \
        -r "$REALM" \
        -s clientId=smartpayut-payment \
        -s enabled=true \
        -s publicClient=false \
        -s serviceAccountsEnabled=true \
        -s standardFlowEnabled=false \
        -s directAccessGrantsEnabled=false \
        -s "secret=$PAYMENT_KEYCLOAK_CLIENT_SECRET"

    payment_client_id="$("$KCADM" get clients \
        -r "$REALM" \
        -q clientId=smartpayut-payment \
        --fields id \
        --format csv \
        --noquotes)"
else
    "$KCADM" update "clients/$payment_client_id" \
        -r "$REALM" \
        -s enabled=true \
        -s publicClient=false \
        -s serviceAccountsEnabled=true \
        -s "secret=$PAYMENT_KEYCLOAK_CLIENT_SECRET"
fi

payment_service_account_id="$("$KCADM" get "clients/$payment_client_id/service-account-user" \
    -r "$REALM" \
    --fields id \
    --format csv \
    --noquotes)"

"$KCADM" add-roles \
    -r "$REALM" \
    --uid "$payment_service_account_id" \
    --rolename SERVICE
