FROM python:3.10-slim
WORKDIR /app
COPY . .
RUN pip3 install --no-cache-dir -r webapp/requirements.txt
RUN pip3 install --no-cache-dir gunicorn
EXPOSE 5000
ENTRYPOINT exec gunicorn -b 0.0.0.0:5000 --chdir webapp "src:create_app()"