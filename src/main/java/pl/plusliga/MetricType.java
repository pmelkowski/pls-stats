package pl.plusliga;

public enum MetricType {

    SMALLINT ("%1.0f %4.2f %1.0f %1.0f"),
    INT      ("%2.0f %5.2f %2.0f %2.0f") ,
    FLOAT    ("%5.2f %5.2f %5.2f %5.2f"),
    PERCENT  ("%6.2f%% %6.2f%% %6.2f%% %6.2f%%");

    private final String format;

    private MetricType(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

}
