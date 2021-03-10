package com.bohan.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @ClassName TestServlet
 * @Description This is testing servlet.
 * @Author bohanxiao
 * @Data 3/6/21 6:05 PM
 * @Version 1.0
 **/
public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println(getServletContext().getRealPath(req.getServletPath().replace("test", ""))+ "WEB-INF" + File.separator + "systemFiles" + File.separator + "users.table");
    }
}
