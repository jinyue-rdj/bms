package com.bc.dao;

import java.util.List;
import com.bc.entity.Book;
import org.apache.ibatis.annotations.Param;

public interface BookDao {
    // 通过ID查询单本图书
    Book queryById(long id);

    // 查询所有图书
    List<Book> queryAll(@Param("offset") int offset, @Param("limit") int limit);

    // 减少馆藏数量
    int reduceNumber(long bookId);
}
