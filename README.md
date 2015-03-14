# sdjson-api
Java client API for Schedules Direct JSON data feed

## Back to Github
Starting with the API 20141201 release, the project has moved back to github.  The old SourceForge site will not be updated any further.

## Development in `release/20141201.0` branch
The `master` branch currently represents the last release of the last API version.  During this transition back to github, development will be contained in this new branch instead.  Eventually I will merge this branch into `master` and then "next release" development will continue in master as usual.

## New releases to JCenter
Starting with the API 20141201 release, all official releases will be posted to the JCenter maven repository and **not** synced to Maven Central.  Older releases remain at Maven Central.  If your project is using Maven Central, simply start pointing to JCenter instead.  More details will be provided as I get closer to an official release for this API.

## Snapshots to OJO
Starting with this release, snapshots will be delivered to the OJO Artifactory repository.  As a matter of fact, snapshot builds of `0.20141201.0-SNAPSHOT` are currently available.  Point to http://oss.jfrog.org/artifactory/oss-snapshot-local/ in order to access snapshot builds.

## Older releases
Older releases prior to `20141201` will remain at Maven Central under the `org.schedulesdirect` group id.  Future releases will **not** be synced to Maven Central, they will only be made available in the JCenter repository.
