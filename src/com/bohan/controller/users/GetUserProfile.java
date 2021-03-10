package com.bohan.controller.users;

import com.bohan.controller.vo.UserUpdateVo;
import com.bohan.utils.R;
import com.google.gson.Gson;
import usermgmt.Encrypt;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName GetUserProfile
 * @Description This controller will return user profile by using e-mail address
 * @Author bohanxiao
 * @Data 3/6/21 7:17 PM
 * @Version 1.0
 **/
public class GetUserProfile extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String tableLocation = getServletContext().getRealPath(req.getServletPath()).replace("profile", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator + "users.table";
        Path table = Paths.get(tableLocation);


        // create a instance of user to return user profile information.
        UserUpdateVo userUpdateVo = new UserUpdateVo();

        // find the user profile info by user email
        Stream<String> lines = Files.lines(table);
        lines.filter(record -> {
            String[] informations = record.split(",");
            // filter all records that is not matched the email.
            return informations[1].equals(email);
        }).forEach(record -> {
            // add all information to user instance
            String[] informations = record.split(",");
            System.out.println(informations.length);
            userUpdateVo.setUsername(informations[0]);
            userUpdateVo.setEmail(informations[1]);
            userUpdateVo.setOrganization(informations[2]);
            userUpdateVo.setJobTitle(informations[3]);
            userUpdateVo.setCountry(informations[4]);
            try {
                // check if user saved any token, if they saved, then decrypt, if not then just return no
                if (!informations[6].equals("no")){
                    Encrypt encrypt = new Encrypt();
                    String decryptToken = encrypt.decrypt(informations[6]);
                    userUpdateVo.setDropboxToken(decryptToken);
                }else {
                    userUpdateVo.setDropboxToken(informations[6]);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // return message and code
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=utf-8");



        Gson gson = new Gson();
        R result = R.ok();
        result.put("data", userUpdateVo);

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

    public static void main(String[] args) {
        String str = "Bohan Xiao,bohan.neal@gmail.com,,,United States,sPGMualKfKc=,sd,";
        String[] split = str.split(",");
        System.out.println(split.length);
        Arrays.stream(split).forEach(System.out::println);
    }
}
