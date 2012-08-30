#!/bin/bash

POSTGRES_DIR=$1
POSTGRES_USER=$2
DATABASE_NAME=$3

wget http://www.pgsql.cz/data/czech.tar.gz
tar -zxvf czech.tar.gz
cd fulltext_dicts
mv * $POSTGRES_DIR/share/tsearch_data
cd ..
rmdir fulltext_dicts

$POSTGRES_DIR/bin/psql $DATABASE_NAME -U $POSTGRES_USER << EOF
CREATE TEXT SEARCH DICTIONARY pg_catalog.cspell
   (template=ispell, dictfile = czech, afffile=czech, stopwords=czech);
CREATE TEXT SEARCH CONFIGURATION pg_catalog.czech (copy=english);
EOF
