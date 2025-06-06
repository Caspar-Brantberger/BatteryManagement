package DTO;

public class ChargeRequestDTO {

    private String charging;

    public ChargeRequestDTO(String charging) {
        this.charging = charging;
    }
    public ChargeRequestDTO() {}

    public String getCharging() {
        return charging;
    }
    public void setCharging(String charging) {
        this.charging = charging;
    }
}
