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
 * @ClassName UpdateProfileServlet
 * @Description TODO
 * @Author bohanxiao
 * @Data 3/6/21 7:08 PM
 * @Version 1.0
 **/
public class UpdateProfileServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. get all information
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String organization = req.getParameter("organization");
        String jobTitle = req.getParameter("jobTitle");
        String country = req.getParameter("country");
        String password = req.getParameter("password");
        String dropboxToken = req.getParameter("dropboxToken");
        String tableLocation = getServletContext().getRealPath(req.getServletPath()).replace("updateProfile", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator + "users.table";

        boolean hasUpdate = User.updateUserInformation(email, username, organization, jobTitle, password, country,dropboxToken, tableLocation);


        // return message and code
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=utf-8");
        R result = null;
        if (hasUpdate) {
            result = R.ok();
        }else {
            result = R.error(10005,"Update failed，please contact the developer!");
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
