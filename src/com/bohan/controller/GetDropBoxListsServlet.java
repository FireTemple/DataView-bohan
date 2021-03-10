package com.bohan.controller;

import com.bohan.utils.DropBoxResult;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderBuilder;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This Servlet is for DropBox request
 */
public class GetDropBoxListsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. get the credential
        String token = (String) req.getParameter("token");
        String root = (String) req.getParameter("fileName");
        // 2. Make connection to dropbox server by using SDK
        DbxRequestConfig config = new DbxRequestConfig("en_US");
        DbxClientV2 client = new DbxClientV2(config, token);
        ListFolderResult result = null;
        Gson gson = new Gson();

        ListFolderBuilder listFolderBuilder = client.files().listFolderBuilder("");

        try {
            result = listFolderBuilder.withRecursive(true).start();
        } catch (DbxException e) {
            e.printStackTrace();
        }

        DropBoxResult dropBoxResult = new DropBoxResult();
        while (true) {

            if (result != null) {
//                System.out.println("size of entries: " + result.getEntries().size());

//                for (Metadata entry : result.getEntries()) {
//                    System.out.println(++count + "th start");
//                    Boolean isParent = true;
//                    if (entry instanceof FileMetadata) {
//                        isParent = false;
//
//                    }
//                    System.out.println("Added file: " + entry.getPathLower());
//                    if (!isParent) {
//                        System.out.println("This is a file");
//                    }
//                }

                result.getEntries().stream().filter(file -> file instanceof FileMetadata).forEach(file -> {
                    String filePath = file.getPathLower();
//                    System.out.println(filePath);
                    if (filePath.startsWith("/dataview/tasks") && filePath.endsWith(".java")){
                        dropBoxResult.getTasks().add(file);
                    }else if (filePath.startsWith("/dataview-input/")){
                        dropBoxResult.getInputs().add(file);
                    }else if (filePath.startsWith("/dataview/workflows/")){
                        dropBoxResult.getWorkflows().add(file);
                    }
                });

                if (!result.getHasMore()) {
                    break;
                }
            }
        }
        String fileList = gson.toJson(dropBoxResult);
        resp.setContentType("application/json");
        resp.getWriter().println(fileList);

    }

}
