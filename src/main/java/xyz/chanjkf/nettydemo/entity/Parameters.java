package xyz.chanjkf.nettydemo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Parameters implements Serializable {
    private String type;
    private Long userId;
}
