package com.livestreaming.main.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Agency list entity class
 */
public class AgencyListBean {

    private int id;
    private String agencyCode;
    private String username;
    private String email;
    private String specialApprovalName;
    private String mobile;
    private String userProfilePic;
    private String approvalStatus;
    private String createdAt;
    private long totalRevenue;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JSONField(name = "agency_code")
    public String getAgencyCode() {
        return agencyCode;
    }

    @JSONField(name = "agency_code")
    public void setAgencyCode(String agencyCode) {
        this.agencyCode = agencyCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JSONField(name = "special_approval_name")
    public String getSpecialApprovalName() {
        return specialApprovalName;
    }

    @JSONField(name = "special_approval_name")
    public void setSpecialApprovalName(String specialApprovalName) {
        this.specialApprovalName = specialApprovalName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @JSONField(name = "user_profile_pic")
    public String getUserProfilePic() {
        return userProfilePic;
    }

    @JSONField(name = "user_profile_pic")
    public void setUserProfilePic(String userProfilePic) {
        this.userProfilePic = userProfilePic;
    }

    @JSONField(name = "approval_status")
    public String getApprovalStatus() {
        return approvalStatus;
    }

    @JSONField(name = "approval_status")
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    @JSONField(name = "created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    @JSONField(name = "created_at")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @JSONField(name = "total_revenue")
    public long getTotalRevenue() {
        return totalRevenue;
    }

    @JSONField(name = "total_revenue")
    public void setTotalRevenue(long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public String getTotalRevenueFormat() {
        return String.valueOf(this.totalRevenue);
    }
} 