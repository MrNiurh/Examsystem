package com.ssm.sample.controller.teacher;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ssm.sample.controller.base.BaseController;

@Controller
@RequestMapping({ "/teacher" })
public class teacher_examController extends BaseController{

	@RequestMapping(value = "/")
	public ModelAndView home() {
		ModelAndView mv = this.getModelAndView();					
		mv.setViewName("teacher/teacher_exam");
		return mv;
	}
}
