package com.bohan.utils;

import dataview.models.DATAVIEW_BigFile;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a tool class to parse the workflow information form xml file
 */
public class ParseXMLUtil {



    public static Document ParseXML(String localFileAbsolutePath) throws Exception{

        // 1. load the workflow file just saved from "saveAs", collect it with BigFile class
        DATAVIEW_BigFile bf = new DATAVIEW_BigFile(localFileAbsolutePath);


        // 2. deal with the format, convert it to Document
        Document diagram = null;
        try {
            diagram = Utility.XMLParser.getDocument(bf.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 1. create document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        // 2. create the basic configuration
        Element experimentSpec = doc.createElement("experimentSpec");
        doc.appendChild(experimentSpec);

        Element experiment = doc.createElement("experiment");
        experimentSpec.appendChild(experiment);

        experiment.setAttribute("name", "testing");

        Element workflowBody = doc.createElement("workflowBody");
        experiment.appendChild(workflowBody);

        Element workflowGraph = doc.createElement("workflowGraph");
        workflowBody.appendChild(workflowGraph);

        Element workflowInstances = doc.createElement("workflowInstances");
        // data channel is
        Element dataChannels = doc.createElement("dataChannels");
        workflowGraph.appendChild(dataChannels);

        // input and output configuration
        Element dataProductsToPorts = doc.createElement("dataProductsToPorts");
        workflowBody.appendChild(dataProductsToPorts);

        // starting parsing documents with dynamic infomration
        // 1. Get root element
        Element rootElement = diagram.getDocumentElement();
        HashMap<String, Element> mxCells = getMxCells(rootElement);

        mxCells.values().stream().forEach(item -> {

            System.out.println("start");
            String id = item.getAttribute("id");
            System.out.println("The ID is: " + item.getAttribute("id"));

            if (item.getAttribute("source") != null && !item.getAttribute("source").equals("")) {
                // input edge
                System.out.println(item.getAttribute("target"));
                String targetType = mxCells.get(item.getAttribute("target")).getElementsByTagName("Object").item(0).getAttributes().getNamedItem("type").getTextContent();
                String sourceType = mxCells.get(item.getAttribute("source")).getElementsByTagName("Object").item(0).getAttributes().getNamedItem("type").getTextContent();
                System.out.println("target source is : "+targetType);

                // There are three cases for the edge:
                //      case1: input -> task
                //      case2: task -> task
                //      case3: task -> output

                // case1 input -> task, as dataProductsToPorts -> inputDP2PortMapping
                if (targetType.equals("task") && sourceType.equals("input")) {
                    // 1. create input node
                    Element inputDP2PortMapping = doc.createElement("inputDP2PortMapping");
                    // 2. get source and target paths
                    String sourcePath = mxCells.get(item.getAttribute("source")).getElementsByTagName("Object").item(0).getAttributes().getNamedItem("path").getTextContent();
                    NamedNodeMap targetData = mxCells.get(item.getAttribute("target")).getElementsByTagName("Object").item(0).getAttributes();
                    String targetPath = targetData.getNamedItem("path").getTextContent();
                    // 2.1 get the target Id, then combined them to workflowInstanceID + portID eg: RARename31.in0
                    String targetPortName = item.getElementsByTagName("Object").item(0).getAttributes().getNamedItem("targetPortName").getTextContent();
                    String targetId = mxCells.get(item.getAttribute("target")).getAttribute("id");
                    targetPath = targetPath.substring(targetPath.lastIndexOf("/") + 1, targetPath.indexOf(".")) + targetId + "." + targetPortName;

                    System.out.println("This is a edge");
                    System.out.println("from: " + sourcePath + " to: " + targetPath);
                    inputDP2PortMapping.setAttribute("from", sourcePath);
                    inputDP2PortMapping.setAttribute("to", targetPath);
                    dataProductsToPorts.appendChild(inputDP2PortMapping);
                }
                // case2 task -> task, as dataChannels
                else if (targetType.equals("task") && sourceType.equals("task")){
                    System.out.println("this is case 2");
                    // 1. get target & source
                    NamedNodeMap target = mxCells.get(item.getAttribute("target")).getElementsByTagName("Object").item(0).getAttributes();
                    NamedNodeMap source = mxCells.get(item.getAttribute("source")).getElementsByTagName("Object").item(0).getAttributes();
                    // 2. get data channel information
                    NamedNodeMap dataChannelInfo = item.getElementsByTagName("Object").item(0).getAttributes();
                    // 3. combined taskName , task id and ports
                    String targetId = mxCells.get(item.getAttribute("target")).getAttribute("id");
                    String sourceId = mxCells.get(item.getAttribute("source")).getAttribute("id");
                    String targetTaskName = target.getNamedItem("path").getTextContent().substring(target.getNamedItem("path").getTextContent().lastIndexOf("/") + 1, target.getNamedItem("path").getTextContent().indexOf("."));
                    String sourceTaskName = source.getNamedItem("path").getTextContent().substring(source.getNamedItem("path").getTextContent().lastIndexOf("/") + 1, source.getNamedItem("path").getTextContent().indexOf("."));
                    String sourcePortName = dataChannelInfo.getNamedItem("sourcePortName").getTextContent();
                    String targetPortName = dataChannelInfo.getNamedItem("targetPortName").getTextContent();
                    String sourcePath = sourceTaskName + sourceId + "." + sourcePortName;
                    String targetPath = targetTaskName + targetId + "." + targetPortName;

                    // 4. append to the channels
                    Element dataChannel = doc.createElement("dataChannel");
                    dataChannel.setAttribute("from", sourcePath);
                    dataChannel.setAttribute("to", targetPath);
                    dataChannels.appendChild(dataChannel);

                    // case3 task -> output, as dataProductsToPorts -> outputDP2PortMapping
                }else if (targetType.equals("output") && sourceType.equals("task")){
                    // create output node
                    Element outputDP2PortMapping = doc.createElement("outputDP2PortMapping");
                    // get the source (task) name and id
                    String taskName = item.getElementsByTagName("Object").item(0).getAttributes().getNamedItem("source").getTextContent();
                    taskName = taskName.substring(taskName.lastIndexOf("/") + 1, taskName.indexOf("."));
                    System.out.println("task name:" + taskName);
                    String sourceId = mxCells.get(item.getAttribute("source")).getAttribute("id");

                    // get the input port
                    String taskPortName = item.getElementsByTagName("Object").item(0).getAttributes().getNamedItem("sourcePortName").getTextContent();

                    taskName = taskName + sourceId + "." + taskPortName;
                    // source and target patten will be like "from="RASetDifference26.out0" to="outputDP0""
                    String targetPath = "outputDP0";
                    outputDP2PortMapping.setAttribute("from", taskName);
                    outputDP2PortMapping.setAttribute("to", targetPath);
                    System.out.println("This is a to output edge");
                    System.out.println("from: " + taskName + " to: " + targetPath);
                    dataProductsToPorts.appendChild(outputDP2PortMapping);
                }
                // case3 task -> ports
            }


            else if (item.hasChildNodes() && item.getElementsByTagName("Object").getLength() != 0) {


                // deal with the input and output

                // get the custom data
                NamedNodeMap data = item.getElementsByTagName("Object").item(0).getAttributes();

                // 1. add workflow instance
                if (data.getNamedItem("type").getTextContent().equals("task")) {
                    // 2. get taskName
                    String taskName = data.getNamedItem("path").getTextContent().substring(data.getNamedItem("path").getTextContent().lastIndexOf("/") + 1, data.getNamedItem("path").getTextContent().indexOf("."));
                    // 3. get Id
                    String instanceId = taskName + id;
                    // 4. append to the workflow to the workflowInstance
                    Element workflow = doc.createElement("workflow");
                    workflow.setTextContent(taskName);
                    Element workflowInstance = doc.createElement("workflowInstance");
                    workflowInstance.setAttribute("id", instanceId);
                    workflowInstance.appendChild(workflow);
                    workflowInstances.appendChild(workflowInstance);
                }else {
                    System.out.println("aaaaaa");
                }
            }
        });

        workflowGraph.appendChild(workflowInstances);


        TransformerFactory tfac = TransformerFactory.newInstance();
        Transformer tra = tfac.newTransformer();
        DOMSource doms = new DOMSource(doc);
        File file = new File("testing.xml");
        FileOutputStream outstream = new FileOutputStream(file);
        StreamResult sr = new StreamResult(outstream);
        tra.transform(doms, sr);


        Document spec = null;
        return doc;
    }



    public static Document startParse(String workflowName, Document mxGraphSource) {

        // 1. create document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            // 2. create the basic configuration
            Element experimentSpec = doc.createElement("experimentSpec");
            doc.appendChild(experimentSpec);

            Element experiment = doc.createElement("experiment");
            experimentSpec.appendChild(experiment);

            experiment.setAttribute("name", workflowName);

            Element workflowBody = doc.createElement("workflowBody");
            experiment.appendChild(workflowBody);

            Element workflowGraph = doc.createElement("workflowGraph");
            workflowBody.appendChild(workflowGraph);

            Element workflowInstances = doc.createElement("workflowInstances");

            // starting parsing documents with dynamic infomration
            // 1. Get root element
            Element rootElement = mxGraphSource.getDocumentElement();
            HashMap<String, Element> mxCells = getMxCells(rootElement);


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }


        return doc;

    }

    /**
     * @return java.util.HashMap<java.lang.String, org.w3c.dom.Element>
     * @Author Bohan Xiao
     * @Description extract mxCells from documents collected with HashMap
     * @Date 7:58 PM 2/4/21
     * @Param [mxGraphEl]
     **/
    private static HashMap<String, Element> getMxCells(Element mxGraphEl) {
        HashMap<String, Element> mxCells = new HashMap<String, Element>();
        NodeList mxCellsList = mxGraphEl.getElementsByTagName("mxCell");

        for (int i = 0; i < mxCellsList.getLength(); i++) {
            Element currentCell = (Element) mxCellsList.item(i);
            String id = currentCell.getAttribute("id").trim();
            mxCells.put(id, currentCell);
        }
        return mxCells;
    }

    /**
     * @return java.util.List<org.w3c.dom.Element>
     * @Author Bohan Xiao
     * @Description //TODO
     * @Date 8:06 PM 2/4/21
     * @Param [mxCells, doc]
     **/
    private static List<Element> getAllWorkflowInstance(Map<String, Element> mxCells, Document doc) {
        ArrayList<Element> workflowInstances = new ArrayList<Element>();
        Element currWorkflowInstance = null;
        for (Element mxCell : mxCells.values()) {
            // TODO change to use iterator
            currWorkflowInstance = null;
            currWorkflowInstance = mxCellToWorkflowInstance(mxCell, doc);

            // if this Element is a task then add it to the workflow instance
            if (currWorkflowInstance != null)
                workflowInstances.add(currWorkflowInstance);
        }

        return workflowInstances;
    }

    public static Element mxCellToWorkflowInstance(Element mxCell, Document doc) {
        // 1. check if this workflow is a task
        if (isWorkflowInstance(mxCell)) {
            // 2. get the task name ?
            String workflowName = getWFNameFromCell(mxCell);
            // 3. get task id, combined wih task name + id
            String instanceId = getInstanceId(mxCell);

            // 4. start to create Element
            // 4.1 workflow
            Node workflow = doc.createElement("workflow");
            workflow.setTextContent(workflowName);

            // 4.2 workflowInstance
            Node workflowInstance = doc.createElement("workflowInstance");

            // 4.3 add id
            ((Element) workflowInstance).setAttribute("id", instanceId);

            // 4.4 append to the workflow to the workflowInstance
            workflowInstance.appendChild(workflow);

            return (Element) workflowInstance;

        } else
            return null;
    }

    public static boolean isWorkflowInstance(Element mxCell) {
        if (mxCell.getAttribute("value").trim().contains("workflowComponent"))
            return true;
        return false;
    }

    public static String getWFNameFromCell(Element mxCell) {
        String result = "undefined";
        result = mxCell.getAttribute("value").trim();
        result = result.substring(result.indexOf("^gt;workflowComponent") + 22, result.indexOf("^lt;/div^gt;"));
        return result;
    }

    /**
     * @return java.lang.String
     * @Author Bohan Xiao
     * @Description get the instance id, the default value is undefined
     * @Date 8:08 PM 2/4/21
     * @Param [mxCell]
     **/
    public static String getInstanceId(Element mxCell) {
        String result = "undefined";
        result = getWFNameFromCell(mxCell) + mxCell.getAttribute("id").trim();
        return result;
    }

    public static boolean isVertex(Element mxCell) {
        return mxCell.getLastChild().getNodeName().equals("Object");
    }
}
