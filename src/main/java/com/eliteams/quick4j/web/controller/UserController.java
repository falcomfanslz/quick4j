package com.eliteams.quick4j.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eliteams.quick4j.core.entity.JSONResult;
import com.eliteams.quick4j.core.entity.resp.LoginResp;
import com.eliteams.quick4j.web.model.User;
import com.eliteams.quick4j.web.security.PermissionSign;
import com.eliteams.quick4j.web.security.RoleSign;
import com.eliteams.quick4j.web.service.UserService;
import com.fasterxml.jackson.databind.util.JSONPObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 用户控制器
 * 
 * @author StarZou
 * @since 2014年5月28日 下午3:54:00
 **/
@Controller
@RequestMapping(value = "/user")
public class UserController {

    private static Logger log = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;
    private Map<String, User> users = new HashMap<String, User>();

    /**
     * 用户登录
     * 
     * @param user
     * @param result
     * @return
     */
    @RequestMapping(value = "/login", method = {RequestMethod.POST, RequestMethod.GET})
    public String login(@Valid User user, BindingResult result, Model model, HttpServletRequest request) {
        try {
            Subject subject = SecurityUtils.getSubject();
            // 已登陆则 跳到首页
            if (subject.isAuthenticated()) {
                return "redirect:/rest/";
            }
            if (result.hasErrors()) {
                model.addAttribute("error", "参数错误！");
                return "login";
            }
            // 身份验证
            UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), user.getPassword());
            token.setRememberMe(true);
            subject.login(token);
            // 验证成功在Session中保存用户信息
            final User authUserInfo = userService.selectByUsername(user.getUsername());
            request.getSession().setAttribute("userInfo", authUserInfo);
        } catch (AuthenticationException e) {
            // 身份验证失败
            model.addAttribute("error", "用户名或密码错误 ！");
            return "login";
        }
        return "redirect:/rest/";
    }

    @RequestMapping(value = "/applogin", method = {RequestMethod.POST})
    @ResponseBody
    public JSONResult<LoginResp> appLogin(@Valid User user, BindingResult result, Model model, HttpServletRequest request){
//    	{    		
//    		"respCode":"00",
//    		"respMsg":,
//    		"data":{
//    		 status:"0"    	
//    		}
//    	}
    	
        JSONResult<LoginResp> resp =new JSONResult<LoginResp>();
        LoginResp data =new LoginResp ();
        resp.setData(data);
        resp.setStatusCode(0);
    	resp.setMessage("登录成功");
    	resp.setSuccess(true);
    	resp.getData().setStatus("0");
    	resp.getData().setUsername(user.getUsername());
    	try {
            Subject subject = SecurityUtils.getSubject();
            // 已登陆则 跳到首页
            if (subject.isAuthenticated()) {
                return resp;
            }
            if (result.hasErrors()) {
            	resp.setStatusCode(0);
            	resp.setMessage("登录失败");
            	resp.getData().setStatus("1");
                return resp;
            }
            // 身份验证
            UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), user.getPassword());
            token.setRememberMe(true);
            subject.login(token);
            // 验证成功在Session中保存用户信息
            final User authUserInfo = userService.selectByUsername(user.getUsername());
            request.getSession().setAttribute("userInfo", authUserInfo);
            String sessionId = (String)SecurityUtils.getSubject().getSession().getId();
        	resp.getData().setToken(sessionId);
        	
        } catch (AuthenticationException e) {
        	resp.setStatusCode(0);
        	resp.setMessage("登录失败");
        	resp.getData().setStatus("1");
            return resp;
        }
    	
        return resp;
    }
    /**
     * 用户登出
     * 
     * @param session
     * @return
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session) {
        session.removeAttribute("userInfo");
        // 登出操作
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "login";
    }

    public String List(Model model){
        List<User> userList = userService.selectList();
        model.addAttribute("userList", userList);
        return "user/list";
    }

    /**
     * 修改用户
     * @param username
     * @param model
     * @return
     */
//    public String update(@PathVariable String username, Model model){
//        model.addAttribute();
//
//        return "user/update";
//    }


    /**
     * 基于角色 标识的权限控制案例
     */
    @RequestMapping(value = "/admin")
    @ResponseBody
    @RequiresRoles(value = RoleSign.ADMIN)
    public String admin() {
        return "拥有admin角色,能访问";
    }

    /**
     * 基于权限标识的权限控制案例
     */
    @RequestMapping(value = "/create")
    @ResponseBody
    @RequiresPermissions(value = PermissionSign.USER_CREATE)
    public String create() {
        return "拥有user:create权限,能访问";
    }


}
