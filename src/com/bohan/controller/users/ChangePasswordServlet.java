package com.bohan.controller.users;

import com.bohan.utils.R;
import com.google.gson.Gson;
import dataview.models.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @ClassName ChangePasswordServlet
 * @Description This method is for reset password after user passed the email validation
 * @Author bohanxiao
 * @Data 3/5/21 4:18 PM
 * @Version 1.0
 **/
public class ChangePasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // get email & new password
        String email = req.getParameter("email");
        String newPassword = req.getParameter("newPassword");

        // do reset
        String tableLocation = getServletContext().getRealPath(req.getServletPath().replace("resetPassword", ""))+ "WEB-INF" + File.separator + "systemFiles" + File.separator + "users.table";
        boolean isSuccess = User.resetPassword(email, newPassword, tableLocation);

        // return message and code
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=utf-8");
        R result = null;
        if (isSuccess) {
            result = R.ok();
        }else {
            result = R.error(10004,"An error happened，please try again!");
        }

        Gson gson = new Gson();
        PrintWriter out = null;
        try {
            out = resp.getWriter();
            out.write(gson.toJson(result));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
