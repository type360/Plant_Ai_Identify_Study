package com.briup.controller;

import com.alibaba.excel.EasyExcel;
import com.briup.bean.User;
import com.briup.bean.UserVO;
import com.briup.service.UserService;
import com.briup.utils.ExcelUtils;
import com.briup.utils.Result;
import com.briup.utils.UserCommonListener;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private ExcelUtils excelUtils;

	//导出数据到excel文件中
	//lombok注解：避免显式捕获或声明检查型异常
	// 在 Java 中，检查型异常必须被显式捕获或在方法签名中声明
	// 使用@SneakyThrows后，Lombok会自动生成相应的异常处理代码，方法上无需显式捕获或声明异常。
	@SneakyThrows
	@GetMapping("/export")
	public void export(HttpServletResponse response) {
		//获取用户数据
		//将用户对象数据 拷贝到 UserVO 对象中
		List<UserVO> userVOList =
				userService.list().stream().map(user -> {
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			return userVO;
		}).collect(Collectors.toList());

		//输入到浏览器中，从浏览器中下载excel
		//设置编码格式
		response.setCharacterEncoding("UTF-8");
		//设置导出文件名称（避免乱码）
		String fileName =
				URLEncoder.encode("用户数据列表".concat(".xlsx"), "UTF-8");
		//设置内容类型
		response.setHeader("content-type", "application/octet-stream");//设置响应类型，数据流格式
		//设置响应的编码格式
		response.setHeader("content-disposition", "attachment;filename=" + fileName);//设置文件格式content-disposition,设置附件，这里是写到浏览器中

		// 指定使用类 UserDataVO 去写到第一个sheet，sheet命名为 用户数据列表，写完文件流会自动关闭
		EasyExcel.write(response.getOutputStream(), UserVO.class)
				.sheet("用户数据列表").doWrite(userVOList);
	}

	@SneakyThrows
	@GetMapping("/export2")
	public void export2(HttpServletResponse response) {
		//获取用户数据
		//将用户对象数据 拷贝到 UserVO 对象中
		List<UserVO> userVOList = userService.list().stream().map(user -> {
			UserVO userVO = new UserVO();
			BeanUtils.copyProperties(user, userVO);
			return userVO;
		}).collect(Collectors.toList());

		//模拟异常
		throw new RuntimeException("出现异常了");

		//System.out.println("after RuntimeException ...");

		/*//设置编码格式
		response.setCharacterEncoding("utf-8");
		//设置导出文件名称（避免乱码）
		String fileName = URLEncoder.encode("用户数据列表".concat(".xlsx"), "UTF-8");
		//设置内容类型
		response.setHeader("content-type", "application/octet-stream");
		//设置响应的编码格式
		response.setHeader("content-disposition", "attachment;filename=" + fileName);

		// 指定使用类 UserDataVO 去写到第一个sheet，sheet命名为 用户数据列表，写完文件流会自动关闭
		EasyExcel.write(response.getOutputStream(), UserVO.class).sheet("用户数据列表").doWrite(userVOList);*/
	}

	@SneakyThrows
	@GetMapping("/export3")
	public void export3(HttpServletResponse response) {
		//1.获取用户数据列表 并 转换成 UserVO集合
		List<UserVO> userVOList =
				userService.list().stream().map(user -> {
					UserVO userVO = new UserVO();
					BeanUtils.copyProperties(user, userVO);
					return userVO;
				}).collect(Collectors.toList());

		//2.使用工具类实现excel的导出功能
		excelUtils.exportExcel(response, userVOList, UserVO.class, "用户数据列表");
	}

	@SneakyThrows
	@PostMapping("/import")
	public Result<String> importData(@RequestPart MultipartFile file) {
		//1.读取excel文件数据,获取数据
		List<UserVO> list = EasyExcel.read(file.getInputStream())
				.head(UserVO.class)
				.sheet()
				.doReadSync();

		//2.将List<UserVO>对象 转换为 List<User>对象
		List<User> userList = list.stream().map(userVO -> {
			User user = new User();
			BeanUtils.copyProperties(userVO, user);
			return user;
		}).collect(Collectors.toList());

		//3.将List<User>数据写入到数据库中
		//userService.saveBatch(userList);
		userList.forEach(System.out::println);

		return Result.success("导入成功！");
	}

	@SneakyThrows
	@PostMapping("/import2")
	public Result<String> importData2(@RequestPart MultipartFile file) {
		//1.读取excel文件数据,获取数据
		List<UserVO> list = EasyExcel.read(file.getInputStream())
				.registerReadListener(new UserCommonListener())
				.head(UserVO.class)
				.sheet()
				.doReadSync();

		//2.将List<UserVO>对象 转换为 List<User>对象
		List<User> userList = list.stream().map(userVO -> {
			User user = new User();
			BeanUtils.copyProperties(userVO, user);
			return user;
		}).collect(Collectors.toList());

		//3.将数据存入数据库中
		userService.saveBatch(userList);

		return Result.success("导入成功!");
	}

	@SneakyThrows
	@PostMapping("/import3")
	public Result<String> importData3(@RequestPart MultipartFile file) {
		//1.使用工具类 读取excel文件数据
		List<UserVO> list =
				excelUtils.importData(file, UserVO.class, new UserCommonListener());

		//2.将List<UserVO>对象 转换为 List<User>对象
		List<User> userList = list.stream().map(userVO -> {
			User user = new User();
			BeanUtils.copyProperties(userVO, user);
			return user;
		}).collect(Collectors.toList());

		//3.将数据存入数据库中
		userService.saveBatch(userList);

		return Result.success("导入成功!");
	}
}
