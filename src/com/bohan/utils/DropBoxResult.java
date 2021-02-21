package com.bohan.utils;

import com.dropbox.core.v2.files.Metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DropBoxResult implements Serializable {

    private List<Metadata> tasks = new ArrayList<>();
    private List<Metadata> inputs = new ArrayList<>();
    private List<Metadata> workflows = new ArrayList<>();

    @Override
    public String toString() {
        return "DropBoxResult{" +
                "tasks=" + tasks +
                ", inputs=" + inputs +
                ", workflows=" + workflows +
                '}';
    }

    public List<Metadata> getTasks() {
        return tasks;
    }

    public void setTasks(List<Metadata> tasks) {
        this.tasks = tasks;
    }

    public List<Metadata> getInputs() {
        return inputs;
    }

    public void setInputs(List<Metadata> inputs) {
        this.inputs = inputs;
    }

    public List<Metadata> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(List<Metadata> workflows) {
        this.workflows = workflows;
    }
}
