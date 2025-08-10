@echo off
REM DSR Database Initialization Script (Windows Batch)
REM Philippine Dynamic Social Registry (DSR) System
REM Version: 3.0.0

setlocal enabledelayedexpansion

REM Configuration
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=dsr_local
set DB_USER=dsr_user
set DB_PASSWORD=dsr_local_password
set POSTGRES_USER=postgres

echo === DSR Database Initialization ===
echo Host: %DB_HOST%:%DB_PORT%
echo Database: %DB_NAME%
echo User: %DB_USER%
echo.

REM Check if psql is available
psql --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: psql command not found. Please install PostgreSQL client tools.
    pause
    exit /b 1
)

REM Check PostgreSQL connection
echo Checking PostgreSQL connection...
pg_isready -h %DB_HOST% -p %DB_PORT% -U %POSTGRES_USER% >nul 2>&1
if errorlevel 1 (
    echo ERROR: PostgreSQL is not running or not accessible
    echo Please ensure PostgreSQL is running on %DB_HOST%:%DB_PORT%
    pause
    exit /b 1
)
echo SUCCESS: PostgreSQL is running

echo.
echo Setting up database and user...
echo Note: You may be prompted for the PostgreSQL superuser password

REM Check if database exists
for /f %%i in ('psql -h %DB_HOST% -p %DB_PORT% -U %POSTGRES_USER% -tAc "SELECT 1 FROM pg_database WHERE datname='%DB_NAME%'" 2^>nul') do set DB_EXISTS=%%i

if "%DB_EXISTS%"=="1" (
    echo Database %DB_NAME% already exists
) else (
    echo Creating database %DB_NAME%...
    psql -h %DB_HOST% -p %DB_PORT% -U %POSTGRES_USER% -c "CREATE DATABASE %DB_NAME%;"
    if errorlevel 1 (
        echo ERROR: Failed to create database
        pause
        exit /b 1
    )
    echo SUCCESS: Database created
)

REM Check if user exists
for /f %%i in ('psql -h %DB_HOST% -p %DB_PORT% -U %POSTGRES_USER% -tAc "SELECT 1 FROM pg_roles WHERE rolname='%DB_USER%'" 2^>nul') do set USER_EXISTS=%%i

if "%USER_EXISTS%"=="1" (
    echo User %DB_USER% already exists
) else (
    echo Creating user %DB_USER%...
    psql -h %DB_HOST% -p %DB_PORT% -U %POSTGRES_USER% -c "CREATE USER %DB_USER% WITH LOGIN PASSWORD '%DB_PASSWORD%';"
    if errorlevel 1 (
        echo ERROR: Failed to create user
        pause
        exit /b 1
    )
    echo SUCCESS: User created
)

REM Grant privileges
echo Granting privileges...
psql -h %DB_HOST% -p %DB_PORT% -U %POSTGRES_USER% -c "GRANT ALL PRIVILEGES ON DATABASE %DB_NAME% TO %DB_USER%;"
if errorlevel 1 (
    echo ERROR: Failed to grant privileges
    pause
    exit /b 1
)

psql -h %DB_HOST% -p %DB_PORT% -U %POSTGRES_USER% -d %DB_NAME% -c "GRANT CREATE ON DATABASE %DB_NAME% TO %DB_USER%;"
if errorlevel 1 (
    echo ERROR: Failed to grant schema creation privileges
    pause
    exit /b 1
)

echo SUCCESS: Database setup complete

REM Run migrations
echo.
echo Running database migrations...

REM Check if migrations directory exists
if not exist "migrations" (
    echo ERROR: Migrations directory not found
    echo Please run this script from the database directory
    pause
    exit /b 1
)

REM Set password environment variable
set PGPASSWORD=%DB_PASSWORD%

REM Run migrations in order
echo Running migration: V1__Initial_Schema.sql
if exist "migrations\V1__Initial_Schema.sql" (
    psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "migrations\V1__Initial_Schema.sql"
    if errorlevel 1 (
        echo ERROR: Failed to run migration V1__Initial_Schema.sql
        set PGPASSWORD=
        pause
        exit /b 1
    )
    echo SUCCESS: Migration V1__Initial_Schema.sql completed
) else (
    echo ERROR: Migration file V1__Initial_Schema.sql not found
    set PGPASSWORD=
    pause
    exit /b 1
)

echo Running migration: V2__Auth_Tables.sql
if exist "migrations\V2__Auth_Tables.sql" (
    psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "migrations\V2__Auth_Tables.sql"
    if errorlevel 1 (
        echo ERROR: Failed to run migration V2__Auth_Tables.sql
        set PGPASSWORD=
        pause
        exit /b 1
    )
    echo SUCCESS: Migration V2__Auth_Tables.sql completed
) else (
    echo ERROR: Migration file V2__Auth_Tables.sql not found
    set PGPASSWORD=
    pause
    exit /b 1
)

echo Running migration: V3__Core_Tables.sql
if exist "migrations\V3__Core_Tables.sql" (
    psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "migrations\V3__Core_Tables.sql"
    if errorlevel 1 (
        echo ERROR: Failed to run migration V3__Core_Tables.sql
        set PGPASSWORD=
        pause
        exit /b 1
    )
    echo SUCCESS: Migration V3__Core_Tables.sql completed
) else (
    echo ERROR: Migration file V3__Core_Tables.sql not found
    set PGPASSWORD=
    pause
    exit /b 1
)

echo SUCCESS: All migrations completed

REM Verify setup
echo.
echo Verifying database setup...

REM Test connection
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT version();" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Cannot connect to database
    set PGPASSWORD=
    pause
    exit /b 1
)

echo SUCCESS: Database connection successful
echo SUCCESS: Database verification complete

REM Clear password environment variable
set PGPASSWORD=

echo.
echo === DSR Database Initialization Complete ===
echo Database URL: jdbc:postgresql://%DB_HOST%:%DB_PORT%/%DB_NAME%
echo Username: %DB_USER%
echo Password: %DB_PASSWORD%
echo.
echo Next steps:
echo 1. Update your application.yml files with the database configuration
echo 2. Remove the 'no-db' profile from your service startup commands
echo 3. Start your DSR services
echo.
pause
