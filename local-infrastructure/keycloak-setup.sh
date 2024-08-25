#!bin/bash

REALM_NAME=mealplanner

# warten, bis der keycloak hochgefahren ist
while ! echo > /dev/tcp/localhost/8080; do
  sleep 5
done

# login des kc admin
/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin

REALM_EXISTS=$( /opt/keycloak/bin/kcadm.sh get realms/$REALM_NAME | grep -q "$REALM_NAME" && echo "true" || echo "false" )

USER_EXISTS() {
  /opt/keycloak/bin/kcadm.sh get users -r $REALM_NAME -q username=$1 | grep -q '"id"'
}

# array von benutzern, die erstellt werden sollen
USERS=(
  "mpadmin"
  "mpuser1"
  "mpuser2"
)

# realm erstellen, wenn er noch nicht existiert
if [ "$REALM_EXISTS" = "true" ]; then
  echo "Realm '$REALM_NAME' exists."
else
  /opt/keycloak/bin/kcadm.sh create realms -s realm=$REALM_NAME -s enabled=true
fi

# user erstellen, wenn sie noch nicht existieren
for USERNAME in "${USERS[@]}"; do
  if USER_EXISTS $USERNAME; then
    echo "User '$USERNAME' already exists"
  else
    /opt/keycloak/bin/kcadm.sh create users -r $REALM_NAME -s username=$USERNAME -s enabled=true -s email="$USERNAME@example.com" -s emailVerified=true -s credentials='[{"type":"password","value":"'$USERNAME'","temporary":false}]'
  fi
done

