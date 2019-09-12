package test.mapper.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import tech.kavi.vs.mybatis.MybatisDataSourceBean;
import tech.kavi.vs.mybatis.AsyncHelperKt;
import tech.kavi.vs.web.LauncherVerticle;
import test.orm.source.BeanConfig;
import test.orm.source.dao.UserDao;
import test.orm.source.mapper.UserMapper;

@Import({BeanConfig.class, MybatisDataSourceBean.class})
@ComponentScan
public class ApplicationVerticle extends LauncherVerticle {


    @Autowired
    public UserMapper userMapper;

    @Autowired
    public UserDao userDao;

    @Override
    public void start() throws Exception {

        AsyncHelperKt.observableAsync(userDao::listUser).subscribe(System.out :: println);

        AsyncHelperKt.singleAsync(() -> userMapper.listUser()).subscribe(it -> {
            System.out.println(it);
        }, e-> {
            e.printStackTrace();
        });

    }


    public static void main( String[] args ) {
        launcher(ApplicationVerticle.class);
    }
}
