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

# Check if psql is available
try {
    $null = & psql --version 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "psql command not found. Please install PostgreSQL client tools." -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "psql command not found. Please install PostgreSQL client tools." -ForegroundColor Red
    exit 1
}

Write-Host "Checking PostgreSQL connection..." -ForegroundColor Yellow
try {
    $null = & pg_isready -h $DbHost -p $DbPort -U $PostgresUser 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ PostgreSQL is running" -ForegroundColor Green
    } else {
        Write-Host "✗ PostgreSQL is not running or not accessible" -ForegroundColor Red
        Write-Host "Please ensure PostgreSQL is running on $DbHost`:$DbPort"
        exit 1
    }
} catch {
    Write-Host "✗ Error checking PostgreSQL connection" -ForegroundColor Red
    exit 1
}

Write-Host "Setting up database and user..." -ForegroundColor Yellow
Write-Host "Note: You may be prompted for the PostgreSQL superuser password" -ForegroundColor Yellow

# Check if database exists
$dbExists = & psql -h $DbHost -p $DbPort -U $PostgresUser -tAc "SELECT 1 FROM pg_database WHERE datname='$DbName'" 2>$null

if ($dbExists -eq "1") {
    Write-Host "Database $DbName already exists" -ForegroundColor Yellow
} else {
    Write-Host "Creating database $DbName..." -ForegroundColor Yellow
    & psql -h $DbHost -p $DbPort -U $PostgresUser -c "CREATE DATABASE $DbName;"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to create database" -ForegroundColor Red
        exit 1
    }
    Write-Host "✓ Database created" -ForegroundColor Green
}

# Check if user exists
$userExists = & psql -h $DbHost -p $DbPort -U $PostgresUser -tAc "SELECT 1 FROM pg_roles WHERE rolname='$DbUser'" 2>$null

if ($userExists -eq "1") {
    Write-Host "User $DbUser already exists" -ForegroundColor Yellow
} else {
    Write-Host "Creating user $DbUser..." -ForegroundColor Yellow
    & psql -h $DbHost -p $DbPort -U $PostgresUser -c "CREATE USER $DbUser WITH LOGIN PASSWORD '$DbPassword';"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to create user" -ForegroundColor Red
        exit 1
    }
    Write-Host "✓ User created" -ForegroundColor Green
}

# Grant privileges
Write-Host "Granting privileges..." -ForegroundColor Yellow
& psql -h $DbHost -p $DbPort -U $PostgresUser -c "GRANT ALL PRIVILEGES ON DATABASE $DbName TO $DbUser;"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to grant privileges" -ForegroundColor Red
    exit 1
}

# Grant schema creation privileges
& psql -h $DbHost -p $DbPort -U $PostgresUser -d $DbName -c "GRANT CREATE ON DATABASE $DbName TO $DbUser;"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to grant schema creation privileges" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Database setup complete" -ForegroundColor Green

# Run migrations
Write-Host "Running database migrations..." -ForegroundColor Yellow

# Check if migrations directory exists
if (-not (Test-Path "migrations")) {
    Write-Host "Migrations directory not found" -ForegroundColor Red
    Write-Host "Please run this script from the database directory"
    exit 1
}

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
        Write-Host "Running migration: $migration" -ForegroundColor Yellow
        & psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -f $migrationPath
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Failed to run migration: $migration" -ForegroundColor Red
            $env:PGPASSWORD = $null
            exit 1
        }
        Write-Host "✓ Migration completed: $migration" -ForegroundColor Green
    } else {
        Write-Host "Migration file not found: $migrationPath" -ForegroundColor Red
        $env:PGPASSWORD = $null
        exit 1
    }
}

Write-Host "✓ All migrations completed" -ForegroundColor Green

# Verify setup
Write-Host "Verifying database setup..." -ForegroundColor Yellow

# Test connection
& psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -c "SELECT version();" > $null 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Cannot connect to database" -ForegroundColor Red
    $env:PGPASSWORD = $null
    exit 1
}

# Check schemas
$schemas = & psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -tAc "SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'dsr_%' ORDER BY schema_name;"
Write-Host "✓ Database connection successful" -ForegroundColor Green
Write-Host "✓ Schemas created:" -ForegroundColor Green
foreach ($schema in $schemas) {
    Write-Host "  • $schema" -ForegroundColor Green
}

# Check tables
$tables = & psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -tAc "SELECT schemaname||'.'||tablename FROM pg_tables WHERE schemaname LIKE 'dsr_%' ORDER BY schemaname, tablename;"
$tableCount = $tables.Count
Write-Host "✓ Tables created: $tableCount" -ForegroundColor Green

Write-Host "✓ Database verification complete" -ForegroundColor Green

# Clear password environment variable
$env:PGPASSWORD = $null

Write-Host ""
Write-Host "=== DSR Database Initialization Complete ===" -ForegroundColor Green
Write-Host "Database URL: jdbc:postgresql://$DbHost`:$DbPort/$DbName" -ForegroundColor Green
Write-Host "Username: $DbUser" -ForegroundColor Green
Write-Host "Password: $DbPassword" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Blue
Write-Host "1. Update your application.yml files with the database configuration"
Write-Host "2. Remove the 'no-db' profile from your service startup commands"
Write-Host "3. Start your DSR services"
Write-Host ""
