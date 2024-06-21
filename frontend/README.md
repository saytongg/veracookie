# Requirements
To develop and run the project, the following must be installed:
- NodeJS with version at least 20.10.0
- `npm`
- `pnpm`

`npm` will be only used to install `pnpm`. The proponents advocate the use of `pnpm` as the default package manager for this project as it is far more efficient than `npm`. 

# Profiles
The project includes two development profiles: `local` and `production`. The `local` profile is for development and testing, while the `prod` profile is intended for deployment. **Before running the application, these profiles must be both present in the directory.** To create a `local` profile, run the following command:
```bash
cd frontend
echo REACT_APP_BACKEND_URL=localhost_url_of_backend > .env.local 
```
Similarly, to create a `production` profile, run the following command:
```bash
cd frontend
echo REACT_APP_BACKEND_URL=production_url_of_backend > .env.production 
```

# Installing the dependencies
The depedencies and plugins needed to run this application are indicated in `package.json`. To set up the project and install the dependencies, run the following command:
```bash
cd frontend
pnpm i
```

# Running the application
To run the application, use the following command:
```bash
cd frontend
pnpm start
```

# Building the application
To build the application for production, use the following command:
```bash
cd frontend
pnpm build --production
```