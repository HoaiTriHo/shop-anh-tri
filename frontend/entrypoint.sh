#!/bin/sh
# Sinh file env.js từ template và biến môi trường
if [ -z "$API_URL" ]; then
  export API_URL="http://localhost:8080"
fi
envsubst < /usr/share/nginx/html/assets/env.template.js > /usr/share/nginx/html/assets/env.js
exec "$@" 