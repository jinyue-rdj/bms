package com.bc.service.impl;
import com.bc.BaseTest;
import com.bc.dto.AppointExecution;
import com.bc.service.BookService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class BookServiceImplTest extends BaseTest {
    @Autowired
    private BookService bookService;

    @Test
    public void testAppoint() throws Exception  {
        long bookId = 1001;
        long studentId = 12345678910L;
        AppointExecution exec = bookService.appoint(bookId, studentId);
        System.out.println(exec);
    }
}

