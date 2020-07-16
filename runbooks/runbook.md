# UPP - Methode Image Model Mapper

This service reads images from a Kafka queue identifying methode images and transforms them into UP format (image model)
and writes to the CMSPublicationEvents queue.

## Code

up-mimm

## Primary URL

<https://upp-prod-delivery-glb.upp.ft.com/__methode-image-model-mapper/>

## Service Tier

Platinum

## Lifecycle Stage

Production

## Delivered By

content

## Supported By

content

## Known About By

- dimitar.terziev
- hristo.georgiev
- elitsa.pavlova
- elina.kaneva
- kalin.arsov
- ivan.nikolov
- miroslav.gatsanoga
- mihail.mihaylov
- tsvetan.dimitrov
- georgi.ivanov
- robert.marinov

## Host Platform

AWS

## Architecture

This is a web application which listens to the NativeCmsPublicationEvents Kafka topic for publishing events coming from
Methode and process only the messages containing an image. It extracts the image metadata, creates a message with this
information and writes it to the CmsPublicationEvents topic.

## Contains Personal Data

No

## Contains Sensitive Data

No

## Dependencies

- kafka-proxy

## Failover Architecture Type

ActiveActive

## Failover Process Type

FullyAutomated

## Failback Process Type

FullyAutomated

## Failover Details

The service is deployed in both Delivery clusters.
The failover guide for the cluster is located here:
<https://github.com/Financial-Times/upp-docs/tree/master/failover-guides/delivery-cluster>

## Data Recovery Process Type

NotApplicable

## Data Recovery Details

The service does not store data, so it does not require any data recovery steps.

## Release Process Type

PartiallyAutomated

## Rollback Process Type

Manual

## Release Details

Manual failover is needed when a new version of
the service is deployed to production.
Otherwise, an automated failover is going to take place when releasing.
For more details about the failover process please see: <https://github.com/Financial-Times/upp-docs/tree/master/failover-guides/delivery-cluster>

## Key Management Process Type

Manual

## Key Management Details

To access the service clients need to provide basic auth credentials.
To rotate credentials you need to login to a particular cluster and update varnish-auth secrets.

## Monitoring

Service in UPP K8S delivery clusters:

- Delivery-Prod-EU health: <https://upp-prod-delivery-eu.ft.com/__health/__pods-health?service-name=methode-image-model-mapper>
- Delivery-Prod-US health: <https://upp-prod-delivery-us.ft.com/__health/__pods-health?service-name=methode-image-model-mapper>

## First Line Troubleshooting

<https://github.com/Financial-Times/upp-docs/tree/master/guides/ops/first-line-troubleshooting>

## Second Line Troubleshooting

Please refer to the GitHub repository README for troubleshooting information.
