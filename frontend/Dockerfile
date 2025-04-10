# --- Stage 1: Build the React application ---
    FROM node:lts-alpine AS build

    # Set the working directory inside the build stage container
    WORKDIR /app
    
    # Copy package.json and package-lock.json (or yarn.lock) first
    # to leverage Docker cache for dependencies
    COPY package*.json ./
    
    # Install project dependencies
    RUN npm install
    
    # Copy the rest of the application code
    COPY . .
    
    # Build the React app for production
    # This command typically outputs static assets to a 'dist' folder
    RUN npm run build
    
    # --- Stage 2: Serve the built assets using Nginx ---
    FROM nginx:stable-alpine
    
    # Copy the static assets from the build stage to the Nginx HTML directory
    COPY --from=build /app/dist /usr/share/nginx/html
    
    # Copy the custom Nginx configuration file
    # Assumes nginx.conf is in the same directory as the Dockerfile (frontend project root)
    COPY nginx.conf /etc/nginx/conf.d/default.conf
    
    # Expose port 80 (the default port Nginx listens on)
    EXPOSE 80
    
    # Default command to start Nginx in the foreground
    CMD ["nginx", "-g", "daemon off;"]