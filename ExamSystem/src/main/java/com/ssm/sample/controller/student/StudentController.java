package com.ssm.sample.controller.student;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.ssm.sample.controller.base.BaseController;
import com.ssm.sample.facade.student.StudentFacade;
import com.ssm.sample.facade.teacher.TeacherFacade;
import com.ssm.sample.util.PageData;

@Controller
@RequestMapping({ "/student" })
public class StudentController extends BaseController {

	@Autowired
	StudentFacade studentFacade;
	@Autowired
	TeacherFacade teacherFacade;

	/*
	 * 
	 * 学生主页面
	 */
	@RequestMapping({ "/" })
	public ModelAndView home() {
		ModelAndView mv = this.getModelAndView();
		mv.setViewName("student/student");
		return mv;
	}

	@ResponseBody
	@RequestMapping("/updateStudentIp")
	public Object updateStudentIp() {
		PageData pd = this.getPageData();

		boolean b = false;
		try {
			ServletContext application = request.getSession().getServletContext();
			pd.put("testid", application.getAttribute("testid"));
			b = this.studentFacade.updateStudentIp(pd);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return b;
	}

	@ResponseBody
	@RequestMapping("/downTest")
	private void down(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// 根据考试 id 获取考试信息
		// list = this.studentFacade.selectTestById(testid);
		ServletContext application = request.getSession().getServletContext();
		List<PageData> list = (List<PageData>) application.getAttribute("RunTest");
		System.out.println(list);
		if (list != null) {
			String path = request.getServletContext().getRealPath("/") + "ExamSystem/"
					+ list.get(0).getString("testname") + "/";
			System.out.println(path);
			String fileName = list.get(0).getString("submit");
			fileName = new String(fileName.getBytes(), "ISO8859-1");
			//System.out.println(path + fileName);
			File file = new File(path + fileName);
			//System.out.println(file);
			if (file != null) {
				//System.out.println("文件的名字：" + fileName);
				response.addHeader("content-disposition", "attachment;filename=" + fileName);
				FileUtils.copyFile(file, response.getOutputStream());
			}

		}
	}

	@ResponseBody
	@RequestMapping("/selectAllInfos")
	public Object selectInfos() {
		List<PageData> list = this.teacherFacade.selectInfos();
		session.setAttribute("infos", list);
		return list;
	}

	@ResponseBody
	@RequestMapping("/studentUpload")
	public String upload(MultipartFile file, HttpServletRequest request, HttpServletRequest reponse)
			throws IOException {
		PageData pd = this.getPageData();
		ServletContext application = request.getSession().getServletContext();
		// 根据考试 id 获取考试信息
		String testid = (String) application.getAttribute("testid");
		List<PageData> list = this.studentFacade.selectTestById(testid);
		pd.put("testid", testid);
		// System.out.println(pd);
		List<PageData> student = this.teacherFacade.selectAllStudent(pd);
		List<PageData> system = this.teacherFacade.selectSystem();
		// System.out.println(student);

		// 保存路径为考试名
		String path = request.getSession().getServletContext()
				.getRealPath("/ExamSystem/" + list.get(0).getString("testname") + "/"
						+ student.get(0).getString("stuid") + "_" + student.get(0).getString("stuname"));
		System.out.println("path>>" + path);
		if (file != null) {
			String fileName = file.getOriginalFilename();
			// 将文件信息加入到数据库
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
			String date = df.format(new Date());
			/*
			 * 文件大小检测
			 */
			long filesize = file.getSize();
			long minSize = (long) system.get(0).get("file_minsize");
			long maxSize = (long) system.get(0).get("file_maxsize");
			// System.out.println(minSize);
			if (minSize > filesize) {
				session.setAttribute("min", minSize + "Byte");
				return "min";
			}
			if (maxSize < filesize) {
				session.setAttribute("max", maxSize + "Byte");
				return "max";
			}
			pd.put("filename", fileName);
			pd.put("fileuptime", date);
			pd.put("filesize", filesize + "Byte");
			// System.out.println("file" + pd);
			// System.out.println("fileName>>" + fileName);
			this.studentFacade.insertUploadFile(pd);
			File dir = new File(path, fileName);
			// System.out.println("dir.exists()>>" + dir.exists());
			if (!dir.exists()) {
				dir.mkdirs();
			}
			// System.out.println("dir.exists()>>" + dir.exists());
//			MultipartFile自带的解析方法
			file.transferTo(dir);
		}

		return "ok";
	}
}