package uz.dadajon.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CtailResponse {
    @JsonProperty("currentTime")
    private String currentTime;

    @JsonProperty("switchNumber")
    private String switchNumber;

    @JsonProperty("msisdn")
    private String msisdn;

    @JsonProperty("imsi")
    private String imsi;

    @JsonProperty("imei")
    private String imei;

    @JsonProperty("mnc")
    private String mnc;

    @JsonProperty("lac")
    private String lac;

    @JsonProperty("cellId")
    private String cellId;
}
