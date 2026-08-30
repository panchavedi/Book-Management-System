import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BookCoverComponent } from '../book-cover/book-cover';
import { Book } from '../../models/book.model';

@Component({
  selector: 'app-book-card',
  imports: [RouterLink, BookCoverComponent],
  templateUrl: './book-card.html',
  styleUrl: './book-card.scss'
})
export class BookCardComponent {
  @Input({ required: true }) book!: Book;
  @Input() admin = false;
  @Output() deleteBook = new EventEmitter<Book>();

  get availabilityLabel(): string {
    if (this.book.availableCopies > 0) return 'Available';
    return this.book.status === 'BORROWED' ? 'Borrowed' : 'Unavailable';
  }
}
