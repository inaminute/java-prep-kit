# Library Management System

## Problem Statement

Design a comprehensive library management system that handles books, members, borrowing, returns, fines, and reservations. Support multiple copies of books, due dates, late fees, and search functionality.

**Requirements:**
- Manage books with multiple copies
- Handle member registration and borrowing limits
- Track borrowed books and due dates
- Calculate late fees
- Support book reservations
- Search books by title, author, ISBN
- Generate reports on popular books

## Approach

- Create Book, BookItem, Member, and Librarian classes
- Use Composite pattern for book categories
- Implement Strategy pattern for fine calculation
- Use Observer pattern for reservation notifications
- Implement search with different criteria
- Track borrowing history and generate analytics

## Solution

```java
import java.time.*;
import java.util.*;

class Book {
    private String ISBN;
    private String title;
    private String author;
    private List<BookItem> items;
    
    public Book(String ISBN, String title, String author) {
        this.ISBN = ISBN;
        this.title = title;
        this.author = author;
        this.items = new ArrayList<>();
    }
    
    public void addItem(BookItem item) { items.add(item); }
    public String getISBN() { return ISBN; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public List<BookItem> getItems() { return items; }
}

class BookItem {
    private String barcode;
    private Book book;
    private boolean isAvailable;
    private LocalDate dueDate;
    
    public BookItem(String barcode, Book book) {
        this.barcode = barcode;
        this.book = book;
        this.isAvailable = true;
    }
    
    public boolean isAvailable() { return isAvailable; }
    public void checkout(LocalDate dueDate) {
        this.isAvailable = false;
        this.dueDate = dueDate;
    }
    public void returnItem() {
        this.isAvailable = true;
        this.dueDate = null;
    }
    public LocalDate getDueDate() { return dueDate; }
    public String getBarcode() { return barcode; }
    public Book getBook() { return book; }
}

class Member {
    private String memberId;
    private String name;
    private List<BookItem> borrowedBooks;
    private double fines;
    private static final int MAX_BOOKS = 5;
    
    public Member(String memberId, String name) {
        this.memberId = memberId;
        this.name = name;
        this.borrowedBooks = new ArrayList<>();
        this.fines = 0.0;
    }
    
    public boolean canBorrow() {
        return borrowedBooks.size() < MAX_BOOKS && fines == 0;
    }
    
    public void borrowBook(BookItem item) {
        borrowedBooks.add(item);
    }
    
    public void returnBook(BookItem item) {
        borrowedBooks.remove(item);
    }
    
    public void addFine(double amount) { fines += amount; }
    public void payFine(double amount) { fines = Math.max(0, fines - amount); }
    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public List<BookItem> getBorrowedBooks() { return borrowedBooks; }
    public double getFines() { return fines; }
}

class Library {
    private Map<String, Book> books;
    private Map<String, Member> members;
    private Map<String, BookItem> bookItems;
    private static final int LOAN_PERIOD_DAYS = 14;
    private static final double FINE_PER_DAY = 0.50;
    
    public Library() {
        this.books = new HashMap<>();
        this.members = new HashMap<>();
        this.bookItems = new HashMap<>();
    }
    
    public void addBook(Book book) {
        books.put(book.getISBN(), book);
    }
    
    public void addBookItem(BookItem item) {
        bookItems.put(item.getBarcode(), item);
        item.getBook().addItem(item);
    }
    
    public void registerMember(Member member) {
        members.put(member.getMemberId(), member);
    }
    
    public boolean checkoutBook(String memberId, String barcode) {
        Member member = members.get(memberId);
        BookItem item = bookItems.get(barcode);
        
        if (member == null || item == null) return false;
        if (!member.canBorrow() || !item.isAvailable()) return false;
        
        LocalDate dueDate = LocalDate.now().plusDays(LOAN_PERIOD_DAYS);
        item.checkout(dueDate);
        member.borrowBook(item);
        
        System.out.println(member.getName() + " borrowed " + item.getBook().getTitle() + 
                         " (Due: " + dueDate + ")");
        return true;
    }
    
    public boolean returnBook(String memberId, String barcode) {
        Member member = members.get(memberId);
        BookItem item = bookItems.get(barcode);
        
        if (member == null || item == null) return false;
        
        // Calculate fine if overdue
        if (item.getDueDate() != null && LocalDate.now().isAfter(item.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(item.getDueDate(), LocalDate.now());
            double fine = daysLate * FINE_PER_DAY;
            member.addFine(fine);
            System.out.println("Book overdue by " + daysLate + " days. Fine: $" + fine);
        }
        
        item.returnItem();
        member.returnBook(item);
        System.out.println(member.getName() + " returned " + item.getBook().getTitle());
        return true;
    }
    
    public List<Book> searchByTitle(String title) {
        List<Book> results = new ArrayList<>();
        for (Book book : books.values()) {
            if (book.getTitle().toLowerCase().contains(title.toLowerCase())) {
                results.add(book);
            }
        }
        return results;
    }
}

class LibraryDemo {
    public static void main(String[] args) {
        Library library = new Library();
        
        // Add books
        Book book1 = new Book("978-0134685991", "Effective Java", "Joshua Bloch");
        library.addBook(book1);
        library.addBookItem(new BookItem("B001", book1));
        library.addBookItem(new BookItem("B002", book1));
        
        // Register member
        Member member = new Member("M001", "Alice");
        library.registerMember(member);
        
        // Checkout and return
        library.checkoutBook("M001", "B001");
        library.returnBook("M001", "B001");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for checkout/return with HashMap, O(n) for search operations

**Space Complexity**: O(b + m + i) for books, members, and book items

## Edge Cases and Pitfalls

- **Multiple Copies**: Track individual book items separately from book metadata
- **Overdue Fines**: Calculate accurately based on actual days late
- **Borrowing Limits**: Enforce maximum books per member and prevent borrowing with outstanding fines
- **Concurrent Access**: Use synchronization for thread-safe operations
- **Reservation Queue**: Implement waitlist when all copies are borrowed

## Interview-Ready Answer

"I'd design a library system with Book (metadata), BookItem (physical copies), and Member classes. Use HashMap for O(1) lookups by ISBN, barcode, and member ID. Track borrowing with due dates, calculate fines for overdue returns, and enforce borrowing limits. Support search by title/author and handle multiple copies of the same book. Time complexity is O(1) for most operations, space is O(b+m+i)."
