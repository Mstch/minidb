package com.minidb.consensus.raft.model;

import com.minidb.common.NodeRoleEnum;
import com.minidb.common.YamlUtil;
import com.minidb.consensus.raft.EntryStoreUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private AtomicInteger commitIndex;
    private AtomicLong lastApplied;

    public Entries getEntries() {
        return entries;
    }

    private Entries entries;
    private Map<Integer, Flower> flowers;

    public Log appendLog(Log log) {
        //先写硬盘,再写入内存（用来发送给其他flower）,并维护 index到log 的映射
        return entries.append(log);
    }


    private Node() {
    }

    private static Node instance() {
        Node node = YamlUtil.readPojo("minidb.yml", Node.class);
        node.host = System.getProperty("host");
        node.electionPort = Integer.parseInt(System.getProperty("electionPort"));
        node.id = Integer.parseInt(System.getProperty("id"));
        node.entries = new Entries(new ConcurrentHashMap<>(), EntryStoreUtil.peek().index);
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


    public AtomicInteger getVotes() {
        return votes;
    }

    public void setVotes(AtomicInteger votes) {
        this.votes = votes;
    }

    public Integer incrVotes() {
        return votes.incrementAndGet();
    }


    public AtomicInteger getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(AtomicInteger commitIndex) {
        this.commitIndex = commitIndex;
    }

    public AtomicLong getLastApplied() {
        return lastApplied;
    }

    public void setLastApplied(AtomicLong lastApplied) {
        this.lastApplied = lastApplied;
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
                ", commitIndex=" + commitIndex +
                ", lastApplied=" + lastApplied +
                '}';
    }

    public Map<Integer, Flower> getFlowers() {
        return flowers;
    }

    public class Flower {
        public AtomicInteger nextIndex;
        public AtomicInteger matchIndex;
    }
}
