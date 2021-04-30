package com.example.demo.database.models;

import com.example.demo.utils.DateUtils;
import com.example.demo.utils.LocalDateTimeConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventHistoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column
    private String who_did;

    @Column
    private String action;

    @Column(columnDefinition="TEXT")
    private String description;
}
