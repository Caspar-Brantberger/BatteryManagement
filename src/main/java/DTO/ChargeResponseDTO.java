package DTO;

public class ChargeResponseDTO {

    private String charging;

    public ChargeResponseDTO() {}

    public ChargeResponseDTO(String charging) {
        this.charging = charging;
    }
    public String getCharging() {
        return charging;
    }
    public void setCharging(String charging) {
        this.charging = charging;
    }
}
