package com.bc.controller;

import com.bc.dto.AppointExecution;
import com.bc.dto.Result;
import com.bc.entity.Book;
import com.bc.enums.AppointStateEnum;
import com.bc.exception.NoNumberException;
import com.bc.exception.RepeatAppointException;
import com.bc.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/book")
public class BookController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BookService bookService;

    @RequestMapping(value="/list", method=RequestMethod.GET)
    private String List(Model model){
        List<Book> list = bookService.getList();
        model.addAttribute("list", list);
        return "list";
    }

    @RequestMapping(value="/{bookId}/detail", method = RequestMethod.GET)
    private String detail(@PathVariable("bookId")Long bookId, Model model){
        if (bookId == null){
            return "redirect:/book/list";
        }

        Book book = bookService.getById(bookId);
        if (book == null){
            return "forward:/book/list";
        }
        model.addAttribute("book", book);
        return "detail";
    }

    @RequestMapping(value = "/{bookId}/appoint", method = RequestMethod.POST, produces ={"application/json;charset=utf-8"})
    @ResponseBody
    private Result<AppointExecution> appoint(@PathVariable("bookId")Long bookId, @RequestParam("studentId")Long studentId){
        if (studentId == null || studentId.equals("")){
            return new Result<>(false, "学号不能为空");
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
