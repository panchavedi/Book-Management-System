# Libre Angular client

The source has been redesigned around live Library Service and User Service data.

## API assumptions

- User Service: `http://localhost:8081`
- Auth: `POST /auth/login`, `POST /auth/register`, `POST /auth/logout`, `GET /auth/validate`
- User profile: `GET /user/me`, `PUT /user/{id}`
- Library Service: `http://localhost:8085`
- Book images: authenticated `/books/{bookId}/images/{imageId}` endpoints

Book images are selected as browser `File` objects and uploaded to the Library Service. The Angular `public/` directory is intentionally not used as a runtime upload destination because browser-deployed static assets are not writable. The backend remains the source of truth for persistent image storage.

## Build

```bash
npm ci
npm run build
```

Do not deploy a previously generated `dist/` folder; build it from the updated source.
