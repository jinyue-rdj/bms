package com.bc.entity;

public class Book {
    private long bookId; // id
    private String name; // name
    private int number; // number

    public Book() {
    }

    public Book(long bookId, String name, int number) {
        this.bookId = bookId;
        this.name = name;
        this.number = number;
    }

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "Book [bookId=" + bookId + ", name=" + name + ", number=" + number + "]";
    }
}
