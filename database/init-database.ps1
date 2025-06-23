# DSR Database Initialization Script (PowerShell)
# Philippine Dynamic Social Registry (DSR) System
# Version: 3.0.0

param(
    [string]$DbHost = "localhost",
    [string]$DbPort = "5432",
    [string]$DbName = "dsr_local",
    [string]$DbUser = "dsr_user",
    [string]$DbPassword = "dsr_local_password",
    [string]$PostgresUser = "postgres"
)

Write-Host "=== DSR Database Initialization ===" -ForegroundColor Blue
Write-Host "Host: $DbHost`:$DbPort"
Write-Host "Database: $DbName"
Write-Host "User: $DbUser"
Write-Host ""

# Function to check if PostgreSQL is running
function Test-PostgreSQLConnection {
    Write-Host "Checking PostgreSQL connection..." -ForegroundColor $Yellow
    try {
        $env:PGPASSWORD = ""
        $result = & pg_isready -h $DbHost -p $DbPort -U $PostgresUser 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ PostgreSQL is running" -ForegroundColor $Green
            return $true
        } else {
            Write-Host "✗ PostgreSQL is not running or not accessible" -ForegroundColor $Red
            Write-Host "Please ensure PostgreSQL is running on $DbHost`:$DbPort"
            return $false
        }
    } catch {
        Write-Host "✗ Error checking PostgreSQL connection: $_" -ForegroundColor $Red
        return $false
    }
}

# Function to create database and user
function Initialize-Database {
    Write-Host "Setting up database and user..." -ForegroundColor $Yellow
    
    try {
        # Set password for postgres user (you'll be prompted)
        Write-Host "Note: You may be prompted for the PostgreSQL superuser password" -ForegroundColor $Yellow
        
        # Check if database exists
        $dbExists = & psql -h $DbHost -p $DbPort -U $PostgresUser -tAc "SELECT 1 FROM pg_database WHERE datname='$DbName'" 2>$null
        
        if ($dbExists -eq "1") {
            Write-Host "Database $DbName already exists" -ForegroundColor $Yellow
        } else {
            Write-Host "Creating database $DbName..." -ForegroundColor $Yellow
            & psql -h $DbHost -p $DbPort -U $PostgresUser -c "CREATE DATABASE $DbName;"
            if ($LASTEXITCODE -ne 0) {
                Write-Host "Failed to create database" -ForegroundColor $Red
                return $false
            }
            Write-Host "✓ Database created" -ForegroundColor $Green
        }
        
        # Check if user exists
        $userExists = & psql -h $DbHost -p $DbPort -U $PostgresUser -tAc "SELECT 1 FROM pg_roles WHERE rolname='$DbUser'" 2>$null
        
        if ($userExists -eq "1") {
            Write-Host "User $DbUser already exists" -ForegroundColor $Yellow
        } else {
            Write-Host "Creating user $DbUser..." -ForegroundColor $Yellow
            & psql -h $DbHost -p $DbPort -U $PostgresUser -c "CREATE USER $DbUser WITH LOGIN PASSWORD '$DbPassword';"
            if ($LASTEXITCODE -ne 0) {
                Write-Host "Failed to create user" -ForegroundColor $Red
                return $false
            }
            Write-Host "✓ User created" -ForegroundColor $Green
        }
        
        # Grant privileges
        Write-Host "Granting privileges..." -ForegroundColor $Yellow
        & psql -h $DbHost -p $DbPort -U $PostgresUser -c "GRANT ALL PRIVILEGES ON DATABASE $DbName TO $DbUser;"
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Failed to grant privileges" -ForegroundColor $Red
            return $false
        }
        
        # Grant schema creation privileges
        & psql -h $DbHost -p $DbPort -U $PostgresUser -d $DbName -c "GRANT CREATE ON DATABASE $DbName TO $DbUser;"
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Failed to grant schema creation privileges" -ForegroundColor $Red
            return $false
        }
        
        Write-Host "✓ Database setup complete" -ForegroundColor $Green
        return $true
    } catch {
        Write-Host "Error setting up database: $_" -ForegroundColor $Red
        return $false
    }
}

# Function to run migrations manually
function Invoke-Migrations {
    Write-Host "Running database migrations..." -ForegroundColor $Yellow
    
    # Check if migrations directory exists
    if (-not (Test-Path "migrations")) {
        Write-Host "Migrations directory not found" -ForegroundColor $Red
        Write-Host "Please run this script from the database directory"
        return $false
    }
    
    try {
        # Set password environment variable
        $env:PGPASSWORD = $DbPassword
        
        # Run migrations in order
        $migrations = @(
            "V1__Initial_Schema.sql",
            "V2__Auth_Tables.sql", 
            "V3__Core_Tables.sql"
        )
        
        foreach ($migration in $migrations) {
            $migrationPath = "migrations\$migration"
            if (Test-Path $migrationPath) {
                Write-Host "Running migration: $migration" -ForegroundColor $Yellow
                & psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -f $migrationPath
                if ($LASTEXITCODE -ne 0) {
                    Write-Host "Failed to run migration: $migration" -ForegroundColor $Red
                    return $false
                }
                Write-Host "✓ Migration completed: $migration" -ForegroundColor $Green
            } else {
                Write-Host "Migration file not found: $migrationPath" -ForegroundColor $Red
                return $false
            }
        }
        
        Write-Host "✓ All migrations completed" -ForegroundColor $Green
        return $true
    } catch {
        Write-Host "Error running migrations: $_" -ForegroundColor $Red
        return $false
    } finally {
        # Clear password environment variable
        $env:PGPASSWORD = $null
    }
}

# Function to verify database setup
function Test-DatabaseSetup {
    Write-Host "Verifying database setup..." -ForegroundColor $Yellow
    
    try {
        # Set password environment variable
        $env:PGPASSWORD = $DbPassword
        
        # Test connection
        & psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -c "SELECT version();" > $null 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "✗ Cannot connect to database" -ForegroundColor $Red
            return $false
        }
        
        # Check schemas
        $schemas = & psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -tAc "SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'dsr_%' ORDER BY schema_name;"
        Write-Host "✓ Database connection successful" -ForegroundColor $Green
        Write-Host "✓ Schemas created:" -ForegroundColor $Green
        foreach ($schema in $schemas) {
            Write-Host "  • $schema" -ForegroundColor $Green
        }
        
        # Check tables
        $tables = & psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -tAc "SELECT schemaname||'.'||tablename FROM pg_tables WHERE schemaname LIKE 'dsr_%' ORDER BY schemaname, tablename;"
        $tableCount = $tables.Count
        Write-Host "✓ Tables created: $tableCount" -ForegroundColor $Green
        
        Write-Host "✓ Database verification complete" -ForegroundColor $Green
        return $true
    } catch {
        Write-Host "Error verifying database: $_" -ForegroundColor $Red
        return $false
    } finally {
        # Clear password environment variable
        $env:PGPASSWORD = $null
    }
}

# Main execution
function Main {
    Write-Host "Starting DSR database initialization..." -ForegroundColor $Blue
    
    # Check prerequisites
    try {
        & psql --version > $null 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "psql command not found" -ForegroundColor $Red
            Write-Host "Please install PostgreSQL client tools"
            return
        }
    } catch {
        Write-Host "psql command not found" -ForegroundColor $Red
        Write-Host "Please install PostgreSQL client tools"
        return
    }
    
    # Run setup steps
    if (-not (Test-PostgreSQLConnection)) { return }
    if (-not (Initialize-Database)) { return }
    if (-not (Invoke-Migrations)) {
        Write-Host "Migration failed, but database and user are set up" -ForegroundColor $Yellow
        Write-Host "You can run migrations manually later" -ForegroundColor $Yellow
    }
    if (-not (Test-DatabaseSetup)) { return }
    
    Write-Host ""
    Write-Host "=== DSR Database Initialization Complete ===" -ForegroundColor $Green
    Write-Host "Database URL: jdbc:postgresql://$DbHost`:$DbPort/$DbName" -ForegroundColor $Green
    Write-Host "Username: $DbUser" -ForegroundColor $Green
    Write-Host "Password: $DbPassword" -ForegroundColor $Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor $Blue
    Write-Host "1. Update your application.yml files with the database configuration"
    Write-Host "2. Remove the 'no-db' profile from your service startup commands"
    Write-Host "3. Start your DSR services"
    Write-Host ""
}

# Run main function
Main
