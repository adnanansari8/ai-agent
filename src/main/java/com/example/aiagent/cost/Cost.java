package com.example.aiagent.cost;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cost")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cost {

    @Id
    private Long id;
    private String name;
    private double cost;

}
