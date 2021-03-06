package com.bohan.controller.users;

import com.bohan.utils.R;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @ClassName CheckValidationServlet
 * @Description The method will check the validation code in "forget password" action
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

        // get validation from front-end and session
        String validation = req.getParameter("validation");
        String validationInSession = (String)req.getSession().getAttribute("validation");


        // check if the validation code is matched
        R result = null;
        if (validation.equals(validationInSession)){
            result = R.ok();
        }else {
            result = R.error("validation code is incorrect or is expired, please try again");
        }

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        Gson gson = new Gson();
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
