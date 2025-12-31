package com.briup;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.briup.bean.User;
import com.briup.utils.UserListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author 86151
 * @program: Plant_Ai_Identify_Study
 * @description
 * @create 2025/12/18 16:45
 **/
@Slf4j
@SpringBootTest
public class ExcelReadTest {
    @Test
    public void testRead() {
        String filename = "C:\\Users\\86151\\Desktop\\项目小组联络表\\4.xlsx";
        //一次读1个sheet?一次读batchCount条数据?
        EasyExcel.read(filename,User.class,new PageReadListener<User>(new Consumer<List<User>>() {
                    @Override
                    public void accept(List<User> userList) {
                        log.info("-----开始读数据------");
                        for (User user : userList) {
                            log.info("读到一条数据",user);
                        }
                    }
                },3))
                .sheet()//无参表示读取第一个sheet 0
                .doRead();
    }
    @Test
    public void testCustomerListenerSomeSheet() {
        String filename = "C:\\Users\\86151\\Desktop\\项目小组联络表\\4.xlsx";
        // 创建 ExcelReader 对象
        ExcelReader excelReader = EasyExcel.read(filename).build();

        // 这里的 readSheet 方法可以传 SheetNo 或 SheetName，SheetNo 下标从 0 开始
        ReadSheet sheet = EasyExcel.readSheet(0)
                .head(User.class)
                //这种读取方式需要指定读的起始行，默认从第二行也就是下标为 1 读起，
                //这里由于我们使用的对象有两行行头，所以需要设置 headRowNumber 为 2
                .headRowNumber(2)
                .registerReadListener(new UserListener())
                .build();

        ReadSheet sheet2 = EasyExcel.readSheet(1)
                .head(User.class)
                .headRowNumber(2)
                .registerReadListener(new AnalysisEventListener<User>() {
                    @Override
                    public void invoke(User data, AnalysisContext context) {
                        log.info("获取的数据为:{}",data);
                    }

                    @Override
                    public void doAfterAllAnalysed(AnalysisContext context) {
                        log.info("数据读取完毕");
                    }
                })
                .build();

        // 这里的 read 方法参数是可变长度的，读取多少个就传多少个
        excelReader.read(sheet, sheet2);

        // 关闭流
        excelReader.finish();
    }
    @Test
    public void testReturnList(){
        String filename = "C:\\Users\\86151\\Desktop\\项目小组联络表\\4.xlsx";
        List<User> list = EasyExcel.read(filename)
                .head(User.class)
                .doReadAllSync();
        System.out.println(list.size());
        list.forEach(System.out::println);
    }
}
