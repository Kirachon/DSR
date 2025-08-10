package ph.gov.dsr.registration.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Household address entity
 */
@Entity
@Table(name = "household_addresses", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
public class HouseholdAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(name = "address_type", length = 20)
    private String addressType = "CURRENT";

    @Column(name = "street_address")
    private String streetAddress;

    @NotBlank
    @Column(name = "barangay", nullable = false, length = 100)
    private String barangay;

    @NotBlank
    @Column(name = "municipality", nullable = false, length = 100)
    private String municipality;

    @NotBlank
    @Column(name = "province", nullable = false, length = 100)
    private String province;

    @NotBlank
    @Column(name = "region", nullable = false, length = 50)
    private String region;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "country", length = 50)
    private String country = "Philippines";

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "housing_type", length = 50)
    private String housingType;

    @Column(name = "housing_material", length = 50)
    private String housingMaterial;

    @Column(name = "roof_material", length = 50)
    private String roofMaterial;

    @Column(name = "has_electricity")
    private Boolean hasElectricity = false;

    @Column(name = "has_water_supply")
    private Boolean hasWaterSupply = false;

    @Column(name = "has_toilet")
    private Boolean hasToilet = false;

    @Column(name = "water_source", length = 50)
    private String waterSource;

    @Column(name = "toilet_type", length = 50)
    private String toiletType;

    @Column(name = "is_current")
    private Boolean isCurrent = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public HouseholdAddress() {}

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getHousingType() {
        return housingType;
    }

    public void setHousingType(String housingType) {
        this.housingType = housingType;
    }

    public String getHousingMaterial() {
        return housingMaterial;
    }

    public void setHousingMaterial(String housingMaterial) {
        this.housingMaterial = housingMaterial;
    }

    public String getRoofMaterial() {
        return roofMaterial;
    }

    public void setRoofMaterial(String roofMaterial) {
        this.roofMaterial = roofMaterial;
    }

    public Boolean getHasElectricity() {
        return hasElectricity;
    }

    public void setHasElectricity(Boolean hasElectricity) {
        this.hasElectricity = hasElectricity;
    }

    public Boolean getHasWaterSupply() {
        return hasWaterSupply;
    }

    public void setHasWaterSupply(Boolean hasWaterSupply) {
        this.hasWaterSupply = hasWaterSupply;
    }

    public Boolean getHasToilet() {
        return hasToilet;
    }

    public void setHasToilet(Boolean hasToilet) {
        this.hasToilet = hasToilet;
    }

    public String getWaterSource() {
        return waterSource;
    }

    public void setWaterSource(String waterSource) {
        this.waterSource = waterSource;
    }

    public String getToiletType() {
        return toiletType;
    }

    public void setToiletType(String toiletType) {
        this.toiletType = toiletType;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (streetAddress != null && !streetAddress.trim().isEmpty()) {
            address.append(streetAddress).append(", ");
        }
        address.append(barangay).append(", ");
        address.append(municipality).append(", ");
        address.append(province).append(", ");
        address.append(region);
        if (zipCode != null && !zipCode.trim().isEmpty()) {
            address.append(" ").append(zipCode);
        }
        return address.toString();
    }

    @Override
    public String toString() {
        return "HouseholdAddress{" +
                "id=" + id +
                ", addressType='" + addressType + '\'' +
                ", municipality='" + municipality + '\'' +
                ", province='" + province + '\'' +
                ", isCurrent=" + isCurrent +
                '}';
    }
}
