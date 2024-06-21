import os
import nltk

from flask import Flask, request
from src.text_classifier import load_model as load_lang_model, rate as rate_text
from src.image_classifier import load_model as load_opt_model, rate as rate_options

def create_app(test_config = None):
    # Create and configure the app
    app = Flask(__name__, instance_relative_config = True)

    # Ensure the instance folder exists
    try:
        os.makedirs(app.instance_path)
    except OSError:
        pass

    # Initialize vectorizers and models
    nltk.download('stopwords')
    nltk.download('wordnet')
    rf_classifier, vectorizer = load_lang_model()
    ViTModel = load_opt_model()

    # Test endpoint
    @app.route('/ping', methods = ['GET'])
    def ping():
        return "Hello world!"
    
    # Classification endpoint
    @app.route('/classify', methods = ['POST'])
    def classify():
        # Get cookie banner text and image (encoded in base64)
        req = request.get_json()
        bannerText, bannerImage = req['text'], req['image']

        # Rate cookie banner text
        textRating = rate_text(rf_classifier, vectorizer, bannerText)

        # Rate options in cookie banner
        imageRating = rate_options(ViTModel, bannerImage)
        
        # Return ratings
        ratings = {}
        ratings["textRating"] = textRating
        ratings["imageRating"] = imageRating
        return ratings

    return app