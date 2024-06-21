import torch
import torch.nn as nn
import base64

from PIL import Image
from io import BytesIO
from torchvision import transforms
from transformers import ViTModel, ViTConfig

import sys
sys.path.append("..")
from const import IMAGE_CLASSIFIER_PATH

class ViT(nn.Module):
  MODEL_CHECKPOINT = 'google/vit-base-patch16-224-in21k'
  NUM_LABELS = 3

  def __init__(self, config = ViTConfig(), num_labels = NUM_LABELS, model_checkpoint = MODEL_CHECKPOINT):
        super(ViT, self).__init__()
        self.vit = ViTModel.from_pretrained(model_checkpoint, add_pooling_layer = False)
        self.classifier = (nn.Linear(config.hidden_size, num_labels))

  def forward(self, x):
    x = self.vit(x)['last_hidden_state']
    output = self.classifier(x[:, 0, :])

    return output

########################################################################################################

def load_model() -> nn.Module:
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model = ViT().to(device)
    model.load_state_dict(torch.load(IMAGE_CLASSIFIER_PATH, map_location = device))
    model.eval()

    return model

def rate(model : nn.Module, base64img : str) -> str:
    CLASSES = {0: "ABSENT", 1: "EVEN", 2: "WEIGHTED"}
    
    # Convert base64 to image
    bytes_decoded = base64.b64decode(base64img)
    img = Image.open(BytesIO(bytes_decoded)).convert("RGB")

    # Pre-process image
    mean = [0.5, 0.5, 0.5]
    std = [0.5, 0.5, 0.5]
    IMAGE_DIMENSION = (224, 224)
    transform = transforms.Compose([
        transforms.ToTensor(),
        transforms.Resize(IMAGE_DIMENSION, antialias = True),
        transforms.Normalize(mean = mean, std = std)
    ])
    img = transform(img)

    # Get prediction
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    output = model(img.unsqueeze(0).to(device))
    prediction = output.argmax(dim = 1).item()

    return CLASSES[prediction]
