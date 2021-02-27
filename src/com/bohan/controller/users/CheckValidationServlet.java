package com.bohan.controller.users;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @ClassName CheckValidationServlet
 * @Description TODO
 * @Author bohanxiao
 * @Data 2/27/21 1:09 AM
 * @Version 1.0
 **/
public class CheckValidationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String validation = req.getParameter("validation");
        String validationInSession = (String)req.getSession().getAttribute("validation");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=utf-8");
        String jsonStr = null;
        if (validation.equals(validationInSession)){
            jsonStr = "{\"msg\":\"information correct\",\"code\":\"0\"}";
        }else {
            jsonStr = "{\"msg\":\"code is not matched the record\",\"code\":\"100002\"}";
        }

        PrintWriter out = null;
        try {
            out = resp.getWriter();
            out.write(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
