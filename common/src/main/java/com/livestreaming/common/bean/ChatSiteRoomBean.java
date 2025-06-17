package com.livestreaming.common.bean;

import java.util.ArrayList;

public class ChatSiteRoomBean {
    public int getLimit() {
        return this.limit; }
    public void setLimit(int limit) {
        this.limit = limit; }
    int limit;
    public int getSearch() {
        return this.search; }
    public void setSearch(int search) {
        this.search = search; }
    int search;
    public ArrayList<ChatSiteUserBean> getUsers() {
        return this.users; }
    public void setUsers(ArrayList<ChatSiteUserBean> users) {
        this.users = users; }
    ArrayList<ChatSiteUserBean> users;

}

