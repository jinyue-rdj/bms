package com.bc.service;

import com.bc.dto.AppointExecution;
import com.bc.entity.Book;
import java.util.List;

public interface BookService {
    Book getById(long bookId);

    List<Book> getList();

    AppointExecution appoint(long bookId, long studentId);
}
