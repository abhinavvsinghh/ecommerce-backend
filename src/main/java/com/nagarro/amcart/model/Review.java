package com.nagarro.amcart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private String userId;
    private String userName;
    private int rating; // 1-5 stars
    private String comment;
    private Date createdAt;
}