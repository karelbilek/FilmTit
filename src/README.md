# FilmTit How-To

## Configuration

For the configuration, please see the configuration.xml file.


## Initial import

To run an initial import of the data run

    $ make import
    $ make reindex

in this directory. The import part loads all subtitle mappings from the files
from /filmtit/data/ (to change this directory, see the Makefile) and queries
the IMDB API for more information. The reindex part initializes the indexes
that are required by the TM. This part is separate so that the pairs don't have
to be re-imported after a change to the TM.


## Configuration

All configuration should be done in configuration.xml. This file also includes
the path of the initial data import.