package com.dev.transcribeflow.settings;

import com.dev.transcribeflow.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "font_size")
    private double fontSize;

    @Column(name = "font_family")
    private String fontFamily;

    @Column(name = "subtitle_color")
    private String subtitleColor;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_language")
    private Language targetLanguage = Language.EN;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;
}
