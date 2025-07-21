package com.example.demo.model;

/**
 * Represents a Credit Card entity with its attributes and properties.
 * This class models the data structure for credit card information in the system.
 */
public class CreditCard {
    // =========================================
    // Fields (Properties of a Credit Card)
    // =========================================

    private String cardTitle;          // Title/name of the credit card
    private String cardImages;         // Image URLs or paths for the card
    private String annualFees;         // Annual fees associated with the card
    private String purchaseInterestRate; // Purchase interest rate
    private String cashInterestRate;   // Cash advance interest rate
    private String productValueProp;   // Value proposition of the card
    private String productBenefits;    // Benefits offered by the card
    private String bankName;           // Name of the issuing bank
    private String cardLink;

    // =========================================
    // Constructors
    // =========================================

    /**
     * Constructs a new CreditCard with all attributes.
     * 
     * @param cardTitle Title/name of the card
     * @param cardImages Image URLs or paths
     * @param annualFees Annual fees amount
     * @param purchaseInterestRate Purchase interest rate
     * @param cashInterestRate Cash advance interest rate
     * @param productValueProp Value proposition
     * @param productBenefits Card benefits
     * @param bankName Issuing bank name
     */
    public CreditCard(String cardTitle, String cardImages, String annualFees, 
                     String purchaseInterestRate, String cashInterestRate, 
                     String productValueProp, String productBenefits, String bankName, String cardLink) {
        this.cardTitle = cardTitle;
        this.cardImages = cardImages;
        this.annualFees = annualFees;
        this.purchaseInterestRate = purchaseInterestRate;
        this.cashInterestRate = cashInterestRate;
        this.productValueProp = productValueProp;
        this.productBenefits = productBenefits;
        this.bankName = bankName;
        this.cardLink = cardLink;
    }

    // =========================================
    // Getter and Setter Methods
    // =========================================

    public String getCardLink() {
        return cardLink;
    }

    public void setCardLink(String cardLink) {
        this.cardLink = cardLink;
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public void setCardTitle(String cardTitle) {
        this.cardTitle = cardTitle;
    }

    public String getCardImages() {
        return cardImages;
    }

    public void setCardImages(String cardImages) {
        this.cardImages = cardImages;
    }

    public String getAnnualFees() {
        return annualFees;
    }

    public void setAnnualFees(String annualFees) {
        this.annualFees = annualFees;
    }

    public String getPurchaseInterestRate() {
        return purchaseInterestRate;
    }

    public void setPurchaseInterestRate(String purchaseInterestRate) {
        this.purchaseInterestRate = purchaseInterestRate;
    }

    public String getCashInterestRate() {
        return cashInterestRate;
    }

    public void setCashInterestRate(String cashInterestRate) {
        this.cashInterestRate = cashInterestRate;
    }

    public String getProductValueProp() {
        return productValueProp;
    }

    public void setProductValueProp(String productValueProp) {
        this.productValueProp = productValueProp;
    }

    public String getProductBenefits() {
        return productBenefits;
    }

    public void setProductBenefits(String productBenefits) {
        this.productBenefits = productBenefits;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}