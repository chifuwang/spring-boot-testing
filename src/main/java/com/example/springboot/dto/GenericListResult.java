package com.example.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GenericListResult<T> {

    private T[] data;
    private Long total;
    private Long start;
    private String sort;
    private String order;
    private Long size;
}
