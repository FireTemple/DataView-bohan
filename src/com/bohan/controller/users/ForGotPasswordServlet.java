package com.bohan.controller.users;

import com.bohan.utils.EmailUtils;
import com.bohan.utils.R;
import com.google.gson.Gson;
import dataview.models.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @ClassName ForGotPasswordServlet
 * @Description This method with check is the email is valid, if it is then will send a validation code to its email address
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
        // convert output as json format
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        Gson gson = new Gson();

        String tableLocation = getServletContext().getRealPath(req.getServletPath().replace("sendCode", ""))+ "WEB-INF" + File.separator + "systemFiles" + File.separator + "users.table";

        // check if this e-mail is valid user
        FileInputStream in = new FileInputStream(tableLocation);
        boolean isUser = User.existsUser(email, in);

        // if it is not a valid user email then return error message and code.
        if (!isUser){
            R result = R.error(100005, "Not Current User");
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
        }else {
            // if it is a valid user email then send the validation code, and store it to the session
            String validation = EmailUtils.sendEmail("", email);
            HttpSession session = req.getSession(true);
            session.setAttribute("validation", validation);

            // return success message and code
            R result = R.ok();
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
}
