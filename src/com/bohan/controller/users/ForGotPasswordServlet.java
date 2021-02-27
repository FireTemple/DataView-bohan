package com.bohan.controller.users;

import com.bohan.utils.EmailUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @ClassName ForGotPasswordServlet
 * @Description TODO
 * @Author bohanxiao
 * @Data 2/27/21 1:00 AM
 * @Version 1.0
 **/
public class ForGotPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String validation = EmailUtils.sendEmail("", email);

        HttpSession session = req.getSession(true);
        session.setAttribute("validation", validation);

    }
}
