# Library Frontend

Angular frontend for the digital library.

## Borrowing rules implemented in the UI

- A user can have a maximum of **5 active borrowed books**.
- A user can borrow books from **one category at a time**.
- After the first book is borrowed, books from a different category are disabled until all current books are returned.
- The same book cannot be borrowed twice by the same user.
- The Borrowed/Reading List page shows the current user's active borrowings and allows each book to be returned.
- Borrowing state is stored per authenticated user in local storage so different users on the same browser do not share the UI state.
- The previous single-book local-storage format is migrated automatically.

## Important backend note

The frontend prevents invalid borrowing actions and gives immediate feedback. The backend should enforce the same rules transactionally because local storage is only a client-side state/cache. The existing REST endpoints used by this frontend remain:

- `POST http://localhost:8082/books/{id}/borrow`
- `POST http://localhost:8082/books/{id}/return`

For production, enforce the 5-book limit and one-category rule against the authenticated user's borrowing records in the backend as well.

## Latest UI refinement
- Reduced desktop shell, sidebar, header, dashboard cards, typography, and book tiles so the interface fits common desktop viewports without horizontal scrolling.
- Reworked dashboard book covers with five distinct color palettes.
- Replaced the Monthly Goal inline conditional interpolation with Angular `@if` blocks so template code cannot appear as visible text.
- Kept borrowing rules and application behavior unchanged.
