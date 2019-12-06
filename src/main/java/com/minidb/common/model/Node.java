package com.minidb.common.model;

import com.minidb.common.NodeRoleEnum;
import com.minidb.common.YamlUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Node {
    private AtomicInteger votes = new AtomicInteger(0);
    private Integer term = 0;
    private NodeRoleEnum role = NodeRoleEnum.FLOWER;
    private Boolean hasLeader = Boolean.FALSE;
    private Integer voteFor;
    private List<Node> nodes;
    private String host;
    private Integer id;
    private Integer port;
    private Integer electionPort;
    private Integer healthPort;
    private AtomicLong commitIndex;
    private AtomicLong lastApplied;

    private Node() {
    }

    private static Node instance() {
        Node node = YamlUtil.readPojo("minidb.yml", Node.class);
        node.host = System.getProperty("host");
        node.electionPort = Integer.parseInt(System.getProperty("electionPort"));
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

    @Override
    public boolean equals(Object node) {
        return node instanceof Node && ((Node) node).getId().equals(id);
    }

    public Integer getHealthPort() {
        return healthPort;
    }

    public void setHealthPort(Integer healthPort) {
        this.healthPort = healthPort;
    }

    public AtomicInteger getVotes() {
        return votes;
    }

    public void setVotes(AtomicInteger votes) {
        this.votes = votes;
    }

    public Integer incrVotes() {
        return votes.incrementAndGet();
    }

    @Override
    public String toString() {
        return "Node{" +
                "votes=" + votes +
                ", term=" + term +
                ", role=" + role +
                ", hasLeader=" + hasLeader +
                ", voteFor=" + voteFor +
                ", nodes=" + nodes +
                ", host='" + host + '\'' +
                ", id=" + id +
                ", port=" + port +
                ", electionPort=" + electionPort +
                ", healthPort=" + healthPort +
                ", commitIndex=" + commitIndex +
                ", lastApplied=" + lastApplied +
                '}';
    }
}
