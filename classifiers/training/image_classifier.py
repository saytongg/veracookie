import torch
import base64
import torch.nn as nn
import matplotlib.pyplot as plt
import pandas as pd
import numpy as np

from PIL import Image
from datasets import load_dataset, Dataset, concatenate_datasets
from torchvision import transforms
from transformers import ViTModel, ViTConfig
from torch.optim import Adam
from torch.utils.data import DataLoader
from tqdm import tqdm
from sklearn.metrics import confusion_matrix, classification_report, ConfusionMatrixDisplay

import sys
sys.path.append("..")
from const import IMAGE_CLASSIFIER_DATASET_PATH, IMAGE_CLASSIFIER_PATH

MODEL_CHECKPOINT = 'google/vit-base-patch16-224-in21k'
IMAGE_DIMENSION = (224,224)
NUM_LABELS = 3

transform = transforms.Compose([
        transforms.ToTensor(),
        transforms.Resize(IMAGE_DIMENSION, antialias = True),
        transforms.Normalize(mean=[0.5, 0.5, 0.5],
                             std=[0.5, 0.5, 0.5])
        ])

###############################################################################################################################################################

class ViT(nn.Module):

  def __init__(self, config = ViTConfig(), num_labels = NUM_LABELS, model_checkpoint = MODEL_CHECKPOINT):
        super(ViT, self).__init__()
        self.vit = ViTModel.from_pretrained(model_checkpoint, add_pooling_layer = False)
        self.classifier = (nn.Linear(config.hidden_size, num_labels))

  def forward(self, x):
    x = self.vit(x)['last_hidden_state']
    output = self.classifier(x[:, 0, :])
    return output
  
class ImageDataset(torch.utils.data.Dataset):

  def __init__(self, input_data):
      self.input_data = input_data
      self.transform = transform

  def __len__(self):
      return len(self.input_data)

  def get_images(self, idx):
      return self.transform(self.input_data[idx]['image'])

  def get_labels(self, idx):
      return self.input_data[idx]['label']

  def __getitem__(self, idx):
      train_images = self.get_images(idx)
      train_labels = self.get_labels(idx)
      return train_images, train_labels
  
###############################################################################################################################################################

# Convert images in dataset to RGB
def transforms(examples):
    examples["image"] = [image.convert("RGB") for image in examples["image"]]

    return examples

def create_model() -> nn.Module:
    # For pie chart
    def autopct_format(values):
        def my_format(pct):
            total = sum(values)
            val = int(round(pct * total / 100.0))

            return '{:.1f}%\n({v:d})'.format(pct, v = val)

        return my_format

    # Load dataset
    dataset = load_dataset("imagefolder", data_dir = IMAGE_CLASSIFIER_DATASET_PATH).map(transforms, batched = True)

    # Apply data augmentation to training set
    flipped_set = dataset["train"].map(
        lambda x: {"image" : x["image"].transpose(method = Image.FLIP_TOP_BOTTOM), "label" : x["label"]}
    )

    training_set = concatenate_datasets([dataset["train"], flipped_set])
    testing_set = dataset["test"]

    # Create a mapping for labels
    labels = training_set.features["label"].names
    label2id, id2label = dict(), dict()
    for i, label in enumerate(labels):
        label2id[label] = i
        id2label[i] = label

    # Visualize data in training and test sets
    fig, (ax1, ax2) = plt.subplots(1,2)

    train_dist = pd.Series(training_set["label"]).map(lambda x : id2label[x]).value_counts()
    ax1.pie(train_dist, labels = train_dist.index, autopct = autopct_format(train_dist))
    ax1.title.set_text("Distribution of data \n in training set")

    test_dist = pd.Series(testing_set["label"]).map(lambda x : id2label[x]).value_counts()
    ax2.pie(test_dist, labels = test_dist.index, autopct = autopct_format(test_dist))
    ax2.title.set_text("Distribution of data \n in testing set")

    # Set hyperparameters
    EPOCHS = 1
    LEARNING_RATE = 0.000152
    BATCH_SIZE = 32

    # START TRAINING MODEL
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model = ViT().to(device)
    criterion = nn.CrossEntropyLoss().to(device)
    optimizer = Adam(model.parameters(), lr = LEARNING_RATE)

    # Load training images
    train_dataset = ImageDataset(training_set)
    train_dataloader = DataLoader(train_dataset, num_workers = 1, batch_size = BATCH_SIZE, shuffle = True)

    # Fine tuning loop
    for i in range(EPOCHS):
        total_acc_train = 0
        total_loss_train = 0.0

        for train_image, train_label in tqdm(train_dataloader):
            output = model(train_image.to(device))
            loss = criterion(output, train_label.to(device))
            acc = (output.argmax(dim=1) == train_label.to(device)).sum().item()
            total_acc_train += acc
            total_loss_train += loss.item()

            loss.backward()
            optimizer.step()
            optimizer.zero_grad()

        print(f'Epochs: {i + 1} | Loss: {total_loss_train / len(train_dataset): .3f} | Accuracy: {total_acc_train / len(train_dataset): .3f}')

    # END TRAINING MODEL

    # START TESTING MODEL
    # Load testing images
    test_dataset = ImageDataset(testing_set)
    test_dataloader = DataLoader(test_dataset, num_workers = 1, batch_size = BATCH_SIZE, shuffle = False)

    y_true = []
    y_pred = []
    correct = 0
    total = 0

    # Testing loop
    for images, labels in tqdm(test_dataloader):
        output = model(images.to(device))
        _, predicted = torch.max(output.data, 1)
        total += labels.size(0)
        correct += (predicted == labels.to(device)).sum()
        y_true += labels.tolist()
        y_pred += predicted.tolist()

    # Display the confusion matrix
    cm = confusion_matrix(np.array(y_true), np.array(y_pred))
    disp = ConfusionMatrixDisplay(confusion_matrix = cm, display_labels = testing_set.features["label"].names)
    disp.plot()
    plt.show()

    # Display classification report
    report = classification_report(y_true, y_pred)
    print("Classification Report:\n", report)

    # END TESTING MODEL

    return model

def save_model(model : nn.Module):
    torch.save(model.state_dict(), IMAGE_CLASSIFIER_PATH)

def eval_model(model_path : str, test_dir : str):
    # Load model
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model = ViT().to(device)
    model.load_state_dict(torch.load(model_path, map_location = device))
    model.eval()

    # Load dataset
    BATCH_SIZE = 32
    dataset = load_dataset("imagefolder", data_dir = test_dir).map(transforms, batched = True)

    # Load testing images
    testing_set = dataset["test"]
    test_dataset = ImageDataset(testing_set)
    test_dataloader = DataLoader(test_dataset, num_workers = 1, batch_size = BATCH_SIZE, shuffle = False)

    y_true = []
    y_pred = []
    correct = 0
    total = 0

    # Testing loop
    for images, labels in tqdm(test_dataloader):
        output = model(images.to(device))
        _, predicted = torch.max(output.data, 1)
        total += labels.size(0)
        correct += (predicted == labels.to(device)).sum()
        y_true += labels.tolist()
        y_pred += predicted.tolist()

    # Display the confusion matrix
    cm = confusion_matrix(np.array(y_true), np.array(y_pred))
    disp = ConfusionMatrixDisplay(confusion_matrix = cm, display_labels = testing_set.features["label"].names)
    disp.plot()
    plt.show()

    # Display classification report
    report = classification_report(y_true, y_pred)
    print("Classification Report:\n", report)

# Main function (uncomment this to train)
# model = create_model()
# save_model(model)

# Uncomment to evaluate
# model_path = IMAGE_CLASSIFIER_PATH
# test_dir = IMAGE_CLASSIFIER_DATASET_PATH
# eval_model(model_path, test_dir)
