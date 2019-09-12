package test.orm.source.mapper2;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import test.orm.source.entity.Book;
import test.orm.source.entity.User;

import java.util.List;

@Mapper
public interface BookMapper {

    @Select("select * from book")
    public List<Book> listBook();

    @Insert("INSERT INTO book (name) VALUES (#{name})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    public Integer save(Book book);
}
