## Overview
This repository contains a Spring Boot-based REST API designed to process event data and generate corresponding diagrams. The API converts event data from [cloud events](https://cloudevents.io) to [XES](https://www.tf-pm.org/resources/xes-standard) in the background, ensuring seamless integration and easy visualization.

## Features
- **Spring Boot Framework**: Utilizes Spring Boot to provide a REST API.
- **Event Data Processing**: Receives event data in form of cloud events, processes it, and converts it to XES.
- **Diagram Generation**: Produces diagrams based on the processed event data for easy visualization and analysis.

## Usage
To use this API, you need to have a modified version of Camunda Optimize 3.10.0 running. Camunda Optimize itself cannot be included in this repository due to licensing restrictions. The API interacts with Camunda Optimize to receive event data, process it, and generate the corresponding diagrams to be shown in the Event Based Process section of Camunda Optimize.
