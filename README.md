# Drawbridge Carpool Planner

## Setup and Run Configurations

To build the Java Maven project, run
```
mvn package
```
in the root directory. Then run 
```
./run –gui --port <PORT>
```
to start the application.

To set up a local database on postgre, download 
PostgreSQL11 from https://www.postgresql.org/download/.
Install PGAdmin4 from https://www.pgadmin.org/download/
as well. Open PGAdmin and create a new database named
"carpools".

Right click on the carpools database and select "restore".
Use the carpoolsDump.tar file to build the database.

Maven will automatically install the JDBC driver needed 
to connect to the postgre database when "mvn package" is 
run, though it can be downloaded at 
https://jdbc.postgresql.org/download.html
and manually imported as an external jar file.
