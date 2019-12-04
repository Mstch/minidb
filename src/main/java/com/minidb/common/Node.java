package com.minidb.common;

public class Node {
    private Integer term = 0;
    private NodeRoleEnum role = NodeRoleEnum.FLOWER;
    private Boolean hasLeader = Boolean.FALSE;
    private Node[] nodes;
    private String host;
    private Integer id;
    private Integer port;
    private Integer electionPort;
    private Integer syncPort;
    private Node(){
        this.host = System.getProperty("host");
//        this.port = Integer.parseInt(System.getProperty("port"));
        this.port = Integer.parseInt(System.getProperty("electionPort"));
//        this.port = Integer.parseInt(System.getProperty("syncPort"));
        this.id = Integer.parseInt(System.getProperty("id"));
    }
    public final static Node instance = new Node();

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public NodeRoleEnum getRole() {
        return role;
    }

    public void setRole(NodeRoleEnum role) {
        this.role = role;
    }

    public Boolean getHasLeader() {
        return hasLeader;
    }

    public void setHasLeader(Boolean hasLeader) {
        this.hasLeader = hasLeader;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public void setNodes(Node[] nodes) {
        this.nodes = nodes;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getElectionPort() {
        return electionPort;
    }

    public void setElectionPort(Integer electionPort) {
        this.electionPort = electionPort;
    }

    public Integer getSyncPort() {
        return syncPort;
    }

    public void setSyncPort(Integer syncPort) {
        this.syncPort = syncPort;
    }
}
