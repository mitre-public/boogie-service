# Boogie Service

## Overview

This repo provides a lightweight REST wrapper around the [Boogie](https://github.com/mitre-public/boogie) library which can be used
for route expansion and is backed by ARINC-424 data.

### Generated Clients

The latest client version is the same as the latest version of the [api.yaml](src/main/resources/api.yaml).

### Local Testing

Use devenv to get new session credentials from cre.mitre.org, specifying the `guardian` profile (to match `application-local.yaml` properties):
```bash
$ devenv update -e aws
* UpdateLocalAwsConfiguration
| + FindCmd(aws) ✔︎
? Please specify an aws profile (leave empty for the 'default' profile) guardian
...
```

Startup a local server with bootRun, referencing the `application-local.yaml` properties for a local run with data from S3:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```
Startup a local server with bootRun, referencing the `application-local-file.yaml` properties for a local run with data from your computer:
```bash
./gradlew bootRun --args='--spring.profiles.active=local-file'
```

Connect to the local server at http://localhost:8080/

---

<p align=center><ins><b>NOTICE</b></ins></p>

<p>This work was produced for the U.S. Government under Contract 693KA8-22-C-00001 and is subject to Federal Aviation Administration Acquisition Management System Clause 3.5-13, Rights In Data-General (Oct. 2014), Alt. III and Alt. IV (Oct. 2009).</p>

<p>The contents of this document reflect the views of the author and The MITRE Corporation and do not necessarily reflect the views of the Federal Aviation Administration (FAA) or the Department of Transportation (DOT). Neither the FAA nor the DOT makes any warranty or guarantee, expressed or implied, concerning the content or accuracy of these views.</p>

<p>For further information, please contact The MITRE Corporation, Contracts Management Office, 7515 Colshire Drive, McLean, VA 22102-7539, (703) 983-6000.</p>

<p align=center><ins><b>&copy; 2024 The MITRE Corporation. All Rights Reserved.</b></ins></p>

---

<p align=center>Approved for Public Release; Distribution Unlimited. Public Release Case Number 24-3505</p>

---
