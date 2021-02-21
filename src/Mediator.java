import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.KeySpec;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import com.bohan.entities.DiagramStr;
import com.bohan.utils.ParseXMLUtil;
import com.google.gson.Gson;
import net.sf.json.groovy.GJson;
import org.apache.commons.codec.binary.Base64;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONException;
import org.json.JSONObject;

import org.w3c.dom.Document;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;

import Utility.MXGraphToSWLTranslator;
import dataview.models.*;
import dataview.planners.WorkflowPlanner;
import dataview.planners.WorkflowPlanner_T_Cluster;
import dataview.workflowexecutors.VMProvisionerAWS;
import dataview.workflowexecutors.WorkflowExecutor;
import dataview.workflowexecutors.WorkflowExecutor_Beta;
import dataview.workflowexecutors.WorkflowExecutor_Local;


/**
 * This is the main servlet that receives and responds to requests from Webbench.
 */
public class Mediator extends HttpServlet {
    /**
     * strUser: user id
     * fileLocation: task source files location for each user
     * tableLocation: the file location of the user table stored all the user registration information
     * token: dropbox token of each user
     * accessKey: AWS EC2 access key
     * secretKey: AWS EC2 secret key
     * outputMapping: map the output port index to the corresponding file name
     */
    private static String strUser = "";
    private static final long serialVersionUID = 1L;
    private static String fileLocation = "";
    private static String tableLocation = "";
    private static String token;
    private static String accessKey;
    private static String secretKey;
    private static HashMap<String, String> outputMapping = new HashMap<String, String>();
    ;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception, ServletException, IOException {
        System.out.println("start process request ----");
        System.out.println("api url: " + request.getRequestURL());
        response.setContentType("text/plain");
        String path = getServletContext().getRealPath(request.getServletPath()).replace("Mediator", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator;
        Dataview.setDebugger(path + "dataview.log");
        Dataview.debugger.setDisplay(true);
        String action = request.getParameter("action");
        System.out.println("action:" + action);
        // Initialize a folder for each user
        // TODO initializeUserFolder
        if (action.equals("initializeUserFolder")) {
//            System.out.println("Initialize Each User Storage Space");
            initializeUserFolder(request, response);


            // Save the composed workflow mxgrah information in Dropbox
        } else if (action.equals("saveAs")) {
            /**
             *  prams:
             *      name: workflow name
             *      diagram: workflow information String format convert from xml
             */

            saveAs(request.getParameter("name"), response, request);

            // Write the Dropbox token into local file
        } else if (action.equals("loadDropboxKey")) {
            loadDropboxKey(request.getParameter("userId"), request.getParameter("token"), response, request);
            // Run a workflow with Amazon EC2
        } else if (action.equals("provisionVMsInEC2AndRunWorkflows")) {
            Dataview.debugger.logSuccessfulMessage("provision vms request recieved from webench ");
            provisionVMsInEC2AndRunWorkflows(request.getParameter("userID"), request.getParameter("name"),
                    request.getParameter("accessKey"), request.getParameter("secretKey"), response);
            // Modify a composed workflow and save in Dropbox
        } else if (action.equals("overwriteAndSave")) {
            overwriteAndSave(request.getParameter("name"), request.getParameter("diagram"), response);
            // Get the mxgraph of a workflow
        } else if (action.equals("getWorkflowDiagram")) {
            getWorkflowDiagram(request.getParameter("wfPath"), response);
            // Get the dropbox token information
        } else if (action.equals("getDropboxDetails")) {
            getDropboxDetails(request.getParameter("userId"), response);
            // Get the input port numbers and output numbers of a task
        } else if (action.equals("getPortsNumber")) {
            getPortsNumber(request.getParameter("filename"), response, request);
            // Retrieve the output information from web bench
        } else if (action.equals("getData")) {
            System.out.println(request.getParameter("dataName"));
            getData(request.getParameter("dataName"), response, request);
            // Store the AWS EC2 accesskey and secretkey
        } else if (action.equals("loadCloudSettings")) {
            loadCloudSettings(request.getParameter("userID"), request.getParameter("accessKey"), request.getParameter("secretKey"));
            // Retrieve the confidential information of AWS
        } else if (action.equals("getCloudSettingDetails")) {
            getCloudSettingsDetail(request.getParameter("userId"), response);
        } else if (action.equals("stopVMs")) {
            System.out.println("This is the VM termination section");
            stopVMs(request.getParameter("userID"), response);
        } else if (action.equals("serverRunWorkflows")) {
            /**
             * Run the workflow on local machine
             *  prams:
             *      name: workflow name
             *      userID: current user
             */
            System.out.println("Run Workflow in local");
            serverRunWorkflows(request.getParameter("userID"), request.getParameter("name"), response);
        } else if (action.equals("createTree")) {

            /**
             * prams:
             *      fileName:file list
             *      token: token
             */

            createTree(request.getParameter("index"), request.getParameter("dropboxToken"), response);

        }
        // added by bohan
        else if (action.equals("getInputData")) {
            getInputData(request.getParameter("fileName"), request.getParameter("tableType"), request, response);
        } else {
            System.out.println("undefined operation!!!!!!!!!!!!!");
        }

    }

    public void createTree(String FileName, String dropboxToken, HttpServletResponse response) {
        System.out.println(FileName);
        System.out.println(dropboxToken);
        JSONArray treeNode = new JSONArray();
        String parentFileName = "";
        if (!FileName.equals("dropbox")) {
            parentFileName = FileName;
        }
        try {
            treeNode = dropboxRetrieve(parentFileName, dropboxToken);
            System.out.println(treeNode);
            response.setContentType("application/json");
            response.getWriter().print(treeNode);
        } catch (ListFolderErrorException e) {
            e.printStackTrace();
        } catch (DbxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONArray dropboxRetrieve(String parentFolder, String dropboxToken) throws ListFolderErrorException, DbxException {

        JSONArray files = new JSONArray();
        DbxRequestConfig config = new DbxRequestConfig("en_US");
        DbxClientV2 client = new DbxClientV2(config, dropboxToken);
        ListFolderResult result = null;
        if (parentFolder.equals("")) {
            result = client.files().listFolder(parentFolder);
        } else {
            if (client.files().getMetadata(parentFolder) instanceof FolderMetadata) {
                result = client.files().listFolder(parentFolder);
            } else {
                return null;
            }
        }
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                String filePath = metadata.getPathDisplay();
                Boolean isParent = true;
                if (metadata instanceof FileMetadata) {
                    isParent = false;
                }
                String name = "";
                if (filePath.contains(".class") || filePath.contains(".jar") || filePath.contains(".spec")) {
                    name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.indexOf("."));
                } else {
                    name = filePath.substring(filePath.lastIndexOf("/") + 1);
                }

                dataview.models.JSONObject node = new dataview.models.JSONObject();
                if (parentFolder.equals("")) {
                    node.put("id", new JSONValue(filePath));
                    node.put("pId", new JSONValue("dropbox"));
                    node.put("text", new JSONValue(name));
                    node.put("isParent", new JSONValue(Boolean.toString(isParent)));
                } else {
                    node.put("id", new JSONValue(filePath));
                    node.put("pId", new JSONValue(parentFolder));
                    node.put("text", new JSONValue(name));
                    node.put("isParent", new JSONValue(Boolean.toString(isParent)));
                }
                //fileNames.add(node);
                dataview.models.JSONValue jv = new dataview.models.JSONValue(node);
                files.add(jv);
            }

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }

        return files;
    }


    /**
     * Terminate all the available and pending VM instances.
     *
     * @param userId
     * @param response
     */
    public void stopVMs(String userId, HttpServletResponse response) {
        accessKey = ReadAndWrite.read(tableLocation + "users.table", userId, 7);
        secretKey = ReadAndWrite.read(tableLocation + "users.table", userId, 8);
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            PrintWriter out = null;
            try {
                out = response.getWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.println("key is empty");
        } else {
            VMProvisionerAWS.initializeProvisioner(accessKey, secretKey, "dataview1", "Dataview_key", "ami-064ab7adf0e30b152");
            ArrayList<String> vms = null;
            try {
                vms = VMProvisionerAWS.getAvailableAndPendingInstIds();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String vmid : vms) {
                VMProvisionerAWS.terminateInstance(vmid);
            }
            VMProvisionerAWS.deleteKeyPair("Dataview_key");
        }
    }

    /**
     * retrieve the accessKey and secreKey information.
     *
     * @param userId
     * @param response
     * @throws JSONException
     * @throws IOException
     */
    public void getCloudSettingsDetail(String userId, HttpServletResponse response) throws JSONException, IOException {
        accessKey = ReadAndWrite.read(tableLocation + "users.table", userId, 7);
        secretKey = ReadAndWrite.read(tableLocation + "users.table", userId, 8);
        JSONObject json = new JSONObject();
        json.put("accessKey", accessKey);
        json.put("secretKey", secretKey);
        JSONObject result = new JSONObject();
        result.put("cloudSettinglist", json);
        System.out.println(result.toString(4));
        PrintWriter out = response.getWriter();
        out.println(json.toString(4));
    }

    /**
     * Write the accessKey and secretKey into the table.
     *
     * @param userID
     * @param accessKey
     * @param secretKey
     * @throws UnsupportedEncodingException
     */
    public void loadCloudSettings(String userID, String accessKey, String secretKey) throws UnsupportedEncodingException {
        if (accessKey != null && accessKey != "" && secretKey != null && secretKey != null) {
            ReadAndWrite.write(tableLocation + "users.table", userID, accessKey, secretKey, 7, 8);
        }

    }

    /**
     * Create a folder named with the user ID (unique) to store user's task files and workflow mxgraph files
     */
    public void initializeUserFolder(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("In the initializaUserFolder " + outputMapping);
        fileLocation = getServletContext().getRealPath(request.getServletPath()).replace("Mediator", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator + strUser;
        tableLocation = getServletContext().getRealPath(request.getServletPath()).replace("Mediator", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator;
        System.out.println(tableLocation);
        File file = new File(fileLocation);
        System.out.println(fileLocation);
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        } else {
            System.out.println("The Directory is already exist");
        }

    }

    /**
     * write the maxgraph information of a workflow to each user's local file space, then upload to the Dropbox
     *
     * @param name
     * @param response
     * @throws Exception
     */
    public void saveAs(String name, HttpServletResponse response, HttpServletRequest request) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream) request.getInputStream(), "utf-8"));
        StringBuffer sb = new StringBuffer("");
        String temp;
        while ((temp = br.readLine()) != null) {
            sb.append(temp);
        }
        br.close();

        String diagramStr = sb.toString();
        Gson gson = new Gson();
        DiagramStr s = gson.fromJson(diagramStr, DiagramStr.class);

        // 1. gat token by user Id
        token = ReadAndWrite.read(tableLocation + "users.table", strUser, 6);
        // 2. do config for Dbx
        DbxRequestConfig config = new DbxRequestConfig("en_US");
        // 3. create the client by using SDK
        DbxClientV2 client = new DbxClientV2(config, token);
        // 4. save with workflow name in dropbox and local file system
        String localFileAbsolutePath = fileLocation + File.separator + name;
        String dropboxPath = "/DATAVIEW/Workflows/" + name;

        // 4.1 save in the local file system
        if (!new File(localFileAbsolutePath).exists()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(localFileAbsolutePath));
            writer.write(s.getDiagramXML());
            writer.close();
        }
        // 4.2 save in the cloud
        InputStream in = new FileInputStream(localFileAbsolutePath);
        client.files().uploadBuilder(dropboxPath).withMode(WriteMode.ADD).uploadAndFinish(in);

        // return success message
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        String jsonStr = "{\"message\":\"success\",\"code\":\"0\"}";
        PrintWriter out = null;

        try {
            out = response.getWriter();
            out.write(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * If a use entries the dropbox token information from webbench, the token will be write to the file, other token will be assigned from the file.
     *
     * @param userId
     * @param token
     * @param response
     * @throws Exception
     */
    public void loadDropboxKey(String userId, String token, HttpServletResponse response, HttpServletRequest request) throws Exception {
        if (token != null && token != "") {
            ReadAndWrite.write(tableLocation + "users.table", userId, token, 6);
        } else {
            token = ReadAndWrite.read(tableLocation + "users.table", userId, 6);
        }

        // when finished, upload the user file
        initializeUserFolder(request, response);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        String jsonStr = "{\"msg\":\"set successfully\",\"code\":\"0\"}";
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.write(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Run the current workflow with the local workflow executor.
     *
     * @param userId   login user name
     * @param name     workflow name create by user
     * @param response
     * @throws Exception
     */
    // TODO  serverRunWorkflows
    public void serverRunWorkflows(String userId, String name, HttpServletResponse response) {

        // 1. load the workflow file just saved from "saveAs", collect it with BigFile class
        String localFileAbsolutePath = fileLocation + File.separator + name;

        Document spec = null;
        try {
            // TODO 记得注释回来
//            spec = MXGraphToSWLTranslator.translateExperiment(name, diagram);
            spec = ParseXMLUtil.ParseXML(localFileAbsolutePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // testing only
        try {
            TransformerFactory tfac = TransformerFactory.newInstance();
            Transformer tra = tfac.newTransformer();
            DOMSource doms = new DOMSource(spec);
            File file = new File("newStu.xml");
            FileOutputStream outstream = new FileOutputStream(file);
            StreamResult sr = new StreamResult(outstream);
            tra.transform(doms, sr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        // 3. create the workflow with spec and file location
        GenericWorkflow GW = new GenericWorkflow(spec, fileLocation);

        GW.design();

        // 4. implement the algorithm
        WorkflowPlanner wp = new WorkflowPlanner_T_Cluster(GW);
        // here's 'plan' is coming from WorkflowPlanner_T_Cluster's override version
        GlobalSchedule gsch = wp.plan();

        // for RA schedule lenth is 1
        for (int i = 0; i < gsch.length(); i++) {
            LocalSchedule lsch = gsch.getLocalSchedule(i);
            for (int j = 0; j < lsch.length(); j++) {
                TaskSchedule tsch = lsch.getTaskSchedule(j);
                dataview.models.JSONObject taskscheduleJson = tsch.getSpecification();
                JSONArray outdcs = taskscheduleJson.get("outgoingDataChannels").toJSONArray();
                for (int k = 0; k < outdcs.size(); k++) {
                    dataview.models.JSONObject outdc = outdcs.get(k).toJSONObject();
                    if (!outdc.get("wout").isEmpty()) {
                        String outputindex = outdc.get("wout").toString().replace("\"", "");
                        if (gsch.getWorkflow().wouts[Integer.parseInt(outputindex)].getClass().equals(DATAVIEW_BigFile.class)) {
                            String outputindexfilename = ((DATAVIEW_BigFile) gsch.getWorkflow().wouts[Integer.parseInt(outputindex)]).getFilename();
                            outputMapping.put(outputindexfilename, outputindexfilename + taskscheduleJson.get("taskInstanceID").toString().replace("\"", ""));
                        }
                    }
                }
            }
        }
        // 5. get token and Execute the workflow
        token = ReadAndWrite.read(tableLocation + "users.table", strUser, 6);
        WorkflowExecutor we = new WorkflowExecutor_Local(fileLocation + File.separator, fileLocation + File.separator, token, gsch);
        try {
            we.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        String jsonStr = "{\"message\":\"running success\",\"code\":\"0\"}";
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.write(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * design a workflow based on the workflow mxgraph information. T_cluster planner is used to generate a workflow planner, on which the execute method of beta executor is called.
     *
     * @param userID
     * @param name
     * @param accessKey
     * @param secretKey
     * @param response
     * @throws Exception
     */
    public void provisionVMsInEC2AndRunWorkflows(String userID, String name, String accessKey, String secretKey,
                                                 HttpServletResponse response) throws Exception {
        Dataview.debugger.logSuccessfulMessage("Inside vm provisioner java side...");
        String localFileAbsolutePath = fileLocation + File.separator + name;
        DATAVIEW_BigFile bf = new DATAVIEW_BigFile(localFileAbsolutePath);
        //Dataview.debugger.logSuccessfulMessage(bf.toString());
        Document diagram = Utility.XMLParser.getDocument(bf.toString());
        //Dataview.debugger.logSuccessfulMessage("The diagram value is "+ Utility.XMLParser.nodeToString(diagram));
        Document spec = MXGraphToSWLTranslator.translateExperiment(name, diagram);
        //Dataview.debugger.logSuccessfulMessage("The spec value is"+ Utility.XMLParser.nodeToString(spec));
        //Dataview.debugger.logObjectValue("fileLocation is ", fileLocation);
        GenericWorkflow GW = new GenericWorkflow(spec, fileLocation);
        GW.design();
        Dataview.debugger.logObjectValue("the workflow object is ", GW.getWorkflowSpecification());
        WorkflowPlanner wp = new WorkflowPlanner_T_Cluster(GW);
        GlobalSchedule gsch = wp.plan();
        Dataview.debugger.logObjectValue("the global schedule ", gsch.getSpecification());
        System.out.println("the global schedule " + gsch.getSpecification());
        for (int i = 0; i < gsch.length(); i++) {
            LocalSchedule lsch = gsch.getLocalSchedule(i);
            for (int j = 0; j < lsch.length(); j++) {
                TaskSchedule tsch = lsch.getTaskSchedule(j);
                dataview.models.JSONObject taskscheduleJson = tsch.getSpecification();
                JSONArray outdcs = taskscheduleJson.get("outgoingDataChannels").toJSONArray();
                for (int k = 0; k < outdcs.size(); k++) {
                    dataview.models.JSONObject outdc = outdcs.get(k).toJSONObject();
                    if (!outdc.get("wout").isEmpty()) {
                        String outputindex = outdc.get("wout").toString().replace("\"", "");
                        if (gsch.getWorkflow().wouts[Integer.parseInt(outputindex)].getClass().equals(DATAVIEW_BigFile.class)) {
                            String outputindexfilename = ((DATAVIEW_BigFile) gsch.getWorkflow().wouts[Integer.parseInt(outputindex)]).getFilename();
                            outputMapping.put(outputindexfilename, outputindexfilename + taskscheduleJson.get("taskInstanceID").toString().replace("\"", ""));
                        }
                    }
                }

            }
        }
        System.out.println("In the provisionVMsInEC2AndRunWorkflows " + outputMapping);
        token = ReadAndWrite.read(tableLocation + "users.table", strUser, 6);
        String accesskey = ReadAndWrite.read(tableLocation + "users.table", strUser, 7);
        String secretkey = ReadAndWrite.read(tableLocation + "users.table", strUser, 8);
        WorkflowExecutor we = new WorkflowExecutor_Beta(fileLocation + File.separator, fileLocation + File.separator,
                gsch, token, accesskey, secretkey);
        Dataview.debugger.logSuccessfulMessage("The workflowExecutor constructor is created");
        we.execute();
        PrintWriter out = response.getWriter();
        out.println("Workflow Running Successfully");
    }

    /**
     * If the mxgraph of a workflow is changed, the file located in the Dropbox should be deleted and a new file should be created and uploaded to Dropbox.
     *
     * @param name
     * @param diagramStr
     * @param response
     * @throws Exception
     */
    public void overwriteAndSave(String name, String diagramStr, HttpServletResponse response) throws Exception {
        System.out.println("--------------------------------------------------------------------");
        System.out.println("diagramStr:" + diagramStr);
        token = ReadAndWrite.read(tableLocation + "users.table", strUser, 6);
        DbxRequestConfig config = new DbxRequestConfig("en_US");
        DbxClientV2 client = new DbxClientV2(config, token);
        String localFileAbsolutePath = fileLocation + File.separator + name;
        String dropboxPath = "/DATAVIEW/Workflows/" + name;
        FileMetadata deleteFile = (FileMetadata) client.files().delete(dropboxPath);
        // save the diagram xml code to file in disc
        if (!new File(localFileAbsolutePath).exists()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(localFileAbsolutePath));
            writer.write(diagramStr);
            writer.close();
        } else {
            new File(localFileAbsolutePath).delete();
            BufferedWriter writer = new BufferedWriter(new FileWriter(localFileAbsolutePath));
            writer.write(diagramStr);
            writer.close();
        }
        InputStream in = new FileInputStream(localFileAbsolutePath);
        // upload to the dropbox cloud
        client.files().uploadBuilder(dropboxPath).withMode(WriteMode.ADD).uploadAndFinish(in);
    }

    /**
     * Download the workflow mxgraph from Dropbox
     *
     * @param filename
     * @param response
     * @throws Exception
     */
    public void getWorkflowDiagram(String filename, HttpServletResponse response) throws Exception {
        String workflowfilename = filename.substring(filename.lastIndexOf("/") + 1);
        String localFileAbsolutePath = fileLocation + File.separator + workflowfilename;
        System.out.println(localFileAbsolutePath);

        if (!new File(localFileAbsolutePath).exists()) {
            token = ReadAndWrite.read(tableLocation + "users.table", strUser, 6);
            DbxRequestConfig config = new DbxRequestConfig("en_US");
            DbxClientV2 client = new DbxClientV2(config, token);
            String dropBoxFilePath = filename;
            DbxDownloader<FileMetadata> dl = null;
            try {
                dl = client.files().download(dropBoxFilePath);
            } catch (DownloadErrorException e) {
                String str = e.getMessage();
                if (str.contains("\"path\":\"not_found\"")) {
                    PrintWriter out = response.getWriter();
                    out.println("the specification file is not exist");
                    return;
                }
            }

            FileOutputStream fOut = new FileOutputStream(localFileAbsolutePath);
            Dataview.debugger.logSuccessfulMessage("Downloading .... " + dropBoxFilePath);
            dl.download(fOut);
            Dataview.debugger.logSuccessfulMessage("Downloading .... finished");
        }
        DATAVIEW_BigFile bf = new DATAVIEW_BigFile(localFileAbsolutePath);
        PrintWriter out = response.getWriter();
        out.println(bf.toString());
    }

    /**
     * Retrieve the Dropbox token information and will be used to show the tree elements in the webbench.
     *
     * @param userID
     * @param response
     * @throws Exception
     */
    public static void getDropboxDetails(String userID, HttpServletResponse response) throws Exception {
        // read user data query by userID ,6 means token
        token = ReadAndWrite.read(tableLocation + "users.table", userID, 6);
        JSONObject json = new JSONObject();
        json.put("token", token);
        JSONObject result = new JSONObject();
        System.out.println(result);
        result.put("dropboxlist", json);
//        System.out.println("result:"+result);
        System.out.println(result.toString(4));
        PrintWriter out = response.getWriter();
        out.println(json.toString(4));
    }

    /**
     * Download the task source file (class or jar format) and retrieve the task specification information and will be used to retrieve the input ports number and output ports number
     *
     * @param filename
     * @param response
     * @throws Exception
     */
    public void getPortsNumber(String filename, HttpServletResponse response, HttpServletRequest request) {
        String task = filename.substring(filename.lastIndexOf("/") + 1);
        String taskName = task.substring(0, task.lastIndexOf("."));
        // initialize the filelocation if it's empty
        if (fileLocation.isEmpty() || fileLocation == null) {
            initializeUserFolder(request, response);
        }
        String localFileAbsolutePath = fileLocation + File.separator + task;
        System.out.println("The localFileAbsolutePath:" + localFileAbsolutePath);
        String Location;
        // check if need to download the task
        if (!new File(localFileAbsolutePath).exists()) {
            token = ReadAndWrite.read(tableLocation + "users.table", strUser, 6);
            // 1. create configuration for Dropbox
            DbxRequestConfig config = new DbxRequestConfig("en_US");
            // 2. create client with configuration and token
            DbxClientV2 client = new DbxClientV2(config, token);
            String dropBoxFilePath = filename;
            // 3. download specific file
            DbxDownloader<FileMetadata> dl = null;
            try {
                // 3.1 download file
                dl = client.files().download(dropBoxFilePath);
            } catch (DbxException e) {
                String str = e.getMessage();
                if (str.contains("\"path\":\"not_found\"")) {
                    PrintWriter out = null;
                    try {
                        out = response.getWriter();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    out.println("the specification file is not exist");
                    return;
                }
            }
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(localFileAbsolutePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println("Downloading .... " + dropBoxFilePath);
            // 3.2 transfer data to local file system
            try {
                dl.download(fOut);
            } catch (DbxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Downloading .... finished! file position in local machine" + localFileAbsolutePath);
        }

        if (new File(fileLocation + File.separator + taskName + ".jar").exists()) {
            Location = fileLocation + File.separator + taskName + ".jar";
        } else {
            Location = fileLocation;
        }

        File clazzPath = new File(Location);
        Task newtask = null;
        try {
            URL url = null;
            url = clazzPath.toURI().toURL();
            URL[] urls = new URL[]{url};
            // 因为这个java类是载入进来的 属于运行时加入 所以需要手动添加到classloader里面
            Thread.currentThread().setContextClassLoader(new URLClassLoader(urls, Thread.currentThread().getContextClassLoader()));
            // TODO 学习一下类加载器
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            Class<?> taskclass = Class.forName(taskName, true, currentClassLoader);
            // 所以必须继承task 否则这里报错
            newtask = (Task) taskclass.getDeclaredConstructor().newInstance();
            Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | MalformedURLException | ClassNotFoundException | InstantiationException e) {
            e.printStackTrace();
            Dataview.debugger.logException(e);
        }

        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 这里返回关于task的所有输入输出要求
        out.println(newtask.getTaskSpecification());

    }

    /**
     * Download the final output files of a workflow from Dropbox and show the content in the webbench if the size is less than 1024.
     *
     * @param dataName
     * @param response
     * @throws Exception
     */
    public static void getData(String dataName, HttpServletResponse response, HttpServletRequest request) throws Exception {

        // added by Bohan, this display type is use for ready data with
        String displayType = request.getParameter("displayType");

        String filename = dataName.substring(dataName.lastIndexOf("/") + 1);
        System.out.println("This is the file name: before " + filename);
        if (!new File(fileLocation + File.separator + filename).exists()) {
            filename = (String) outputMapping.get(filename);
            System.out.println("This is the file name: " + filename);

        }
        String localFileAbsolutePath = fileLocation + File.separator + filename;
        System.out.println(localFileAbsolutePath);

        if (!new File(localFileAbsolutePath).exists()) {
            token = ReadAndWrite.read(tableLocation + "users.table", strUser, 6);
            DbxRequestConfig config = new DbxRequestConfig("en_US");
            DbxClientV2 client = new DbxClientV2(config, token);
            String dropBoxFilePath = dataName.substring(0, dataName.lastIndexOf("/") + 1) + filename;
            System.out.println(dropBoxFilePath);
            DbxDownloader<FileMetadata> dl = null;
            try {
                dl = client.files().download(dropBoxFilePath);
            } catch (DownloadErrorException e) {
                String str = e.getMessage();
                if (str.contains("\"path\":\"not_found\"")) {
                    PrintWriter out = response.getWriter();
                    out.println("the output file is not exist");
                    return;
                }
            }
            FileOutputStream fOut = new FileOutputStream(localFileAbsolutePath);
            System.out.println("Downloading .... " + dropBoxFilePath);
            dl.download(fOut);
            System.out.println("Downloading .... finished");
        }
        File file = new File(localFileAbsolutePath);
        PrintWriter out = response.getWriter();


        if (file.length() < 1024 * 5) {
            DATAVIEW_BigFile bf = new DATAVIEW_BigFile(localFileAbsolutePath);
            out.println(bf.toString());
        } else {
            out.println("TOO MUCH DATA Please Go To " + dataName.substring(0, dataName.lastIndexOf("/")) + filename);
        }
    }

    /**
     * get input data from local or dropbox
     *
     * @param fileName
     * @param tableType
     * @param request
     * @param response
     */
    public void getInputData(String fileName, String tableType, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String filename = fileName.substring(fileName.lastIndexOf("/") + 1);
        if (fileLocation == null || fileLocation.equals("")) {
            fileLocation = getServletContext().getRealPath(request.getServletPath()).replace("Mediator", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator + strUser;
            tableLocation = getServletContext().getRealPath(request.getServletPath()).replace("Mediator", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator;
        }

        System.out.println("The name of the file that about to download:" + fileName);

        // check if this file is already downloaded before
        if (!new File(fileLocation + File.separator + filename).exists()) {
            filename = (String) outputMapping.get(filename);
            System.out.println("This is the file name: " + filename);
        }
        String localFileAbsolutePath = fileLocation + File.separator + filename;

        if (!new File(localFileAbsolutePath).exists()) {
            token = ReadAndWrite.read(tableLocation + "users.table", strUser, 6);
            DbxRequestConfig config = new DbxRequestConfig("en_US");
            DbxClientV2 client = new DbxClientV2(config, token);
            String dropBoxFilePath = fileName.substring(0, fileName.lastIndexOf("/") + 1) + filename;
            System.out.println(dropBoxFilePath);
            DbxDownloader<FileMetadata> dl = null;
            try {
                dl = client.files().download(dropBoxFilePath);
            } catch (DownloadErrorException e) {
                String str = e.getMessage();
                if (str.contains("\"path\":\"not_found\"")) {
                    PrintWriter out = response.getWriter();
                    out.println("the output file is not exist");
                    return;
                }
            } catch (DbxException e) {
                e.printStackTrace();
            }

            FileOutputStream fOut = new FileOutputStream(localFileAbsolutePath);
            System.out.println("Downloading .... " + dropBoxFilePath);
            try {
                dl.download(fOut);
            } catch (DbxException e) {
                e.printStackTrace();
            }
            System.out.println("Downloading .... finished");
        }
        File file = new File(localFileAbsolutePath);
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (file.length() < 1024 * 2) {
            Map<String, List<String>> result = new HashMap<>();
            List<String> returnHeaders = new ArrayList<>();
            if (tableType != null && tableType.equals("table")) {
                //read file into stream, try-with-resources
                // init first line as table header
                try (Stream<String> stream = Files.lines(Paths.get(localFileAbsolutePath))) {
                    stream.limit(1).forEach(headers -> {
                        returnHeaders.addAll(Arrays.asList(headers.split(",")));
                    });
                    result.put("headers", returnHeaders);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                try (Stream<String> stream = Files.lines(Paths.get(localFileAbsolutePath))) {
                    List<String> contents = stream.skip(1).collect(Collectors.toList());
                    result.put("contents", contents);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                try (Stream<String> stream = Files.lines(Paths.get(localFileAbsolutePath))) {
                    List<String> contents = stream.collect(Collectors.toList());
                    result.put("contents", contents);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String s = gson.toJson(result);
            out.println(s);
            out.flush();
            out.close();
        } else {
            Map<String, String> result = new HashMap<>();
            result.put("message", "TOO MUCH DATA Please Go To " + fileName.substring(0, fileName.lastIndexOf("/")) + filename);
            result.put("code", "200001");
            String json = gson.toJson(result);
            out.println(json);
            out.flush();
            out.close();
        }
    }

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Mediator() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {

            HttpSession session = request.getSession(true);
            // record the userId, that's why need to login first, or will cause Null pointer
            strUser = session.getAttribute("UserID").toString();
            processRequest(request, response);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {

            System.out.println("Entering Mediator Controller -------");
            HttpSession session = request.getSession(true);

            if (session.getAttribute("UserID") == null) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json; charset=utf-8");
                String jsonStr = "{\"message\":\"You need to sign in first or your session is expired\",\"code\":\"100002\"}";
                PrintWriter out = null;

                try {
                    out = response.getWriter();
                    out.write(jsonStr);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    if (out != null) {
                        out.close();
                    }

                }
            }else {
                strUser = session.getAttribute("UserID").toString();
                System.out.println("before process request");
                processRequest(request, response);
                System.out.println("finished");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

}

class TrippleDes {

    private static final String UNICODE_FORMAT = "UTF8";
    public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    private KeySpec ks;
    private SecretKeyFactory skf;
    private Cipher cipher;
    byte[] arrayBytes;
    private String myEncryptionKey;
    private String myEncryptionScheme;
    SecretKey key;

    public TrippleDes() throws Exception {
        myEncryptionKey = "ThisIsSpartaThisIsSparta";
        myEncryptionScheme = DESEDE_ENCRYPTION_SCHEME;
        arrayBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
        ks = new DESedeKeySpec(arrayBytes);
        skf = SecretKeyFactory.getInstance(myEncryptionScheme);
        cipher = Cipher.getInstance(myEncryptionScheme);
        key = skf.generateSecret(ks);
    }


    public String encrypt(String unencryptedString) {
        String encryptedString = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
            byte[] encryptedText = cipher.doFinal(plainText);
            encryptedString = new String(Base64.encodeBase64(encryptedText));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedString;
    }


    public String decrypt(String encryptedString) {
        String decryptedText = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedText = Base64.decodeBase64(encryptedString);
            byte[] plainText = cipher.doFinal(encryptedText);
            decryptedText = new String(plainText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedText;
    }
}
