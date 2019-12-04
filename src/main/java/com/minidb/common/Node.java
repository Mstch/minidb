package com.minidb.common;

import sun.misc.Contended;

import java.util.List;

public class Node {
    private Integer votes = 0;
    private Integer term = 0;
    private NodeRoleEnum role = NodeRoleEnum.FLOWER;
    private Boolean hasLeader = Boolean.FALSE;
    private Integer voteFor;
    private List<Node> nodes;
    private String host;
    private Integer id;
    private Integer port;
    private Integer electionPort;
    private Integer syncPort;

    private Node() {
    }

    private static Node instance() {
        Node node = YamlUtil.readPojo("minidb.yml", Node.class);
        node.host = System.getProperty("host");
        node.port = Integer.parseInt(System.getProperty("electionPort"));
        node.id = Integer.parseInt(System.getProperty("id"));
        return node;
    }

    public final static Node instance = instance();

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

    public Integer getVoteFor() {
        return voteFor;
    }

    public void setVoteFor(Integer voteFor) {
        this.voteFor = voteFor;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }
}
