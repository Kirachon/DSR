#!/bin/bash

# DSR Database Initialization Script
# Philippine Dynamic Social Registry (DSR) System
# Version: 3.0.0

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-dsr_local}
DB_USER=${DB_USER:-dsr_user}
DB_PASSWORD=${DB_PASSWORD:-dsr_local_password}
POSTGRES_USER=${POSTGRES_USER:-postgres}

echo -e "${BLUE}=== DSR Database Initialization ===${NC}"
echo "Host: $DB_HOST:$DB_PORT"
echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo ""

# Function to check if PostgreSQL is running
check_postgres() {
    echo -e "${YELLOW}Checking PostgreSQL connection...${NC}"
    if pg_isready -h $DB_HOST -p $DB_PORT -U $POSTGRES_USER > /dev/null 2>&1; then
        echo -e "${GREEN}✓ PostgreSQL is running${NC}"
        return 0
    else
        echo -e "${RED}✗ PostgreSQL is not running or not accessible${NC}"
        echo "Please ensure PostgreSQL is running on $DB_HOST:$DB_PORT"
        return 1
    fi
}

# Function to create database and user
setup_database() {
    echo -e "${YELLOW}Setting up database and user...${NC}"
    
    # Check if database exists
    DB_EXISTS=$(psql -h $DB_HOST -p $DB_PORT -U $POSTGRES_USER -tAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" 2>/dev/null || echo "")
    
    if [ "$DB_EXISTS" = "1" ]; then
        echo -e "${YELLOW}Database $DB_NAME already exists${NC}"
    else
        echo -e "${YELLOW}Creating database $DB_NAME...${NC}"
        psql -h $DB_HOST -p $DB_PORT -U $POSTGRES_USER -c "CREATE DATABASE $DB_NAME;" || {
            echo -e "${RED}Failed to create database${NC}"
            exit 1
        }
        echo -e "${GREEN}✓ Database created${NC}"
    fi
    
    # Check if user exists
    USER_EXISTS=$(psql -h $DB_HOST -p $DB_PORT -U $POSTGRES_USER -tAc "SELECT 1 FROM pg_roles WHERE rolname='$DB_USER'" 2>/dev/null || echo "")
    
    if [ "$USER_EXISTS" = "1" ]; then
        echo -e "${YELLOW}User $DB_USER already exists${NC}"
    else
        echo -e "${YELLOW}Creating user $DB_USER...${NC}"
        psql -h $DB_HOST -p $DB_PORT -U $POSTGRES_USER -c "CREATE USER $DB_USER WITH LOGIN PASSWORD '$DB_PASSWORD';" || {
            echo -e "${RED}Failed to create user${NC}"
            exit 1
        }
        echo -e "${GREEN}✓ User created${NC}"
    fi
    
    # Grant privileges
    echo -e "${YELLOW}Granting privileges...${NC}"
    psql -h $DB_HOST -p $DB_PORT -U $POSTGRES_USER -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;" || {
        echo -e "${RED}Failed to grant privileges${NC}"
        exit 1
    }
    
    # Grant schema creation privileges
    psql -h $DB_HOST -p $DB_PORT -U $POSTGRES_USER -d $DB_NAME -c "GRANT CREATE ON DATABASE $DB_NAME TO $DB_USER;" || {
        echo -e "${RED}Failed to grant schema creation privileges${NC}"
        exit 1
    }
    
    echo -e "${GREEN}✓ Database setup complete${NC}"
}

# Function to run Flyway migrations
run_migrations() {
    echo -e "${YELLOW}Running database migrations...${NC}"
    
    # Check if migrations directory exists
    if [ ! -d "migrations" ]; then
        echo -e "${RED}Migrations directory not found${NC}"
        echo "Please run this script from the database directory"
        exit 1
    fi
    
    # Create flyway.conf if it doesn't exist
    cat > flyway.conf << EOF
flyway.url=jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME
flyway.user=$DB_USER
flyway.password=$DB_PASSWORD
flyway.locations=filesystem:migrations
flyway.schemas=dsr_core,dsr_auth,dsr_analytics,dsr_audit
flyway.baselineOnMigrate=true
flyway.validateOnMigrate=true
flyway.cleanDisabled=false
EOF
    
    # Check if Flyway is available
    if command -v flyway &> /dev/null; then
        echo -e "${YELLOW}Using system Flyway...${NC}"
        flyway migrate
    else
        echo -e "${YELLOW}Flyway not found in system, checking for Maven...${NC}"
        if command -v mvn &> /dev/null; then
            echo -e "${YELLOW}Running migrations via Maven Flyway plugin...${NC}"
            cd ..
            mvn flyway:migrate -Dflyway.configFiles=database/flyway.conf
            cd database
        else
            echo -e "${RED}Neither Flyway nor Maven found${NC}"
            echo "Please install Flyway or Maven to run migrations"
            echo "Alternatively, you can run the SQL files manually:"
            echo "  psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f migrations/V1__Initial_Schema.sql"
            echo "  psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f migrations/V2__Auth_Tables.sql"
            echo "  psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f migrations/V3__Core_Tables.sql"
            return 1
        fi
    fi
    
    echo -e "${GREEN}✓ Migrations completed${NC}"
}

# Function to verify database setup
verify_setup() {
    echo -e "${YELLOW}Verifying database setup...${NC}"
    
    # Test connection
    psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT version();" > /dev/null 2>&1 || {
        echo -e "${RED}✗ Cannot connect to database${NC}"
        return 1
    }
    
    # Check schemas
    SCHEMAS=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -tAc "SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'dsr_%' ORDER BY schema_name;")
    echo -e "${GREEN}✓ Database connection successful${NC}"
    echo -e "${GREEN}✓ Schemas created:${NC}"
    echo "$SCHEMAS" | while read schema; do
        echo -e "  ${GREEN}• $schema${NC}"
    done
    
    # Check tables
    TABLES=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -tAc "SELECT schemaname||'.'||tablename FROM pg_tables WHERE schemaname LIKE 'dsr_%' ORDER BY schemaname, tablename;")
    TABLE_COUNT=$(echo "$TABLES" | wc -l)
    echo -e "${GREEN}✓ Tables created: $TABLE_COUNT${NC}"
    
    echo -e "${GREEN}✓ Database verification complete${NC}"
}

# Main execution
main() {
    echo -e "${BLUE}Starting DSR database initialization...${NC}"
    
    # Check prerequisites
    if ! command -v psql &> /dev/null; then
        echo -e "${RED}psql command not found${NC}"
        echo "Please install PostgreSQL client tools"
        exit 1
    fi
    
    # Run setup steps
    check_postgres || exit 1
    setup_database || exit 1
    run_migrations || {
        echo -e "${YELLOW}Migration failed, but database and user are set up${NC}"
        echo -e "${YELLOW}You can run migrations manually later${NC}"
    }
    verify_setup || exit 1
    
    echo ""
    echo -e "${GREEN}=== DSR Database Initialization Complete ===${NC}"
    echo -e "${GREEN}Database URL: jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME${NC}"
    echo -e "${GREEN}Username: $DB_USER${NC}"
    echo -e "${GREEN}Password: $DB_PASSWORD${NC}"
    echo ""
    echo -e "${BLUE}Next steps:${NC}"
    echo "1. Update your application.yml files with the database configuration"
    echo "2. Remove the 'no-db' profile from your service startup commands"
    echo "3. Start your DSR services"
    echo ""
}

# Run main function
main "$@"
