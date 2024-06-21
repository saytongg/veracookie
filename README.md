# About
VeraCookie is a web application that leverages machine learning to flag deceptive designs in cookie banners. Specifically, it utilizes two classifiers namely:
- a text classifier based on Random Forest Classifier to assess how clearly the text in the cookie banner explains the objectives of the data collection.
- an image classifier based on Vision Transformer (ViT) model to evaluate the symmetry of the options present in a cookie banner.

The web application is composed of six components, each described as follows:
- A **frontend** that acts as the user interface.
- A **controller service** that validates input and extracts the cookie banner.
- An **in-memory database** for caching recently queried links.
- The **two classifiers** mentioned above.
- A **classifications API** that exposes the classifiers to the controller service.

# Installation
The easiest way to run the application is with Docker Compose tool. First, clone this repository. After cloning, execute the following commands:
```bash
cd veracookie
docker compose up -d
```
Once the containers are running, open the browser and go to [`http://localhost:3000`](http://localhost:3000). To stop the application, run:
```bash
cd veracookie
docker compose stop
```
To start the application again, run:
```bash
cd veracookie
docker compose start
```

