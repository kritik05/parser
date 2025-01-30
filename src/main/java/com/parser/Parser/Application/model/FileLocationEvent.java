package com.parser.Parser.Application.model;

public class FileLocationEvent {

    private String filePath; // e.g. /tmp/scan/codescan/owner/repo/scan...
    private String owner;
    private String repo;
    private String toolName; // e.g. "codescan", "dependabot", or "secretscan"

    public FileLocationEvent() {}

    public FileLocationEvent(String filePath, String owner, String repo, String toolName) {
        this.filePath = filePath;
        this.owner = owner;
        this.repo = repo;
        this.toolName = toolName;
    }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getRepo() { return repo; }
    public void setRepo(String repo) { this.repo = repo; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    @Override
    public String toString() {
        return "FileLocationEvent{" +
                "filePath='" + filePath + '\'' +
                ", owner='" + owner + '\'' +
                ", repo='" + repo + '\'' +
                ", toolName='" + toolName + '\'' +
                '}';
    }
}
