# Requirements
To develop and run the project, the following must be installed:
- at least Python 3.10.12
- `pip`

# Setting up the virtual environment
We recommend to set up a virtual environment to manage the project's dependencies efficiently. Use the following command to create one:
```bash
python3 -m venv classifiers
```

# Training the models
The depedencies and plugins needed to train the models are indicated in `training/requirements.txt`. To install the dependencies, run the following command:
```bash
cd training
pip3 install -r requirements.txt
```
`constant.py` file contains the directory paths for the dataset that will be used to train the classifiers. Moreover, the file also contains the paths for saving and loading the trained models.

To run the classifiers, uncomment the main functions in `training/text_classifier.py` and `training/image_classifier.py` then execute the following commands:
```bash
# To train the text classifier
cd training
python3 text_classifier.py

# To train the image classifier
cd training
python3 image_classifier.py
```
Jupyter notebooks are also provided for training the classifiers in other environments, such as Google Colab. These notebooks can be found in `training/notebooks`.

# Running the web application
The depedencies and plugins needed to run the web application are indicated in `webapp/requirements.txt`. To install the dependencies, run the following command:
```bash
cd webapp
pip3 install -r requirements.txt
```

To run the web application, use the following commands:
```bash
cd webapp
flask --app src run
```
