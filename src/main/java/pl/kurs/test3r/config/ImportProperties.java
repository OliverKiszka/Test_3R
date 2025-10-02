package pl.kurs.test3r.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.import")
public class ImportProperties {

    private int batchSize = 100;
    private int maxConcurrentImports = 1;
    private double minimumTps = 10.0;

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getMaxConcurrentImports() {
        return maxConcurrentImports;
    }

    public void setMaxConcurrentImports(int maxConcurrentImports) {
        this.maxConcurrentImports = maxConcurrentImports;
    }

    public double getMinimumTps() {
        return minimumTps;
    }

    public void setMinimumTps(double minimumTps) {
        this.minimumTps = minimumTps;
    }
}
