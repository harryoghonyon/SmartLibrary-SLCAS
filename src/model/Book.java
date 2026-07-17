package model;

public class Book extends LibraryItem {
    private String isbn;
    private int pages;

    public Book(String id, String title, String author, int year, String isbn, int pages) {
        super(id, title, author, year);
        this.isbn = isbn;
        this.pages = pages;
    }

    @Override
    public String getType() {
        return "Book";
    }

    @Override
    public String getExtraInfo() {
        return "ISBN: " + isbn + ", Pages: " + pages;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
