#!bin/bash

# realm and client names
REALM_NAME=mealplanner
CLIENT_NAME=api-user

# array of users to create
USERS=(
  "mpadmin"
  "mpuser1"
  "mpuser2"
)
# functions that check the existence of resources in keycloak
REALM_EXISTS() {
  /opt/keycloak/bin/kcadm.sh get realms/$REALM_NAME | grep -q "$REALM_NAME"
}

CLIENT_EXISTS() {
  /opt/keycloak/bin/kcadm.sh get clients -r mealplanner -q clientId=$CLIENT_NAME | grep -q '"id"'
}

USER_EXISTS() {
  /opt/keycloak/bin/kcadm.sh get users -r $REALM_NAME -q username=$1 | grep -q '"id"'
}

# wait until keycloak is up and running
while ! echo > /dev/tcp/localhost/8080; do
  sleep 5
done

# login des kc admin
/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin

# create realm if non existent
if REALM_EXISTS; then
  echo "Realm '$REALM_NAME' already exists. Skipping creation."
else
  echo "Realm '$REALM_NAME' does not exist. Creating realm."
  /opt/keycloak/bin/kcadm.sh create realms -s realm=$REALM_NAME -s enabled=true
fi

# create client if non existent
if CLIENT_EXISTS; then
  echo "Client '$CLIENT_NAME' already exists. Skipping creation"
else
  echo "Client '$CLIENT_NAME' does not exist. Creating client."
    /opt/keycloak/bin/kcadm.sh create clients -r $REALM_NAME -s clientId=$CLIENT_NAME -s enabled=true -s directAccessGrantsEnabled=true -s publicClient=true
fi

# create users if non existent
for USERNAME in "${USERS[@]}"; do
  if USER_EXISTS $USERNAME; then
    echo "User '$USERNAME' already exists. Skipping creation."
  else
    echo "User '$USERNAME' does not exist. Creating user."
    /opt/keycloak/bin/kcadm.sh create users -r $REALM_NAME -s username=$USERNAME -s enabled=true -s email="$USERNAME@example.com" -s emailVerified=true -s credentials='[{"type":"password","value":"'$USERNAME'","temporary":false}]' -s firstName="$USERNAME" -s lastName="$USERNAME"
  fi
done