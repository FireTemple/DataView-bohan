package com.bohan.entities;

import java.io.Serializable;

/**
 * @ClassName diagramStr
 * @Description TODO
 * @Author bohanxiao
 * @Data 2/12/21 12:41 PM
 * @Version 1.0
 **/
public class DiagramStr implements Serializable {

    private String diagramXML;

    public String getDiagramXML() {
        return diagramXML;
    }

    public void setDiagramXML(String diagramStr) {
        this.diagramXML = diagramStr;
    }

    @Override
    public String toString() {
        return "diagramXML{" +
                "diagramXML='" + diagramXML + '\'' +
                '}';
    }
}
