#!/bin/bash
set -e

# Function to create user and database
create_user_and_database() {
    local database=$1
    local user=$2
    local password=$3

    echo "Creating database '$database' with user '$user'"

    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
        CREATE USER $user WITH PASSWORD '$password';
        CREATE DATABASE $database;
        GRANT ALL PRIVILEGES ON DATABASE $database TO $user;
        ALTER DATABASE $database OWNER TO $user;
EOSQL
}

# Create multiple databases with their own users
if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
    echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"

    # Split by comma
    IFS=',' read -ra DBS <<< "$POSTGRES_MULTIPLE_DATABASES"

    for db in "${DBS[@]}"; do
        # Trim whitespace
        db=$(echo "$db" | xargs)

        # Generate username and password based on db name
        user="${db}_user"
        password="${db}_pass_123"

        create_user_and_database "$db" "$user" "$password"

        # Also grant to admin user if exists
        if [ -n "$POSTGRES_USER" ]; then
            psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
                GRANT ALL PRIVILEGES ON DATABASE $db TO $POSTGRES_USER;
EOSQL
        fi
    done

    echo "Multiple databases created successfully"
fi