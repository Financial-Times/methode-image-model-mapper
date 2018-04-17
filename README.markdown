[![CircleCI](https://circleci.com/gh/Financial-Times/methode-image-model-mapper.svg?style=svg)](https://circleci.com/gh/Financial-Times/methode-image-model-mapper) [![Coverage Status](https://coveralls.io/repos/github/Financial-Times/methode-image-model-mapper/badge.svg)](https://coveralls.io/github/Financial-Times/methode-image-model-mapper)

# Methode Image Model Mapper
This is a web application which listens to the NativeCmsPublicationEvents Kafka topic for publishing events coming from Methode and process only the messages
containing an image. It extracts the image metadata, creates a message with this information and writes it to the CmsPublicationEvents topic.

## Introduction

The service listens to the NativeCmsPublicationEvents Kafka topic and ingests the image messages coming from Methode.
The image messages coming from Methode have the header: `Origin-System-Id: http://cmdb.ft.com/systems/methode-web-pub` and the JSON payload has the 
field `"type":"Image"`. Other messages are discarded.

## Mapping

Maps Graphics types as well.

External images, from custom whitelisted URLs can be mapped if the `ExternalUrl` property is set. This means that the binary from the Methode payload will not apply, it is ignored and the linked image is from the third party.

### Methode XPATH to image model mapping

<table border="1">
    <tr>
        <th>UPP field</td>
        <th>Methode Field - xpath</td>
    </tr>
    <tr>
        <td>title</td>
        <td>/meta/picture/web_information/caption</td>
    </tr>
    <tr>
        <td>description</td>
        <td>/meta/picture/web_information/alt_tag</td>
    </tr>
    <tr>
        <td>pixelWidth</td>
        <td>/props/imageInfo/width</td>
    </tr>
    <tr>
        <td>pixelHeight</td>
        <td>/props/imageInfo/height</td>
    </tr>
    <tr>
        <td>mediaType</td>
        <td>/props/imageInfo/fileType</td>
    </tr>
</table>

## Running locally
To compile, run tests and build jar
    
    mvn clean verify 

To run locally, run:
    
    java -jar target/methode-image-model-mapper.jar server methode-image-model-mapper.yaml

## Healthchecks 
http://localhost:16080/__health

## Admin Endoint
http://localhost:16081