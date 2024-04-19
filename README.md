# Project for the exam of Distributed and Pervasive Systems
This repository contains the source code used to develop a mandatory individual project to complete the exam of DPS at master's degree in Computer Science at UNIMI.
I've done the project in August 2023.

This code doesn't satisfy a lot of best practices of OOP but it is a full and correct implementation of project requirements.
## Requirements
In short, the goal of the project is to implement an Administrator Server, an Administrator Client, and a peer-to-peer system of cleaning robots that periodically send pollution measurements to the Administrator Server through MQTT, and autonomously organize themselves through gRPC when they concurrently need to go to the mechanic of the smart city.
The Administrator Server computes some statistics about the air pollution produced by the robots and makes them available to the Administrator Client.

The complete requirements list is available in the document "project_dps_2023_Greenfield.pdf".
