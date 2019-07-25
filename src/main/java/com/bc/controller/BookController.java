package com.bc.controller;

import com.bc.dto.AppointExecution;
import com.bc.dto.Result;
import com.bc.entity.Book;
import com.bc.enums.AppointStateEnum;
import com.bc.exception.NoNumberException;
import com.bc.exception.RepeatAppointException;
import com.bc.redis.RedisCache;
import com.bc.redis.RedisEvict;
import com.bc.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/book")
public class BookController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BookService bookService;

    @RequestMapping(value="/list", method=RequestMethod.GET)
    @ResponseBody
    public String list(Model model){
        List<Book> list = bookService.getList();
        model.addAttribute("list", list);
        return "list";
    }

    @RequestMapping(value="/{bookId}/detail", method = RequestMethod.GET)
    @RedisCache(type=BookController.class, fieldKey="#bookId")
    public Book detail(@PathVariable("bookId")Long bookId, Model model){
        if (bookId == null){
            //return "redirect:/book/list";
        }

        Book book = bookService.getById(bookId);
        if (book == null){
            //return "forward:/book/list";
        }

        System.out.println("The book is " + book.getName()+ " " + book.getBookId() + " " + book.getNumber());

        //model.addAttribute("book", book);
        return book;
    }

    @RequestMapping(value="/{bookId}/cleancache", method=RequestMethod.GET)
    @RedisEvict(type=BookController.class, fieldKey="#bookId")
    public void cheancache(@PathVariable("bookId")Long bookId){
        if (bookId != null) {
            System.out.println("The clean cache is " + bookId);
        }
        System.out.println("The bookId is empty");
    }

    @RequestMapping(value = "/{bookId}/appoint", method = RequestMethod.POST, produces ={"application/json;charset=utf-8"})
    @ResponseBody
    public Result<AppointExecution> appoint(@PathVariable("bookId")Long bookId, @RequestParam("studentId")Long studentId){
        if (studentId == null || studentId.equals("")){
            return new Result<AppointExecution>(false, "学号不能为空");
        }

        AppointExecution exec = null;
        try{
            exec = bookService.appoint(bookId, studentId);
        }catch (NoNumberException e1){
            exec = new AppointExecution(bookId, AppointStateEnum.NO_NUMBER);
        }catch (RepeatAppointException e2){
            exec = new AppointExecution(bookId, AppointStateEnum.REPEAT_APPOINT);
        }catch (Exception e){
            exec = new AppointExecution(bookId, AppointStateEnum.INNER_ERROR);
        }
        return new Result<AppointExecution>(true, exec);
    }
}
