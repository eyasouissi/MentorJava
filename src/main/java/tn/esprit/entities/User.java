package tn.esprit.entities;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

import java.time.LocalDateTime;
import java.util.*;

public class User {
    private Long id;
    private String email;
    private String password;
    private Set<String> roles = new HashSet<>();
    private String bio;
    private String name;
    private String gender;
    private LocalDateTime creationDate;
    private boolean verified = false;
    private String passwordResetToken;
    private LocalDateTime passwordResetRequestedAt;
    private String diplome;
    private String speciality;
    private Integer age;
    private String country;
    private String plainPassword;
    private String verificationToken;
    private LocalDateTime verificationTokenExpiry;
    private String pfp;
    private String bg;
    private String locale = "en";
    private int karmaPoints = 0;
    private String oauthId;
    private String oauthType;
    private boolean restricted = false;
    private String googleId;

    // Relationships
    private List<Post> posts = new ArrayList<>();
    private Set<Post> likedPosts = new HashSet<>();
    private Set<GroupStudent> groups = new HashSet<>();
    private Set<Level> completedLevels = new HashSet<>();
    private List<Rating> ratings = new ArrayList<>();
    private User currentUser;
    private Set<Room> rooms = new HashSet<>();

    public User() {
        this.creationDate = LocalDateTime.now();
        this.roles.add("ROLE_STUDENT");
    }

    public User(String email, String name, String password) {
        this();
        this.email = email;
        this.name = name;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public void addRole(String role) { this.roles.add(role); }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getPasswordResetToken() { return passwordResetToken; }
    public void setPasswordResetToken(String passwordResetToken) { this.passwordResetToken = passwordResetToken; }

    public LocalDateTime getPasswordResetRequestedAt() { return passwordResetRequestedAt; }
    public void setPasswordResetRequestedAt(LocalDateTime passwordResetRequestedAt) { this.passwordResetRequestedAt = passwordResetRequestedAt; }

    public String getDiplome() { return diplome; }
    public void setDiplome(String diplome) { this.diplome = diplome; }

    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPlainPassword() { return plainPassword; }
    public void setPlainPassword(String plainPassword) { this.plainPassword = plainPassword; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public String getPfp() { return pfp; }
    public void setPfp(String pfp) { this.pfp = pfp; }

    public String getBg() { return bg; }
    public void setBg(String bg) { this.bg = bg; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public int getKarmaPoints() { return karmaPoints; }
    public void setKarmaPoints(int karmaPoints) { this.karmaPoints = karmaPoints; }

    public String getOauthId() { return oauthId; }
    public void setOauthId(String oauthId) { this.oauthId = oauthId; }

    public String getOauthType() { return oauthType; }
    public void setOauthType(String oauthType) { this.oauthType = oauthType; }

    public boolean isRestricted() { return restricted; }
    public void setRestricted(boolean restricted) { this.restricted = restricted; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }

    public Set<Post> getLikedPosts() { return likedPosts; }
    public void setLikedPosts(Set<Post> likedPosts) { this.likedPosts = likedPosts; }

    public Set<GroupStudent> getGroups() { return groups; }
    public void setGroups(Set<GroupStudent> groups) { this.groups = groups; }

    public Set<Level> getCompletedLevels() { return completedLevels; }
    public void setCompletedLevels(Set<Level> completedLevels) { this.completedLevels = completedLevels; }

    public List<Rating> getRatings() { return ratings; }
    public void setRatings(List<Rating> ratings) { this.ratings = ratings; }

    // Business methods
    public void addPost(Post post) {
        posts.add(post);
        post.setUser(this);
    }

    public void removePost(Post post) {
        posts.remove(post);
        post.setUser(null);
    }

    public void likePost(Post post) {
        likedPosts.add(post);
        post.getLikedByUsers().add(this);
    }

    public void unlikePost(Post post) {
        likedPosts.remove(post);
        post.getLikedByUsers().remove(this);
    }

    public void addGroup(GroupStudent group) {
        groups.add(group);
        group.getMembers().add(this);
    }

    public void removeGroup(GroupStudent group) {
        groups.remove(group);
        group.getMembers().remove(this);
    }

    public void completeLevel(Level level) {
        if (!completedLevels.contains(level)) {
            completedLevels.add(level);
            karmaPoints += 1;
        }
    }

    public boolean hasCompletedLevel(Level level) {
        return completedLevels.contains(level);
    }

    public void addRating(Rating rating) {
        ratings.add(rating);
        rating.setUser(this);
    }

    public void removeRating(Rating rating) {
        ratings.remove(rating);
        rating.setUser(null);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", roles=" + roles +
                ", verified=" + verified +
                ", karmaPoints=" + karmaPoints +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public void setVerificationTokenExpiry(LocalDateTime verificationTokenExpiry) {
        this.verificationTokenExpiry = verificationTokenExpiry;
    }

    public LocalDateTime getVerificationTokenExpiry() {
        return verificationTokenExpiry;
    }


    public static class Builder {
        private String email;
        private String name;
        private String password;
        private Set<String> roles = new HashSet<>();

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder role(String role) {
            this.roles.add(role);
            return this;
        }

        public User build() {
            User user = new User(email, name, password);
            user.setRoles(roles);
            return user;
        }
    }
    public Set<Room> getRooms() { return rooms; }
    public void setRooms(Set<Room> rooms) { this.rooms = rooms; }

}
