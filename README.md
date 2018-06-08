### smi-service-dell-chassis-inventory

123Retrieves inventory data from a Dell blade chassis

Copyright © 2017 Dell Inc. or its subsidiaries.  All Rights Reserved. 

### Purpose

Connects to a Dell blade chassis and retrieves inventory information.

---

### How to Use

A docker container for this service is available at: https://hub.docker.com/r/rackhd/dell-chassis-inventory/

#### Startup
~~~
sudo docker run -p 0.0.0.0:46001:46001 --name dell-chassis-inventory -d rackhd/dell-chassis-inventory:latest
~~~
.
#### API Definitions

The service can be called without a callback URL in order to run synchronously.  If a callback URL is provided in the payload, the service will return immediately and post to the callback URL when the results are available.

A swagger UI is provided by the microservice at http://<<ip>>:46001/swagger-ui.html

#### Example Post (Without Callback)

http://<<ip>>:46001/api/1.0/chassis/inventory/details
~~~
{
  "address": "100.68.124.33",
  "identifier": null,
  "password": "calvin",
  "userName": "root"
}
~~~
---

#### Licensing
Licensed under the Apache License, Version 2.0 (the “License”); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

Source code for this microservice is available in repositories at https://github.com/RackHD.  

The microservice makes use of dependent Jar libraries that may be covered by other licenses. In order to comply with the requirements of applicable licenses, the source for dependent libraries used by this microservice is available for download at:   https://bintray.com/rackhd/binary/download_file?file_path=smi-service-dell-chassis-inventory-dependency-sources-devel.zip
Additionally the binary and source jars for all dependent libraries are available for download on Maven Central.

RackHD is a Trademark of Dell EMC


### Support
Slack Channel: codecommunity.slack.com
