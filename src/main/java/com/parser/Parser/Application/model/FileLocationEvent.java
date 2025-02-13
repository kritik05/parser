package com.parser.Parser.Application.model;

public class FileLocationEvent {

    private String filePath;
    private String toolName;
    private String findingindex;

    public FileLocationEvent() {}

    public FileLocationEvent(String filePath, String toolName,String findingindex) {
        this.filePath = filePath;
        this.toolName = toolName;
        this.findingindex=findingindex;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getFindingindex() {
        return findingindex;
    }

    public void setFindingindex(String findingindex) {
        this.findingindex = findingindex;
    }

    @Override
    public String toString() {
        return "FileLocationEvent{" +
                "filePath='" + filePath + '\'' +
                ", toolName='" + toolName + '\'' +
                ", findingindex='" + findingindex + '\'' +
                '}';
    }
}
