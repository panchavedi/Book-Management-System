import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';
import { EntityOption } from '../../models/book.model';
import { EntityService } from '../../services/entity.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-entity-typeahead',
  imports: [CommonModule],
  templateUrl: './entity-typeahead.html',
  styleUrl: './entity-typeahead.scss'
})
export class EntityTypeaheadComponent implements OnChanges {
  private readonly entities = inject(EntityService);
  private readonly toast = inject(ToastService);
  private readonly search$ = new Subject<string>();

  @Input({ required: true }) label = '';
  @Input({ required: true }) kind: 'author' | 'category' = 'author';
  @Input() selected: EntityOption | null = null;
  @Input() allowCreate = true;
  @Output() selectedChange = new EventEmitter<EntityOption | null>();

  readonly query = signal('');
  readonly options = signal<EntityOption[]>([]);
  readonly loading = signal(false);
  readonly showModal = signal(false);
  readonly draftName = signal('');

  constructor() {
    this.search$
      .pipe(
        debounceTime(220),
        distinctUntilChanged(),
        switchMap((query) => {
          this.loading.set(true);
          return this.kind === 'author'
            ? this.entities.searchAuthors(query)
            : this.entities.searchCategories(query);
        })
      )
      .subscribe({
        next: (options) => {
          this.options.set(options);
          this.loading.set(false);
        },
        error: (error) => {
          this.options.set([]);
          this.loading.set(false);
          this.toast.show(error?.error?.message || `Could not load ${this.title().toLowerCase()}s.`, 'error');
        }
      });

  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['selected'] && this.selected && this.query() !== this.selected.name) {
      this.query.set(this.selected.name);
    }
  }

  onInput(value: string): void {
    this.query.set(value);
    if (this.selected && this.selected.name !== value) {
      this.selectedChange.emit(null);
    }
    this.search$.next(value.trim());
  }

  pick(option: EntityOption): void {
    this.query.set(option.name);
    this.options.set([]);
    this.selectedChange.emit(option);
  }

  openCreate(): void {
    this.draftName.set(this.query().trim());
    this.showModal.set(true);
  }

  closeCreate(): void {
    this.showModal.set(false);
  }

  create(): void {
    const name = this.draftName().trim();
    if (!name) {
      return;
    }

    const request = this.kind === 'author'
      ? this.entities.createAuthor(name)
      : this.entities.createCategory(name);

    request.subscribe({
      next: (entity) => {
        this.pick(entity);
        this.showModal.set(false);
        this.toast.show(`${this.title()} created and selected.`, 'success');
      },
      error: () => this.toast.show(`Could not create ${this.kind}.`, 'error')
    });
  }

  title(): string {
    return this.kind === 'author' ? 'Author' : 'Category';
  }
}
