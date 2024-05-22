package com.openAPITalentoB.openAPITalentoB.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="document")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    @Lob
    private byte[] content;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String textContent;

}

