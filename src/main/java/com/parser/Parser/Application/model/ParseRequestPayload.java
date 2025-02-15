package com.parser.Parser.Application.model;


public class ParseRequestPayload {
    private  String filePath;
    private Integer tenantId; // or Long, depending on your system
    private String tooltype;

    public ParseRequestPayload(){};

    public ParseRequestPayload(Integer tenantId, String filePath,String tooltype) {
        this.tenantId = tenantId;
        this.filePath = filePath;
        this.tooltype=tooltype;
    }

    public String getTooltype() {
        return tooltype;
    }

    public void setTooltype(String tooltype) {
        this.tooltype = tooltype;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }
}