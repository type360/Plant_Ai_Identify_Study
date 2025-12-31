package com.briup;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.briup.bean.User;
import com.briup.bean.UserVO;
import com.briup.bean.UserVO2;
import com.briup.service.UserService;
import com.briup.utils.UserListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
class ExcelDemoApplicationTests {

	@Autowired
	private UserService userService;

	//输出所有列
	@Test
	public void test01() {
		//获取数据
		List<User> list = userService.list();

		// 输出文件路径
		String filename = "D:/briup/excel/write01.xlsx";
		// EasyExcel: EasyExcelFactory 用于构建 write 和 read 对象的工厂
		// 指定使用类User去写到第一个sheet(命名为"数据列表")，写完文件流会自动关闭
		EasyExcel.write(filename, User.class)//xxx.class是excle数据生成模版
				//设置excel文件类型为xlsx
				.excelType(ExcelTypeEnum.XLSX)
				.sheet("数据列表")
				.doWrite(list);
		//注意：列名 默认 使用 User属性名
	}

	//导出时排除指定列
	@Test
	public void test02() {
		//获取数据
		List<User> list = userService.list();

		// 输出文件路径
		String filename = "D:/briup/excel/write02.xlsx";

		//添加 忽略自定义列（借助属性名实现）
		HashSet<String> excludeSet = new HashSet<>();
		excludeSet.add("id");
		excludeSet.add("createTime");

		EasyExcel.write(filename,User.class)
				//设置excel文件类型为xlsx
				.excelType(ExcelTypeEnum.XLSX)
				//设置忽略的列
				.excludeColumnFieldNames(excludeSet)
				.sheet("数据列表")
				.doWrite(list);
	}

	//输出指定列
	@Test
	public void test03() {
		//获取数据
		List<User> list = userService.list();

		// 输出文件路径
		String filename = "D:/briup/excel/write03.xlsx";

		//添加 仅输出自定义列
		HashSet<String> includeSet = new HashSet<>();
		includeSet.add("name");
		includeSet.add("address");

		EasyExcel.write(filename,User.class)
				//设置excel文件类型为xlsx
				.excelType(ExcelTypeEnum.XLSX)
				//设置输出指定列
				.includeColumnFieldNames(includeSet)
				.sheet("数据列表")
				.doWrite(list);
	}

	//使用注解忽略指定列，设置列名
	@Test
	public void test04() {
		// 获取数据
		List<User> list = userService.list();

		// 输出文件路径
		String filename = "D:/briup/excel/write04.xlsx";

		// 由 List<User>对象 转化得到 List<UserVO>对象
		// 借助bean拷贝实现：将user对象数据,拷贝到userVO对象中
		ArrayList<UserVO> userVOList = new ArrayList<>();
		// 借助 Lambda表达式(Consumer函数式接口)，实现功能
		list.forEach(user -> {
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			userVOList.add(userVO);
		});

		EasyExcel.write(filename, UserVO.class)
				//设置excel文件类型为xlsx
				.excelType(ExcelTypeEnum.XLSX)
				.sheet("数据列表")
				.doWrite(userVOList);
	}

	@Test
	public void test04_UserVO2() {
		List<User> list = userService.list();

		//准备导出文件路径
		String filename = "D:/briup/excel/write04.xlsx";

		ArrayList<UserVO2> userList = new ArrayList<>();
		list.forEach(user -> {
			UserVO2 userVO2 = new UserVO2();
			BeanUtils.copyProperties(user, userVO2);
			userList.add(userVO2);
		});

		EasyExcel.write(filename, UserVO2.class)
				.excelType(ExcelTypeEnum.XLSX)
				.sheet("数据列表")
				.doWrite(userList);
	}

	@Test
	public void test05() {
		List<User> list = userService.list();
		//1.设置输出文件路径
		String filename = "D:/briup/excel/write05.xlsx";

		//2.bean拷贝,将user对象数据,拷贝到userVO对象中
		ArrayList<UserVO> userVOList = new ArrayList<>();
		list.forEach(user -> {
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			userVOList.add(userVO);
		});

		//3.按照性别从集合数据中区分数据
		// 3.1获取性别为女的用户数据
		List<UserVO> girlList = userVOList.stream()
				.filter(userVO -> userVO.getGender().equals(0))
				.collect(Collectors.toList());
		// 3.2获取性别为男的用户数据
		List<UserVO> boyList = userVOList.stream()
				.filter(userVO -> userVO.getGender().equals(1))
				.collect(Collectors.toList());

		//4.创建导出对象，并准备2个sheet对象(按性别存储数据)
		ExcelWriter excelWriter =
				EasyExcel.write(filename, UserVO.class).build();
		//创建存储女性用户的sheet对象
		WriteSheet writerSheet =
				EasyExcel.writerSheet("女性用户").build();
		//创建存储男性用户的sheet对象
		WriteSheet writerSheet2 =
				EasyExcel.writerSheet("男性用户").build();

		//5.写出数据到sheet中
		excelWriter.write(girlList, writerSheet);
		excelWriter.write(boyList, writerSheet2);

		//6.关闭文件流
		excelWriter.finish();
	}

	/**
	 * 模拟分页获取数据
	 * @param pageNum  页码
	 * @param pageNum  页码
	 * @param userList 全部用户信息
	 */
	private List<UserVO> getDataByPage(Integer pageNum, Integer pageSize,List<User> userList) {
		// 分页处理对象到excel数据模型实体类
		List<UserVO> userVOList = userList.stream()
				.skip((long) (pageNum - 1) * pageSize)
				.limit(pageSize)
				.map(user -> {
					UserVO userVO = new UserVO();
					BeanUtils.copyProperties(user, userVO);
					return userVO;
				}).collect(Collectors.toList());

		return userVOList;
	}

	//分页导出多个sheet
	@Test
	public void test06() {
		//1.设置输出文件路径
		String filename = "D:/briup/excel/write06.xlsx";

		//2.创建ExcelWriter对象，指定使用类 UserVO 去写入数据
		ExcelWriter excelWriter =
				EasyExcel.write(filename, UserVO.class).build();

		//3.模拟分页，向Excel的同一个Sheet重复写入数据
		// 每页5条记录，从第1页开始写入
		int pageNum = 1;
		int pageSize = 5;

		//查询所有数据
		List<User> userList = userService.list();
		while(true) {
			//3.1 获取分页数据
			List<UserVO> userVOList =
					getDataByPage(pageNum, pageSize,userList);
			// 如果查询出来当前页数据为空，则结束循环
			if (userVOList == null || userVOList.isEmpty()) {
				break;
			}

			//3.2 创建Sheet对象，注意：此处可以不指定sheetNum，但sheetName必须不一样
			WriteSheet writeSheet =
					EasyExcel.writerSheet(pageNum, "数据列表" + pageNum).build();

			//3.2 写入数据到sheet中
			excelWriter.write(userVOList, writeSheet);

			//3.3 页码自增
			pageNum++;
		}

		//4.关闭文件流
		excelWriter.finish();
	}

	@Test
	public void test07() {
		// 输入文件路径
		String filename = "D:/briup/excel/write06.xlsx";
		//创建导入对象,同时指定导入的文件路径
		EasyExcel.read(filename)
				//指定使用类 UserVO 读取数据
				.head(UserVO.class)
				//注册监听器,监听器中包含对读取数据的处理逻辑,
				//这里使用EasyExcel提供的PageReadListener,采用分页读取的方式进行读取,默认每次读取100条数据
				//处理逻辑需要自己提供,分页条数也可以自己提供
				.registerReadListener(new PageReadListener<UserVO>(userList ->
				{
					log.info("--------开始读取数据--------");
					for (UserVO user : userList) {
						log.info("读取到一条数据:{}", user);
					}
				}, 3))
				//指定读取的 sheet页 无参表示读取第1个sheet 0
				.sheet()
				//开始读取数据,采用 Sax 读取
				.doRead();
	}

	//使用自定义监听器 实现excel文件的读取
	@Test
	public void test08() {
		// 输入文件路径
		String filename = "D:/briup/excel/write06.xlsx";

		// 执行execl文件的读取
		EasyExcel.read(filename, UserVO.class, new UserListener())
				.sheet()
				.doRead();
	}

	//匿名内部类形式 实现自定义监听器
	@Test
	public void test09() {
		// 输入文件路径
		String filename = "D:/briup/excel/write06.xlsx";

		//创建导入对象,同时指定导入的文件路径
		EasyExcel.read(filename)
				//指定使用类 UserVO 读取数据
				.head(UserVO.class)
				.registerReadListener(new AnalysisEventListener<UserVO>() {
					/**
					 * 单次缓存的数据量，达到该值后存储一次数据库，然后清理 list,方便内存回收
					 */
					private static final int BATCH_COUNT = 3;

					/**
					 * 缓存的数据列表
					 */
					private final List<UserVO> userList = new ArrayList<>();

					@Override
					public void invoke(UserVO data, AnalysisContext context) {
						log.info("解析一行数据:{}", data);
						userList.add(data);
						// 数据量达到设置的阈值，处理一次数据，然后清空列表，防止内存中数据量过大导致OOM
						if (userList.size() >= BATCH_COUNT) {
							// 处理数据,如输出数据
							log.info("--------开始处理数据--------");
							for (UserVO user : userList) {
								log.info("处理数据:{}", user);
							}
							// 清空数据列表
							userList.clear();
						}
					}

					@Override
					public void doAfterAllAnalysed(AnalysisContext context) {
						log.info("开始执行 doAfterAllAnalysed方法...");
						// 处理剩余的没达到阈值的数据
						if (userList.size() > 0) {
							// 处理数据,如输出数据
							log.info("--------开始处理数据--------");
							for (UserVO user : userList) {
								log.info("处理数据:{}", user);
							}
							// 清空数据列表
							userList.clear();
						}
						log.info("解析完成");
					}
				})
				.sheet()
				.doRead();
	}

	@Test
	public void test10() {
		// 输入文件路径
		String filename = "D:/briup/excel/write06.xlsx";

		// 这里 UserListener 的 doAfterAllAnalysed 会在每个 Sheet 读取完毕后调用一次。
		// 然后所有 Sheet 都会往同一个 UserListener 里写数据
		EasyExcel.read(filename, UserVO.class, new UserListener())
				.doReadAll();
	}

	@Test
	public void test11() {
		// 输入文件路径
		String filename = "D:/briup/excel/write06.xlsx";

		// 创建 ExcelReader 对象
		ExcelReader excelReader = EasyExcel.read(filename,
				UserVO.class,
				new UserListener()).build();

		// 这里的 readSheet 方法可以传 SheetNo 或 SheetName，SheetNo 下标从 0 开始
		ReadSheet sheet = EasyExcel.readSheet(0).build();
		ReadSheet sheet2 = EasyExcel.readSheet(1).build();

		// 这里的 read 方法参数是可变长度的，读取多少个就传多少个
		excelReader.read(sheet, sheet2);

		// 关闭流
		excelReader.finish();
	}

	@Test
	public void test12() {
		// 输入文件路径
		String filename = "D:/briup/excel/write06.xlsx";

		// 创建 ExcelReader 对象
		ExcelReader excelReader = EasyExcel.read(filename).build();

		// 这里的 readSheet 方法可以传 SheetNo 或 SheetName，SheetNo 下标从 0 开始
		ReadSheet sheet = EasyExcel.readSheet(0)
				.head(UserVO.class)
				//这种读取方式需要指定读的起始行，默认从第二行也就是下标为 1 读起，
				//这里由于我们使用的对象有两行行头，所以需要设置 headRowNumber 为 2
				.headRowNumber(2)
				.registerReadListener(new UserListener())
				.build();

		ReadSheet sheet2 = EasyExcel.readSheet(1)
				.head(UserVO.class)
				.headRowNumber(2)
				.registerReadListener(new AnalysisEventListener<UserVO>() {
					@Override
					public void invoke(UserVO data, AnalysisContext context) {
						log.info("获取的数据为:{}", data);
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
	public void test13() {
		// 输入文件路径
		String filename = "D:/briup/excel/error_test.xlsx";

		// 读取，观察 异常处理效果
		EasyExcel.read(filename, UserVO.class, new UserListener())
				.doReadAll();
	}

	@Test
	public void test14() {
		// 输入文件路径
		String filename = "D:/briup/excel/write06.xlsx";

		//doReadAllSync 同步读取返回结果 返回一个list集合
		List<UserVO> list = EasyExcel.read(filename,
				UserVO.class,
				new PageReadListener<UserVO>(userVOList -> {
			for (UserVO userVO : userVOList) {
				log.info("读取数据:{}", userVO);
			}
		})).doReadAllSync();

		//输出读取到的内容
		list.forEach(System.out::println);
	}

	@Test
	public void test15() {
		// 输入文件路径
		String filename = "D:/briup/excel/write06.xlsx";

		//doReadSync 同步读取返回结果 返回一个list集合
		List<UserVO> list = EasyExcel.read(filename,
						UserVO.class,
						new PageReadListener<UserVO>(userVOList -> {
					for (UserVO userVO : userVOList) {
						log.info("读取数据:{}", userVO);
					}
				}))
				//读取指定sheet数据,编号从0开始
				.sheet(2)
				.doReadSync();
		list.forEach(System.out::println);
	}
}