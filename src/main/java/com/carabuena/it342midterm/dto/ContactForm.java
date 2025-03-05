package com.carabuena.it342midterm.dto;

public class ContactForm {
    private String givenName;
    private String familyName;
    private String email;
    private String phone;
    private String resourceName;

    // Getters and setters
    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getName() {
        if (givenName != null && familyName != null) {
            return givenName + " " + familyName;
        } else if (givenName != null) {
            return givenName;
        } else if (familyName != null) {
            return familyName;
        }
        return "";
    }

    public void setName(String fullName) {
        if (fullName != null) {
            String[] parts = fullName.split(" ", 2);
            this.givenName = parts[0];
            this.familyName = parts.length > 1 ? parts[1] : "";
        }
    }
}