package com.vaultcore.vbms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name="user_profiles") @Getter @Setter @NoArgsConstructor
public class UserProfile {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="profile_id") private Integer profileId;
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id", nullable=false) private User user;
    @Column(name="first_name") private String firstName;
    @Column(name="last_name") private String lastName;
    private String phone;
    private LocalDate dob;
    private String gender;
    private String address;
}
