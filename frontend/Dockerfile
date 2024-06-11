FROM node:20.11.0 AS build
WORKDIR /app
COPY . .
RUN npm install && npm run build --production

FROM node:20.11.0-alpine
WORKDIR /app
COPY --from=build /app/build /app/build
RUN npm install -g serve
EXPOSE 3000
ENTRYPOINT ["serve", "-s", "build"]