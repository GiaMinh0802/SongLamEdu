#!/bin/bash
# Automated backup script

BACKUP_DIR="/home/songlam/backups"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="songlamedu"

mkdir -p $BACKUP_DIR

echo "Starting backup at $(date)"

# Backup database
docker exec songlam-postgres pg_dump -U postgres $DB_NAME | gzip > $BACKUP_DIR/db_backup_$DATE.sql. gz

# Keep only last 7 days of backups
find $BACKUP_DIR -name "db_backup_*. sql.gz" -mtime +7 -delete

echo "Backup completed:  db_backup_$DATE.sql.gz"