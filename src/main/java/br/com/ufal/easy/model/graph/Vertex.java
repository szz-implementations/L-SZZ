package br.com.ufal.easy.model.graph;

public class Vertex {

    private String hash;
    private int line;
    private String label;

    public Vertex(String hash, int line) {
        this.hash = hash;
        this.line = line;
        this.label = "";
    }

    public Vertex(String hash, int line, String label) {
        this.hash = hash;
        this.line = line;
        this.label = label;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vertex vertex = (Vertex) o;

        if (line != vertex.line) return false;
        if (!hash.equals(vertex.hash)) return false;
        return label.equals(vertex.label);
    }

    @Override
    public int hashCode() {
        int result = hash.hashCode();
        result = 31 * result + line;
        return result;
    }

    @Override
    public String toString() {
        return "Vertex{" +
                "hash='" + hash + '\'' +
                ", line=" + line +
                ", label='" + label + '\'' +
                '}';
    }


}
