### smi-service-dell-chassis-inventory

Retrieves inventory data from a Dell blade chassis

### Purpose

Connects to a Dell blade chassis and retrieves inventory information.

---

### How to Use

Under Construction. Docker container not yet published on DockerHub..... 

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

### Support
Slack Channel: codecommunity.slack.com