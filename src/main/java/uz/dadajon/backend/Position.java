package uz.dadajon.backend;

public class Position {
    private String latitude;
    private String longitude;
    private String timestamp;
    private String msisdn;

    public Position(String latitude, String longitude, String timestamp, String msisdn) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.msisdn = msisdn;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getMsisdn() {
        return this.msisdn;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    @Override
    public String toString() {
        return "Latitude - " + this.latitude + " Longitude - " + this.longitude + " Timestamp - " + this.timestamp + " MSISDN - " + this.msisdn;
    }
}
