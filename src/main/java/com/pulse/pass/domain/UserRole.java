package com.pulse.pass.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "user_roles")
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    public UserRole() {
    }

    public UserRole(User user, Role role) {
        this.user = user;
        this.id = new UserRoleId(user.getId(), role);
    }

    public UserRoleId getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Role getRole() {
        return id.getRole();
    }

    public void setId(UserRoleId id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
