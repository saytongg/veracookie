import re
import joblib

from nltk.corpus import stopwords
from nltk.stem import WordNetLemmatizer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.ensemble import RandomForestClassifier

import sys
sys.path.append("..")
from const import LANG_CLARITY_MODEL_PATH, VECTORIZER_PATH

def load_model() -> [RandomForestClassifier, TfidfVectorizer]:
    model = joblib.load(LANG_CLARITY_MODEL_PATH)
    vectorizer = joblib.load(VECTORIZER_PATH)
    
    return [model, vectorizer]

def rate(model : RandomForestClassifier, vectorizer : TfidfVectorizer, text : str) -> str:
    
    def preprocess_text(text : str) -> str:
        text = re.sub(r'<.*?>', '', text)
        text = re.sub(r'[^a-zA-Z\s]', '', text)
        text = text.lower()
        tokens = text.split()
        stop_words = set(stopwords.words('english'))
        tokens = [word for word in tokens if word.lower() not in stop_words]
        lemmatizer = WordNetLemmatizer()
        tokens = [lemmatizer.lemmatize(word) for word in tokens]
        preprocessed_text = ' '.join(tokens)
        
        return preprocessed_text

    preprocessed_new_data = [preprocess_text(text)]
    new_data_tfidf = vectorizer.transform(preprocessed_new_data)
    new_predictions = model.predict(new_data_tfidf)

    return new_predictions[0]