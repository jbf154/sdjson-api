# sdjson-api
Java client API for Schedules Direct JSON data feed

## 0.20141201.1
Fix 1 for the `20141201` API is now available.  This first fix release only addresses an issue where star ratings for movies were not being parsed properly due to a labelling change in the SD data.

## Back to Github
Starting with the API 20141201 release, the project has moved back to github.  The old SourceForge site will not be updated any further.

## New releases to JCenter
Starting with the API 20141201 release, all official releases will be posted to the JCenter maven repository and **not** synced to Maven Central.  Older releases remain at Maven Central.  If your project is using Maven Central, simply start pointing to JCenter instead.

## Snapshots to OJO
Starting with the `20141201` release, snapshots will be delivered to the OJO Artifactory repository.  Point to http://oss.jfrog.org/artifactory/oss-snapshot-local/ in order to access snapshot builds.

## Older releases
Older releases prior to `20141201` will remain at Maven Central under the `org.schedulesdirect` group id.  Future releases will **not** be synced to Maven Central, they will only be made available in the JCenter repository.
