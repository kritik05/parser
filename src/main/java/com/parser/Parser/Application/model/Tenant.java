package com.parser.Parser.Application.model;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "tenant")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String repo;

    @Column(nullable = false)
    private String pat;

    @Column(nullable = false)
    private String findingindex;

    @Column(nullable = false)
    private String tenant_name;

    public Tenant(){};
    public Tenant(Integer id, String owner, String repo, String pat, String findingindex,String tenant_name) {
        this.id = id;
        this.owner = owner;
        this.repo = repo;
        this.pat = pat;
        this.findingindex = findingindex;
        this.tenant_name=tenant_name;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getPat() {
        return pat;
    }

    public void setPat(String pat) {
        this.pat = pat;
    }

    public String getFindingindex() {
        return findingindex;
    }

    public void setFindingindex(String findingindex) {
        this.findingindex = findingindex;
    }
    public String getTenant_name() {
        return tenant_name;
    }

    public void setTenant_name(String tenant_name) {
        this.tenant_name = tenant_name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tenant)) return false;
        Tenant tenant = (Tenant) o;
        return Objects.equals(id, tenant.id)
                && Objects.equals(repo, tenant.repo)
                && Objects.equals(owner, tenant.owner)
                && Objects.equals(pat, tenant.pat)
                && Objects.equals(findingindex, tenant.findingindex)
                && Objects.equals(tenant_name, tenant.tenant_name);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, repo, owner, pat, findingindex, tenant_name);
    }


    @Override
    public String toString() {
        return "Tenant{" +
                "id=" + id +
                ", owner='" + owner + '\'' +
                ", repo='" + repo + '\'' +
                ", pat='" + pat + '\'' +
                ", findingindex='" + findingindex + '\'' +
                ", tenant_name='" + tenant_name + '\'' +
                '}';
    }
}