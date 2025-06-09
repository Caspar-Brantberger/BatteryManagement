package DTO;

public class ChargeRequestDTO {

    private String charging;

    private String discharging;

    public ChargeRequestDTO(String charging, String discharging) {
        this.charging = charging;
        this.discharging = discharging;
    }
    public ChargeRequestDTO() {}

    public String getCharging() {
        return charging;
    }
    public void setCharging(String charging) {
        this.charging = charging;
    }
    public String getDischarging() {
        return discharging;
    }
    public void setDischarging(String discharging) {
        this.discharging = discharging;
    }
}
