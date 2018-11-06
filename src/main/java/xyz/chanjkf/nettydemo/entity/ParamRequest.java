package xyz.chanjkf.nettydemo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParamRequest implements Serializable {
    private String event;
    private Parameters parameters;
}
