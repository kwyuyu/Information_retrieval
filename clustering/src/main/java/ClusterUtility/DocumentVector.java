package ClusterUtility;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DocumentVector implements Map<String, Double> {

    private Map<String, Double> vector;

    public DocumentVector(Map<String, Double> vector) {
        this.vector = vector;
    }

    public DocumentVector() {
        this.vector = new HashMap<>();
    }

    public double dot(DocumentVector other) {
        double result = 0.0;
        for (String term: this.keySet()) {
            result += other.containsKey(term) ? this.get(term) * other.get(term) : 0.0;
        }
        return result;
    }

    public double sumSquare() {
        double result = 0.0;
        for (double num: this.values()) {
            result += Math.pow(num, 2);
        }
        return result;
    }

    public void add(DocumentVector dv) {
        for (String term: dv.keySet()) {
            this.put(term, this.getOrDefault(term, 0.0) + dv.get(term));
        }
    }

    public void multiply(double mul) {
        for (String term: this.keySet()) {
            this.put(term, this.get(term) * mul);
        }
    }

    public void divide(double div) {
        if (div == 0) return;
        for (String term: this.keySet()) {
            this.put(term, this.get(term) / div);
        }
    }

    public void printDocumentVector() {
        for (String term: this.keySet()) {
            System.out.println(term + " " + this.get(term));
        }
    }







    // Map interface

    @Override
    public int size() {
        return this.vector.size();
    }

    @Override
    public boolean isEmpty() {
        return this.vector.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.vector.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.vector.containsValue(value);
    }

    @Override
    public Double get(Object key) {
        return this.vector.get(key);
    }

    @Override
    public Double getOrDefault(Object key, Double defaultValue) {
        if (this.containsKey(key)) {
            return this.get(key);
        }
        return defaultValue;
    }

    @Override
    public Double put(String key, Double value) {
        return this.vector.put(key, value);
    }

    @Override
    public Double remove(Object key) {
        return this.vector.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Double> m) {
        this.vector.putAll(m);
    }

    @Override
    public void clear() {
        this.vector.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.vector.keySet();
    }

    @Override
    public Collection<Double> values() {
        return this.vector.values();
    }

    @Override
    public Set<Entry<String, Double>> entrySet() {
        return this.vector.entrySet();
    }
}
